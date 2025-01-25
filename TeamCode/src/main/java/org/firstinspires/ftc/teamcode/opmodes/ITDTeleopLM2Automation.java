package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;


@TeleOp(name = "Teleop LM3 WITH AUTOMATION")
public class ITDTeleopLM2Automation extends LinearOpMode {

    /* Declare OpMode members. */
    public DcMotor  leftFront   = null;
    public DcMotor  rightFront  = null;
    public DcMotor  rightBack  = null;
    public DcMotor  leftBack  = null;

    public DcMotor lift;
    public CRServo claw;
    public DcMotor liftPivot;
    public CRServo claw2;


    @Override
    public void runOpMode() {

        // Define and Initialize Motors
        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
        rightBack = hardwareMap.get(DcMotor.class, "backRight");
        leftBack = hardwareMap.get(DcMotor.class, "backLeft");

        rightFront.setDirection(DcMotorSimple.Direction.REVERSE);
        rightBack.setDirection(DcMotorSimple.Direction.REVERSE);

        //RNRRMecanumDrive drive = new RNRRMecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        //drive.setPoseEstimate(startPose);

        lift = hardwareMap.get(DcMotor.class, "lift");
        liftPivot = hardwareMap.get(DcMotor.class, "liftPivot");
        claw = hardwareMap.get(CRServo.class, "claw");
        claw2 = hardwareMap.get(CRServo.class, "claw2");

        liftPivot.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        liftPivot.setDirection(DcMotorSimple.Direction.REVERSE);

        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips


        // Send telemetry message to signify robot waiting;
        telemetry.addData(">", "Robot Ready.  Press START.");    //
        telemetry.update();

        // Wait for the game to start (driver presses START)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x;

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio,
            // but only if at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;

            leftFront.setPower(0.8*frontLeftPower);
            leftBack.setPower(0.8*backLeftPower);
            rightFront.setPower(0.8*frontRightPower);
            rightBack.setPower(0.8*backRightPower);

            lift.setPower(-gamepad2.left_stick_y);
            liftPivot.setPower(0.7*gamepad2.right_stick_y);

            telemetry.addData("Lift encoder ticks: ", lift.getCurrentPosition());
            telemetry.update();
            if (gamepad2.a) {
                claw.setPower(1);
                claw2.setPower(-1);
            }
            else if (gamepad2.x) {
                claw.setPower(-1);
                claw2.setPower(1);
            }
            // outtake slowly to slowly let out a specimen so the hook is exposed
            else if (gamepad2.b) {
                claw.setPower(-0.35);
                claw2.setPower(0.35);
            }
            else {
                claw.setPower(0);
                claw2.setPower(0);
            }

            // at the beginning of teleop, reset encoders to 0 (lift and liftpivot have to be all teh way down
            if (gamepad2.right_stick_button) {
                lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                liftPivot.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            }
            // lift up to high basket
            if (gamepad2.dpad_up) {
                //lift.setTargetPosition(2600); // FIXME change encoder value
                // lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                liftPivot.setPower(1);

                liftPivot.setTargetPosition(1710);
                liftPivot.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            }
            //
            else if (gamepad2.dpad_down) {
                lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                liftPivot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }

            // alternative to dpad down: if there is joystick input, override the run to position mode
            if (gamepad2.left_stick_x > 0 || gamepad2.left_stick_y > 0 || gamepad2.right_stick_x > 0 || gamepad2.right_stick_y > 0) {
                lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                liftPivot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
            // if the lift and lift pivot are all the way in and we want to go out into the submersible to get a sample, lift the pivot slightly so the intake doesn't get stuck and then extend
            if (gamepad2.dpad_left && liftPivot.getCurrentPosition() < 50 && lift.getCurrentPosition() < 30) {
                liftPivot.setPower(-0.9);
                liftPivot.setTargetPosition(150);
                liftPivot.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            }

            // Pace this loop so jaw action is reasonable speed.
            sleep(50);
        }
    }
}