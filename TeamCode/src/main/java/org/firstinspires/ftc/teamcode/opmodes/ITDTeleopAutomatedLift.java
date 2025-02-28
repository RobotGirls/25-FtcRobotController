package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;


@TeleOp(name = "Teleop ILT Automated Lift")
public class ITDTeleopAutomatedLift extends LinearOpMode {

    /* Declare OpMode members. */
    public DcMotor  leftFront   = null;
    public DcMotor  rightFront  = null;
    public DcMotor  rightBack  = null;
    public DcMotor  leftBack  = null;

    public DcMotor lift;
    public CRServo claw;
    public DcMotor liftPivot;
    public DcMotor liftPivot2;
    public CRServo claw2;
    public Servo wrist;
    public DcMotor lift2;


    @Override
    public void runOpMode() {

        // Define and Initialize Motors
        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
        rightBack = hardwareMap.get(DcMotor.class, "backRight");
        leftBack = hardwareMap.get(DcMotor.class, "backLeft");

        rightFront.setDirection(DcMotorSimple.Direction.REVERSE);
        rightBack.setDirection(DcMotorSimple.Direction.REVERSE);

        lift = hardwareMap.get(DcMotor.class, "lift");
        lift2 = hardwareMap.get(DcMotor.class, "lift2");
        liftPivot = hardwareMap.get(DcMotor.class, "liftPivot");
        liftPivot2 = hardwareMap.get(DcMotor.class, "liftPivot2");
        claw = hardwareMap.get(CRServo.class, "claw");
        claw2 = hardwareMap.get(CRServo.class, "claw2");
        wrist = hardwareMap.servo.get("wrist");

        liftPivot.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        liftPivot2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lift2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

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

        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        liftPivot.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        wrist.setPosition(0.99);

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

            liftPivot.setPower(gamepad2.right_stick_y);
            liftPivot2.setPower(-gamepad2.right_stick_y);

            if (gamepad2.a) {
                claw.setPower(1);
                claw2.setPower(-1);
            }
            else if (gamepad2.x) {
                claw.setPower(-1);
                claw2.setPower(1);
            }
            else {
                claw.setPower(0);
                claw2.setPower(0);
            }

            if (gamepad2.y) {
                wrist.setPosition(0.65);
            }
            else if (gamepad2.b) {
                // UP POSITION (init position)
                wrist.setPosition(0.99);
            }
            else if (gamepad2.dpad_right) {
                wrist.setPosition(0.86);
            }

            if (gamepad2.dpad_up) {
                // up to basket
                setPivotPosition(500);
                setLiftPosition(2000);
            }
            else if (gamepad2.dpad_down) {
                setLiftPosition(500);
            }
            else if (gamepad2.dpad_left) {
                setLiftPosition(1000);
            }
            else if (Math.abs(gamepad2.left_stick_y) > 0.05 || Math.abs(gamepad2.right_stick_y) > 0.05) {
                lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                lift.setPower(-gamepad2.left_stick_y);
                lift2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                lift2.setPower(-gamepad2.left_stick_y);
                liftPivot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                liftPivot.setPower(-gamepad2.right_stick_y);
                liftPivot2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                liftPivot2.setPower(gamepad2.right_stick_y);
            }

            else if (!lift.isBusy() && !lift2.isBusy()) {
                lift.setPower(0);
                lift2.setPower(0);
            }
            else if (!liftPivot.isBusy() && !liftPivot2.isBusy()) {
                liftPivot.setPower(0);
                liftPivot2.setPower(0);
            }


            // Pace this loop so jaw action is reasonable speed.
            sleep(50);
        }
    }

    public void setLiftPosition(int encoderVal) {
        lift.setTargetPosition(encoderVal);
        lift2.setTargetPosition(encoderVal);
        lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lift2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lift.setPower(1);
        lift2.setPower(1);
    }

    public void setPivotPosition(int encoderVal) {
        liftPivot.setTargetPosition(encoderVal);
        liftPivot.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        liftPivot.setPower(1);

        liftPivot2.setTargetPosition(encoderVal);
        liftPivot2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        liftPivot2.setPower(1);
    }

}