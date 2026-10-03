package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.biobuzz2627.teleop.BIOBUZZTeleopLM0Blue;

@TeleOp(name = "ShootingRangeTeleop")
public class ShootingRangeTeleop extends LinearOpMode {
    private Limelight3A limelight;

    public DcMotor shooter;

    private final int ALIGN_THRESHOLD = 3;
    private double lastError = 0;

    // Update these values after tuning them
    private double derivative;
    private double integralSum = 0;

    private double Kp = 0.014; // Tx range is 0 to 26 --> at max offset 26, when Kp is 0.02, speed is half power
    private double Ki = 0;
    private double Kd = 0;

    public enum FlywheelState {
        ON,
        OFF
    }

    ShootingRangeTeleop.FlywheelState flywheelState = ShootingRangeTeleop.FlywheelState.OFF;

    @Override
    public void runOpMode() {


        shooter = hardwareMap.get(DcMotor.class, "shooter");

        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        telemetry.setMsTransmissionInterval(11);

        // Pipeline 0: blue
        limelight.pipelineSwitch(0);

        limelight.start();

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            if (gamepad2.x) {
                shooter.setPower(-0.72);

            } else {
                shooter.setPower(0);
            }

            LLResult result = limelight.getLatestResult();

            if (result.isValid()) {

                // Access general information
                Pose3D botpose = result.getBotpose();

                LimelightHelpers.PoseEstimate mt1 = LimelightHelpers.getBotPoseEstimate_wpiBlue("limelight");


                telemetry.addData("tx", result.getTx());
                telemetry.addData("ty", result.getTy());
               // telemetry.addData("tz", botpose.);

                // telemetry.addData("Horizontal distance",)

            } else {
                // apriltag not in view of limelight
                telemetry.addData("Limelight", "No data available");
            }


        }


    }
}
