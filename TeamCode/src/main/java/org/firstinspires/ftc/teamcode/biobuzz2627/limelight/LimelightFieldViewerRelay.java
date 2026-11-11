package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Small robot-side HTTP bridge for FieldViewer.
 *
 * <p>GET / proxies the Limelight MJPEG stream. GET /api/pipeline reports the active pipeline, and
 * POST /api/pipeline?index=N switches it through the FTC Limelight API. The pipeline/config route
 * proxies stored pipeline JSON to Limelight's REST API for FieldViewer's custom editor. The server
 * only exists while an OpMode owns the Limelight and calls {@link #start()}.</p>
 */
public final class LimelightFieldViewerRelay {
    private final Limelight3A limelight;
    private final ExecutorService clients = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "Limelight-FieldViewer-client");
        thread.setDaemon(true);
        return thread;
    });
    private volatile boolean running;
    private volatile int selectedPipeline;
    private ServerSocket serverSocket;
    private Thread acceptThread;

    public LimelightFieldViewerRelay(Limelight3A limelight, int initialPipeline) {
        this.limelight = limelight;
        selectedPipeline = initialPipeline;
    }

    public synchronized void start() throws IOException {
        if (running || !HiveAssistConfig.FIELD_VIEWER_LIMELIGHT_RELAY_ENABLED) return;
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(HiveAssistConfig.LIMELIGHT_RELAY_PORT));
        running = true;
        acceptThread = new Thread(this::acceptLoop, "Limelight-FieldViewer-relay");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public synchronized void stop() {
        running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
                // Closing the socket is only used to wake the accept thread.
            }
            serverSocket = null;
        }
        if (acceptThread != null) {
            acceptThread.interrupt();
            acceptThread = null;
        }
        clients.shutdownNow();
    }

    public boolean isRunning() {
        return running;
    }

    public int selectedPipeline() {
        return selectedPipeline;
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(5000);
                clients.execute(() -> handle(socket));
            } catch (IOException error) {
                if (running) {
                    // Keep accepting after a temporary network error.
                }
            }
        }
    }

    private void handle(Socket socket) {
        try (Socket client = socket;
             OutputStream output = client.getOutputStream()) {
            Request request = readRequest(client.getInputStream());
            if (request == null) return;
            if (request.method.isEmpty() || request.target.isEmpty()) {
                sendJson(output, 400, "{\"error\":\"bad request\"}");
                return;
            }

            String path = request.target;
            String query = "";
            int queryStart = request.target.indexOf('?');
            if (queryStart >= 0) {
                path = request.target.substring(0, queryStart);
                query = request.target.substring(queryStart + 1);
            }

            if ("/".equals(path) && "GET".equals(request.method)) {
                proxyMjpeg(output);
            } else if ("/api/pipeline".equals(path) && "GET".equals(request.method)) {
                // getStatus() can block while the camera is busy streaming. The relay already
                // tracks every pipeline selected through this OpMode, so return that immediately.
                sendPipeline(output, false);
            } else if ("/api/pipeline".equals(path) && "POST".equals(request.method)) {
                switchPipeline(output, query);
            } else if ("/api/pipeline/config".equals(path)
                    && ("GET".equals(request.method) || "POST".equals(request.method))) {
                pipelineConfig(output, request.method, query, request.body);
            } else {
                sendJson(output, 404, "{\"error\":\"not found\"}");
            }
        } catch (IOException ignored) {
            // The viewer disconnected or the robot network changed.
        }
    }

    private void pipelineConfig(OutputStream output, String method, String query, byte[] body)
            throws IOException {
        String rawIndex = queryValue(query, "index");
        int index;
        try {
            index = Integer.parseInt(rawIndex);
        } catch (RuntimeException error) {
            sendJson(output, 400, "{\"error\":\"pipeline index must be an integer\"}");
            return;
        }
        if (index < HiveAssistConfig.LIMELIGHT_MIN_PIPELINE
                || index > HiveAssistConfig.LIMELIGHT_MAX_PIPELINE) {
            sendJson(output, 400, "{\"error\":\"pipeline index out of range\"}");
            return;
        }
        if ("POST".equals(method) && (body == null || body.length == 0)) {
            sendJson(output, 400, "{\"error\":\"pipeline JSON is empty\"}");
            return;
        }

        if ("POST".equals(method)) {
            boolean uploaded = limelight.uploadPipeline(
                    new String(body, StandardCharsets.UTF_8), index);
            if (!uploaded) {
                sendJson(output, 502,
                        "{\"error\":\"Limelight rejected the pipeline upload\"}");
                return;
            }
            if (index == selectedPipeline) limelight.pipelineSwitch(index);
            sendJson(output, 200, "{\"ok\":true,\"pipeline\":" + index + "}");
            return;
        }

        String pipelineJson;
        try {
            pipelineJson = loadPipelineJson(index);
        } catch (IOException error) {
            sendJson(output, 502, "{\"error\":\"" + escapeJson(error.getMessage()) + "\"}");
            return;
        }
        byte[] responseBody = pipelineJson.getBytes(StandardCharsets.UTF_8);
        writeHeaders(output, 200, "application/json; charset=utf-8", responseBody.length);
        output.write(responseBody);
        output.flush();
    }

    /** FTC SDK 12 has the REST-backed loader but currently marks it private. */
    private String loadPipelineJson(int index) throws IOException {
        try {
            Method loader = Limelight3A.class.getDeclaredMethod("getPipelineAtIndex", int.class);
            loader.setAccessible(true);
            Object pipeline = loader.invoke(limelight, index);
            if (pipeline == null) {
                throw new IOException("Limelight did not return pipeline JSON; verify LimelightOS is current");
            }
            String json = pipeline.toString().trim();
            if (!json.startsWith("{")) {
                throw new IOException("Limelight returned an unsupported pipeline format");
            }
            return json;
        } catch (ReflectiveOperationException error) {
            throw new IOException("FTC SDK pipeline loader unavailable", error);
        }
    }

    private void switchPipeline(OutputStream output, String query) throws IOException {
        String raw = queryValue(query, "index");
        int pipeline;
        try {
            pipeline = Integer.parseInt(raw);
        } catch (RuntimeException error) {
            sendJson(output, 400, "{\"error\":\"pipeline index must be an integer\"}");
            return;
        }
        if (pipeline < HiveAssistConfig.LIMELIGHT_MIN_PIPELINE
                || pipeline > HiveAssistConfig.LIMELIGHT_MAX_PIPELINE) {
            sendJson(output, 400, "{\"error\":\"pipeline index out of range\"}");
            return;
        }
        limelight.pipelineSwitch(pipeline);
        selectedPipeline = pipeline;
        sendPipeline(output, false);
    }

    private void sendPipeline(OutputStream output, boolean refreshFromCamera) throws IOException {
        if (refreshFromCamera) {
            try {
                selectedPipeline = limelight.getStatus().getPipelineIndex();
            } catch (RuntimeException ignored) {
                // Report the last pipeline selected through this relay while the camera reconnects.
            }
        }
        sendJson(output, 200, "{\"pipeline\":" + selectedPipeline
                + ",\"minimum\":" + HiveAssistConfig.LIMELIGHT_MIN_PIPELINE
                + ",\"maximum\":" + HiveAssistConfig.LIMELIGHT_MAX_PIPELINE + "}");
    }

    private void proxyMjpeg(OutputStream output) throws IOException {
        HttpURLConnection camera = (HttpURLConnection) new URL(
                "http", HiveAssistConfig.LIMELIGHT_STREAM_HOST,
                HiveAssistConfig.LIMELIGHT_STREAM_PORT, "/").openConnection();
        camera.setConnectTimeout(3000);
        camera.setReadTimeout(10000);
        camera.setRequestProperty("User-Agent", "BioBuzz-FieldViewer-relay");
        try {
            int code = camera.getResponseCode();
            if (code != 200) {
                sendJson(output, 502, "{\"error\":\"Limelight stream unavailable\"}");
                return;
            }
            String contentType = camera.getContentType();
            if (contentType == null || contentType.trim().isEmpty()) {
                contentType = "multipart/x-mixed-replace";
            }
            writeHeaders(output, 200, contentType, -1);
            try (InputStream input = camera.getInputStream()) {
                byte[] buffer = new byte[16 * 1024];
                int count;
                while (running && (count = input.read(buffer)) >= 0) {
                    output.write(buffer, 0, count);
                    output.flush();
                }
            }
        } finally {
            camera.disconnect();
        }
    }

    private static String queryValue(String query, String name) throws IOException {
        for (String part : query.split("&")) {
            int equals = part.indexOf('=');
            String key = equals < 0 ? part : part.substring(0, equals);
            if (name.equals(URLDecoder.decode(key, "UTF-8"))) {
                return equals < 0 ? "" : URLDecoder.decode(part.substring(equals + 1), "UTF-8");
            }
        }
        return "";
    }

    private static String escapeJson(String text) {
        if (text == null) return "unknown Limelight error";
        return text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", " ").replace("\n", " ").replace("\t", " ");
    }

    private static Request readRequest(InputStream input) throws IOException {
        ByteArrayOutputStream headers = new ByteArrayOutputStream();
        int matched = 0;
        while (matched < 4) {
            int value = input.read();
            if (value < 0) return null;
            headers.write(value);
            if (headers.size() > 64 * 1024) throw new IOException("request headers too large");
            int expected = matched == 0 || matched == 2 ? '\r' : '\n';
            if (value == expected) {
                matched++;
            } else {
                matched = value == '\r' ? 1 : 0;
            }
        }

        String text = new String(headers.toByteArray(), StandardCharsets.US_ASCII);
        String[] lines = text.split("\\r\\n");
        if (lines.length == 0) return null;
        String[] first = lines[0].split(" ");
        if (first.length < 2) return new Request("", "", new byte[0]);
        int contentLength = 0;
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon > 0 && "content-length".equalsIgnoreCase(lines[i].substring(0, colon).trim())) {
                contentLength = Integer.parseInt(lines[i].substring(colon + 1).trim());
            }
        }
        if (contentLength < 0 || contentLength > 4 * 1024 * 1024) {
            throw new IOException("request body too large");
        }
        byte[] body = new byte[contentLength];
        int offset = 0;
        while (offset < contentLength) {
            int count = input.read(body, offset, contentLength - offset);
            if (count < 0) throw new IOException("incomplete request body");
            offset += count;
        }
        return new Request(first[0], first[1], body);
    }

    private static final class Request {
        final String method;
        final String target;
        final byte[] body;

        Request(String method, String target, byte[] body) {
            this.method = method;
            this.target = target;
            this.body = body;
        }
    }

    private static void sendJson(OutputStream output, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        writeHeaders(output, status, "application/json; charset=utf-8", body.length);
        output.write(body);
        output.flush();
    }

    private static void writeHeaders(OutputStream output, int status, String type, int length)
            throws IOException {
        String reason = status == 200 ? "OK" : status == 400 ? "Bad Request"
                : status == 404 ? "Not Found" : "Bad Gateway";
        StringBuilder headers = new StringBuilder("HTTP/1.1 ").append(status).append(' ')
                .append(reason).append("\r\nContent-Type: ").append(type)
                .append("\r\nCache-Control: no-store\r\nConnection: close\r\n");
        if (length >= 0) headers.append("Content-Length: ").append(length).append("\r\n");
        headers.append("\r\n");
        output.write(headers.toString().getBytes(StandardCharsets.US_ASCII));
        output.flush();
    }
}
