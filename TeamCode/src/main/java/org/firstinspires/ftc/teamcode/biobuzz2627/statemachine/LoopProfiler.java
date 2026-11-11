package org.firstinspires.ftc.teamcode.biobuzz2627.statemachine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Rolling loop-rate and named-section profiler with no Android dependencies. */
public final class LoopProfiler {
    private static final long SAMPLE_WINDOW_NANOS = 500_000_000L;

    private final Map<String, Long> sectionStarts = new LinkedHashMap<>();
    private final Map<String, Long> sectionTotals = new LinkedHashMap<>();
    private final Map<String, Double> sectionAveragesMillis = new LinkedHashMap<>();
    private long windowStartedNanos = System.nanoTime();
    private long loopCount;
    private double loopsPerSecond;

    public void beginLoop() {
        long now = System.nanoTime();
        if (now - windowStartedNanos >= SAMPLE_WINDOW_NANOS) {
            double seconds = (now - windowStartedNanos) / 1_000_000_000.0;
            loopsPerSecond = seconds > 0 ? loopCount / seconds : 0;
            for (Map.Entry<String, Long> entry : sectionTotals.entrySet()) {
                double average = loopCount == 0 ? 0
                        : entry.getValue() / 1_000_000.0 / loopCount;
                sectionAveragesMillis.put(entry.getKey(), average);
            }
            windowStartedNanos = now;
            loopCount = 0;
            sectionTotals.clear();
        }
    }

    public void endLoop() {
        loopCount++;
    }

    public void beginSection(String name) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Section needs a name");
        sectionStarts.put(name, System.nanoTime());
    }

    public void endSection(String name) {
        Long start = sectionStarts.remove(name);
        if (start == null) throw new IllegalStateException("Section was not started: " + name);
        long elapsed = System.nanoTime() - start;
        Long total = sectionTotals.get(name);
        sectionTotals.put(name, (total == null ? 0 : total) + elapsed);
    }

    public double loopsPerSecond() {
        return loopsPerSecond;
    }

    public Map<String, Double> sectionAveragesMillis() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(sectionAveragesMillis));
    }
}
