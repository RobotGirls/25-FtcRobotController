package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;

import java.io.IOException;

/** Camera-only OpMode for FieldViewer video and wireless pipeline editing. */
@TeleOp(name = "Limelight FieldViewer Stream", group = "Sensor")
public final class LimelightFieldViewerStreamTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() {
        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");
        LimelightFieldViewerRelay relay = new LimelightFieldViewerRelay(
                limelight, HiveAssistConfig.LIMELIGHT_APRILTAG_PIPELINE);

        limelight.pipelineSwitch(HiveAssistConfig.LIMELIGHT_APRILTAG_PIPELINE);
        limelight.start();
        try {
            relay.start();
        } catch (IOException error) {
            limelight.stop();
            telemetry.addData("FieldViewer relay", error.getMessage());
            telemetry.update();
            return;
        }

        telemetry.addLine("Camera only - no motors or servos initialized");
        telemetry.addData("FieldViewer relay", "port %d", HiveAssistConfig.LIMELIGHT_RELAY_PORT);
        telemetry.addLine("Pipeline editor available inside FieldViewer");
        telemetry.update();
        waitForStart();

        try {
            while (opModeIsActive()) {
                telemetry.addData("FieldViewer relay", relay.isRunning() ? "online" : "offline");
                telemetry.addData("Limelight pipeline", relay.selectedPipeline());
                telemetry.update();
                sleep(100);
            }
        } finally {
            relay.stop();
            limelight.stop();
        }
    }
}
