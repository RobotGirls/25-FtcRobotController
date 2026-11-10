package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.biobuzz2627.mechCode.LimelightPurePursuit;

/**
 * Camera-only OpMode for viewing the Limelight stream in FieldViewer.
 *
 * This OpMode initializes only the configured "limelight" device. It does not look up or command
 * any motors or servos. The video relay starts during INIT and remains active until STOP.
 */
@TeleOp(name = "Limelight FieldViewer Stream", group = "Testing")
public class LimelightFieldViewerStream extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        LimelightPurePursuit limelight = new LimelightPurePursuit();

        try {
            // Pipeline 0 is sufficient for the camera feed; targeting results are not used here.
            limelight.initLimelightBlue(hardwareMap, telemetry);
            telemetry.addLine("Camera-only mode: no motors or servos are initialized.");
            telemetry.addLine("Connect FieldViewer while this OpMode is in INIT or RUNNING.");
            telemetry.update();

            waitForStart();

            while (opModeIsActive()) {
                sleep(100);
            }
        } finally {
            limelight.stop();
        }
    }
}
