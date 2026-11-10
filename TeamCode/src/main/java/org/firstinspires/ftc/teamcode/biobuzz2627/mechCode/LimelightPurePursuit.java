package org.firstinspires.ftc.teamcode.biobuzz2627.mechCode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamServer;
import org.firstinspires.ftc.teamcode.biobuzz2627.PurePursuit.LimelightDriverStationStream;
import org.firstinspires.ftc.teamcode.biobuzz2627.PurePursuit.LimelightStreamRelay;
import org.firstinspires.ftc.teamcode.biobuzz2627.PurePursuit.PurePursuitConfig;

public class LimelightPurePursuit {
    private Limelight3A limelight;
    private LimelightStreamRelay streamRelay;
    private LimelightDriverStationStream driverStationStream;
    LLResult result;
    private boolean isValid;


    public void initLimelightBlue(HardwareMap hardwareMap, Telemetry telemetry) {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        telemetry.setMsTransmissionInterval(11);
        limelight.pipelineSwitch(0);
        // Starts polling for data.  If you neglect to call start(), getLatestResult() will return null.
        limelight.start();
        startStreamRelay(telemetry);
        startDriverStationStream(telemetry);

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
    }

    public void initLimelightRed(HardwareMap hardwareMap, Telemetry telemetry) {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        telemetry.setMsTransmissionInterval(11);
        limelight.pipelineSwitch(1);
        // Starts polling for data.  If you neglect to call start(), getLatestResult() will return null.
        limelight.start();
        startStreamRelay(telemetry);
        startDriverStationStream(telemetry);

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
    }

    public boolean isLimeValid() {
        result = limelight.getLatestResult();

        if (result.isValid()) {
            isValid = true;
        } else {
            isValid = false;
        }

        return isValid;
    }

    public double getLimeTx() {
        result = limelight.getLatestResult();

        return result.getTx();
    }

    public void stop() {
        if (streamRelay != null) {
            streamRelay.stop();
            streamRelay = null;
        }
        if (driverStationStream != null) {
            CameraStreamServer.getInstance().setSource(null);
            driverStationStream.stop();
            driverStationStream = null;
        }
        if (limelight != null) {
            limelight.stop();
        }
    }

    private void startStreamRelay(Telemetry telemetry) {
        if (!PurePursuitConfig.STREAM_LIMELIGHT_TO_FIELD_VIEWER) {
            return;
        }
        streamRelay = new LimelightStreamRelay(
                PurePursuitConfig.LIMELIGHT_HOST,
                PurePursuitConfig.LIMELIGHT_STREAM_PORT,
                PurePursuitConfig.LIMELIGHT_RELAY_PORT);
        streamRelay.start();
        telemetry.addData("Limelight video", streamRelay.getStatus());
    }

    private void startDriverStationStream(Telemetry telemetry) {
        driverStationStream = new LimelightDriverStationStream(
                PurePursuitConfig.LIMELIGHT_HOST,
                PurePursuitConfig.LIMELIGHT_STREAM_PORT);
        CameraStreamServer.getInstance().setSource(driverStationStream);
        telemetry.addLine("Driver Station: menu > Camera Stream (tap image to refresh)");
    }
}
