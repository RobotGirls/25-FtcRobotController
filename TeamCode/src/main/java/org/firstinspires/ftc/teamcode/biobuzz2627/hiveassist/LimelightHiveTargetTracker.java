package org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.firstinspires.ftc.teamcode.biobuzz2627.limelight.LimelightShootingRange;
import org.firstinspires.ftc.teamcode.biobuzz2627.limelight.LimelightFieldViewerRelay;

import java.io.IOException;

/** Filters Limelight fiducials to the color/side cluster selected by the driver. */
public final class LimelightHiveTargetTracker {
    private final Limelight3A limelight;
    private final LimelightShootingRange shootingRange = new LimelightShootingRange();
    private final LimelightFieldViewerRelay fieldViewerRelay;
    private long lastTargetSeenAt;
    private int lastTagId = -1;
    private LimelightShootingRange.Measurement latestRange;
    private boolean selectedTagVisibleNow;
    private String fieldViewerRelayStatus = "stopped";

    public LimelightHiveTargetTracker(Limelight3A limelight) {
        this.limelight = limelight;
        fieldViewerRelay = new LimelightFieldViewerRelay(
                limelight, HiveAssistConfig.LIMELIGHT_APRILTAG_PIPELINE);
    }

    public void start() {
        limelight.setPollRateHz(50);
        limelight.pipelineSwitch(HiveAssistConfig.LIMELIGHT_APRILTAG_PIPELINE);
        limelight.start();
        try {
            fieldViewerRelay.start();
            fieldViewerRelayStatus = fieldViewerRelay.isRunning() ? "online" : "disabled";
        } catch (IOException error) {
            // FieldViewer is optional; a relay failure must not disable driving or turret aiming.
            fieldViewerRelayStatus = "unavailable - " + error.getMessage();
        }
    }

    public void stop() {
        fieldViewerRelay.stop();
        fieldViewerRelayStatus = "stopped";
        limelight.stop();
    }

    public void update(HiveTargetSelection selection) {
        selectedTagVisibleNow = false;
        LLResult result = limelight.getLatestResult();
        if (result == null || !result.isValid()) return;

        for (LLResultTypes.FiducialResult fiducial : result.getFiducialResults()) {
            int id = fiducial.getFiducialId();
            if (selection.matchesTag(id)) {
                lastTagId = id;
                lastTargetSeenAt = nowMillis();
                latestRange = shootingRange.measure(fiducial);
                selectedTagVisibleNow = true;
                return;
            }
        }
    }

    public void invalidate() {
        lastTargetSeenAt = 0;
        lastTagId = -1;
        latestRange = null;
        selectedTagVisibleNow = false;
    }

    public boolean targetReady() {
        return lastTargetSeenAt != 0
                && nowMillis() - lastTargetSeenAt <= HiveAssistConfig.TARGET_LOSS_GRACE_MS;
    }

    public int lastTagId() {
        return lastTagId;
    }

    public LimelightShootingRange.Measurement latestRange() {
        return targetReady() ? latestRange : null;
    }

    /** True only when the latest Limelight frame contains the selected tag. */
    public boolean selectedTagVisibleNow() {
        return selectedTagVisibleNow;
    }

    public String fieldViewerRelayStatus() {
        return fieldViewerRelayStatus;
    }

    public boolean inVerifiedShootingRange() {
        return targetReady() && shootingRange.isInVerifiedShootingWindow(latestRange);
    }

    private static long nowMillis() {
        return System.nanoTime() / 1_000_000L;
    }
}
