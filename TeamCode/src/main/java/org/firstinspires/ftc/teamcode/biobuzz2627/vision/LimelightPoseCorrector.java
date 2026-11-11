package org.firstinspires.ftc.teamcode.biobuzz2627.vision;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.hardware.limelightvision.LLResult;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;

/** Applies bounded, blended Limelight corrections only after the field transform is verified. */
public final class LimelightPoseCorrector {
    private String status = "not evaluated";

    public boolean update(MecanumDrive drive, LLResult result, LimelightTargeting targeting) {
        if (!AutomationConfig.LIMELIGHT_POSE_TRANSFORM_VERIFIED) {
            status = "pose transform unverified";
            return false;
        }
        if (result == null || !result.isValid() || !targeting.selectedTagFresh()) {
            status = "no selected-tag pose";
            return false;
        }
        double latency = result.getCaptureLatency() + result.getTargetingLatency();
        if (result.getStaleness() > AutomationConfig.LIMELIGHT_MAX_STALENESS_MS
                || latency > AutomationConfig.LIMELIGHT_MAX_TOTAL_LATENCY_MS
                || result.getBotposeTagCount() < AutomationConfig.LIMELIGHT_MIN_TAG_COUNT
                || result.getBotposeAvgDist() > AutomationConfig.LIMELIGHT_MAX_TAG_DISTANCE_M) {
            status = "quality gate rejected pose";
            return false;
        }

        Pose3D cameraPose = result.getBotpose_MT2();
        double measuredX = cameraPose.getPosition().toUnit(DistanceUnit.INCH).x
                * AutomationConfig.LIMELIGHT_X_SIGN + AutomationConfig.LIMELIGHT_X_OFFSET_IN;
        double measuredY = cameraPose.getPosition().toUnit(DistanceUnit.INCH).y
                * AutomationConfig.LIMELIGHT_Y_SIGN + AutomationConfig.LIMELIGHT_Y_OFFSET_IN;
        double measuredHeading = Math.toRadians(
                cameraPose.getOrientation().getYaw(AngleUnit.DEGREES)
                        * AutomationConfig.LIMELIGHT_HEADING_SIGN
                        + AutomationConfig.LIMELIGHT_HEADING_OFFSET_DEG);

        Pose2d current = drive.localizer.getPose();
        double distanceJump = Math.hypot(
                measuredX - current.position.x, measuredY - current.position.y);
        double headingJump = wrap(measuredHeading - current.heading.toDouble());
        if (distanceJump > AutomationConfig.LIMELIGHT_MAX_TRANSLATION_JUMP_IN
                || Math.abs(Math.toDegrees(headingJump))
                > AutomationConfig.LIMELIGHT_MAX_HEADING_JUMP_DEG) {
            status = "innovation gate rejected pose";
            return false;
        }

        double blend = clip(AutomationConfig.LIMELIGHT_BLEND, 0, 1);
        drive.localizer.setPose(new Pose2d(
                new Vector2d(
                        current.position.x + (measuredX - current.position.x) * blend,
                        current.position.y + (measuredY - current.position.y) * blend),
                current.heading.toDouble() + headingJump * blend));
        status = "pose corrected";
        return true;
    }

    public String status() { return status; }
    private static double wrap(double angle) { return Math.atan2(Math.sin(angle), Math.cos(angle)); }
    private static double clip(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
