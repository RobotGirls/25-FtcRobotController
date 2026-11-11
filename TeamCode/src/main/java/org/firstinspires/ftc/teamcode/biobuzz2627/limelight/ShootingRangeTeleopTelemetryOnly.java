package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;

/** Camera-only test OpMode for calibrating the shared shooting-range limits. */
@TeleOp(name = "ShootingRangeTeleop")
public final class ShootingRangeTeleopTelemetryOnly extends LinearOpMode {
    @Override
    public void runOpMode() {
        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");
        LimelightShootingRange shootingRange = new LimelightShootingRange();

        telemetry.setMsTransmissionInterval(50);
        limelight.pipelineSwitch(HiveAssistConfig.LIMELIGHT_APRILTAG_PIPELINE);
        limelight.start();

        telemetry.addLine("Robot ready — press PLAY");
        telemetry.update();
        waitForStart();

        try {
            while (opModeIsActive()) {
                LLResult result = limelight.getLatestResult();
                if (result == null || !result.isValid()) {
                    telemetry.addLine("No valid Limelight result");
                } else {
                    for (LLResultTypes.FiducialResult tag : result.getFiducialResults()) {
                        showMeasurement(shootingRange, shootingRange.measure(tag));
                    }
                }
                telemetry.update();
            }
        } finally {
            limelight.stop();
        }
    }

    private void showMeasurement(LimelightShootingRange calculator,
                                 LimelightShootingRange.Measurement range) {
        telemetry.addData("AprilTag ID", range.tagId());
        telemetry.addData("Camera position", "x %.3f, y %.3f, z %.3f m",
                range.xMeters(), range.yMeters(), range.zMeters());
        telemetry.addData("Camera -> tag", "%.3f m", range.distanceMeters());
        telemetry.addData("Angles", "tx %.2f°, ty %.2f°",
                range.txDegrees(), range.tyDegrees());

        if (!HiveAssistConfig.SHOOTING_RANGE_LIMITS_VERIFIED) {
            telemetry.addData("Shooting", "UNCALIBRATED — tune HiveAssistConfig");
        } else {
            telemetry.addData("Shooting", calculator.isInVerifiedShootingWindow(range)
                    ? "READY TO SHOOT" : "MOVE ROBOT");
        }
    }
}
