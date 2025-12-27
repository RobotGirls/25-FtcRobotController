package org.firstinspires.ftc.teamcode.opmodes;



import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.MecanumDrive;


@TeleOp(name = "DECODE TELEOP")
public class DECODETeleopTankDrive extends LinearOpMode {

    /* Declare OpMode members. */
    public DcMotor leftFront   = null;
    public DcMotor  rightFront  = null;
    public DcMotor  rightBack  = null;
    public DcMotor  leftBack  = null;

    public DcMotorEx shooter;
    public DcMotor transfer;
    public DcMotor intake;

    private Limelight3A limelight;
    private DcMotor turret;
    private final int ALIGN_THRESHOLD = 3;
    private double lastError = 0;
    private double derivative;
    private double integralSum = 0;

    private double Kp = 0.014; // Tx range is 0 to 26 --> at max offset 26, when Kp is 0.02, speed is half power
    private double Ki = 0;
    private double Kd = 0;


    public enum FlywheelState {
        ON,
        OFF
    }
    FlywheelState flywheelState = FlywheelState.OFF;


    @Override
    public void runOpMode() {

        // Define and Initialize Motors
        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
        rightBack = hardwareMap.get(DcMotor.class, "backRight");
        leftBack = hardwareMap.get(DcMotor.class, "backLeft");

        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);


        //turret = hardwareMap.get(DcMotor.class, "turret");
        //limelight = hardwareMap.get(Limelight3A.class, "limelight");

        //telemetry.setMsTransmissionInterval(11);

       // limelight.pipelineSwitch(0);

      //  limelight.start();

        //telemetry.addData(">", "Robot Ready.  Press Play.");
        //telemetry.update();
        //waitForStart();


        //RNRRMecanumDrive drive = new RNRRMecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        //drive.setPoseEstimate(startPose);


        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // defaults: p 10, i 3, d 0, f 0

        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips


        // Send telemetry message to signify robot waiting;
        telemetry.addData(">", "Robot Ready.  Press START.");
        telemetry.update();

        // Wait for the game to start (driver presses START)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            leftFront.setPower(gamepad1.left_stick_y);
            leftBack.setPower(gamepad1.left_stick_y);
            rightFront.setPower(gamepad1.right_stick_y);
            rightBack.setPower(gamepad1.right_stick_y);



            //LLResult result = limelight.getLatestResult();
/*
            if (result.isValid()) {

                // Access general information
                Pose3D botpose = result.getBotpose();
                double captureLatency = result.getCaptureLatency();
                double targetingLatency = result.getTargetingLatency();
                double parseLatency = result.getParseLatency();

                telemetry.addData("tx", result.getTx());
                telemetry.addData("txnc", result.getTxNC());
                telemetry.addData("ty", result.getTy());
                telemetry.addData("tync", result.getTyNC());

                telemetry.addData("Botpose", botpose.toString());

                double error = result.getTx();
                ElapsedTime timer = new ElapsedTime();
                if (Math.abs(error) > ALIGN_THRESHOLD) {
                    error = -1 * result.getTx();
                    derivative = (error - lastError) / timer.seconds();
                    integralSum = integralSum + (error * timer.seconds());
                    double power = (Kp * error) + (Ki * integralSum) + (Kd * derivative);
                    turret.setPower(power);
                    lastError = error;
                } else {
                    turret.setPower(0);  // aligned
                }


            } else {
                // apriltag not in view of limelight
                turret.setPower(gamepad1.right_stick_x);
                telemetry.addData("Limelight", "No data available");
            }

            telemetry.update();
        }
        limelight.stop();

 */
            // Pace this loop so jaw action is reasonable speed.
            sleep(50);
        }
    }
}