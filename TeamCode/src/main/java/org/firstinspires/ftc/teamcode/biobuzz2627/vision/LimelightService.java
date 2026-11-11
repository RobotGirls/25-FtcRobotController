package org.firstinspires.ftc.teamcode.biobuzz2627.vision;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;

/** Owns the camera lifecycle and polls exactly once per robot loop. */
public final class LimelightService {
    private final Limelight3A limelight;
    private LLResult latest;

    public LimelightService(Limelight3A limelight) {
        this.limelight = limelight;
    }

    public void start() {
        limelight.setPollRateHz(50);
        limelight.pipelineSwitch(HiveAssistConfig.LIMELIGHT_APRILTAG_PIPELINE);
        limelight.start();
    }

    /** Supplies the field-relative robot heading required by MegaTag2, then polls once. */
    public void update(double robotYawDegrees) {
        updateRobotOrientation(robotYawDegrees);
        latest = limelight.getLatestResult();
    }

    /** MegaTag2 requires the current field-relative robot yaw before polling pose. */
    public void updateRobotOrientation(double robotYawDegrees) {
        if (!Double.isFinite(robotYawDegrees)) {
            throw new IllegalArgumentException("Robot yaw must be finite");
        }
        limelight.updateRobotOrientation(robotYawDegrees);
    }

    public LLResult latest() { return latest; }
    public void stop() { limelight.stop(); }
}
