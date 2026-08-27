package org.firstinspires.ftc.teamcode.test;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Esther's Limelight Test Teleop")

public class LimelightTestTeleopEsther extends LinearOpMode {

    private Limelight3ASensor limelightSensor = new Limelight3ASensor();

    @Override
    public void runOpMode() throws InterruptedException {
        initHardware();
        waitForStart();
        while (opModeIsActive()) {
            limelightSensor.limelightProcessing(telemetry);

            // Share the CPU.
            sleep(20);
        }
        limelightSensor.stopLimelightProcessing();
    }

    public void initHardware() {
        limelightSensor.initLimelight(hardwareMap, telemetry);
    }


}
