package org.firstinspires.ftc.teamcode.biobuzz2627.calibration;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.vision.LimelightService;

/** Read-only comparison for measuring the Limelight-to-Road-Runner field transform. */
@Disabled
@TeleOp(name = "LOCKED Limelight Pose Calibration", group = "Calibration")
public final class LimelightPoseCalibrationTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() {
        Pose2d initialPose = new Pose2d(
                HiveAssistConfig.START_FIELD_X_IN - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                HiveAssistConfig.START_FIELD_Y_IN - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                Math.toRadians(HiveAssistConfig.START_HEADING_DEG));
        MecanumDrive drive = new MecanumDrive(
                hardwareMap, initialPose, AutomationConfig.mecanumLocalizerMode());
        LimelightService camera = new LimelightService(
                hardwareMap.get(Limelight3A.class, "limelight"));
        camera.start();
        waitForStart();

        try {
            while (opModeIsActive()) {
                drive.updatePoseEstimate();
                Pose2d odometry = drive.localizer.getPose();
                camera.update(Math.toDegrees(odometry.heading.toDouble()));
                telemetry.addData("Road Runner", "x %.2f y %.2f h %.1f°",
                        odometry.position.x, odometry.position.y,
                        Math.toDegrees(odometry.heading.toDouble()));

                LLResult result = camera.latest();
                if (result == null || !result.isValid()) {
                    telemetry.addLine("No valid Limelight pose");
                } else {
                    Pose3D raw = result.getBotpose_MT2();
                    double x = raw.getPosition().toUnit(DistanceUnit.INCH).x;
                    double y = raw.getPosition().toUnit(DistanceUnit.INCH).y;
                    double heading = raw.getOrientation().getYaw(AngleUnit.DEGREES);
                    telemetry.addData("Raw Limelight", "x %.2f y %.2f h %.1f°", x, y, heading);
                    double transformedX = x * AutomationConfig.LIMELIGHT_X_SIGN
                            + AutomationConfig.LIMELIGHT_X_OFFSET_IN;
                    double transformedY = y * AutomationConfig.LIMELIGHT_Y_SIGN
                            + AutomationConfig.LIMELIGHT_Y_OFFSET_IN;
                    double transformedHeading = heading * AutomationConfig.LIMELIGHT_HEADING_SIGN
                            + AutomationConfig.LIMELIGHT_HEADING_OFFSET_DEG;
                    telemetry.addData("Configured transform", "x %.2f y %.2f h %.1f°",
                            transformedX, transformedY, transformedHeading);
                    telemetry.addData("Delta", "x %.2f y %.2f h %.1f°",
                            transformedX - odometry.position.x,
                            transformedY - odometry.position.y,
                            wrapDegrees(transformedHeading
                                    - Math.toDegrees(odometry.heading.toDouble())));
                    telemetry.addData("Quality", "tags %d, avgDist %.2f m, stale %d ms",
                            result.getBotposeTagCount(), result.getBotposeAvgDist(), result.getStaleness());
                }
                telemetry.addData("Correction enabled",
                        AutomationConfig.LIMELIGHT_POSE_TRANSFORM_VERIFIED);
                telemetry.update();
            }
        } finally {
            camera.stop();
        }
    }

    private static double wrapDegrees(double angle) {
        return Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(angle)),
                Math.cos(Math.toRadians(angle))));
    }
}
