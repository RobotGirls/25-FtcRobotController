//package org.firstinspires.ftc.teamcode.opmodes;
//
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.CRServo;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//import com.qualcomm.robotcore.hardware.Servo;
//
//
//@TeleOp(name = "Teleop State Machine")
//public class ITDTeleopStateMachine extends LinearOpMode {
//
//    /* Declare OpMode members. */
//    public DcMotor  leftFront   = null;
//    public DcMotor  rightFront  = null;
//    public DcMotor  rightBack  = null;
//    public DcMotor  leftBack  = null;
//
//    public DcMotor lift;
//    public CRServo claw;
//    public DcMotor liftPivot;
//    public DcMotor liftPivot2;
//    public CRServo claw2;
//    public Servo wrist;
//    public DcMotor lift2;
//
//    private enum State {
//        IDLE,
//        PIVOT_TO_BASKET,
//        LIFT_TO_BASKET,
//        INTAKE,
//        OUTTAKE,
//        LIFT_TO_INTAKE,
//        MANUAL_CONTROL,
//        PIVOT_TO_CHAMBER,
//        OUTTAKE_SPECIMEN,
//        PIVOT_TO_INTAKE,
//        WRIST_TO_INTAKE
//    }
//
//    private State currentState = State.IDLE;
//
//
//    @Override
//    public void runOpMode() {
//
//        // Define and Initialize Motors
//        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
//        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
//        rightBack = hardwareMap.get(DcMotor.class, "backRight");
//        leftBack = hardwareMap.get(DcMotor.class, "backLeft");
//
//        rightFront.setDirection(DcMotorSimple.Direction.REVERSE);
//        rightBack.setDirection(DcMotorSimple.Direction.REVERSE);
//
//        lift = hardwareMap.get(DcMotor.class, "lift");
//        lift2 = hardwareMap.get(DcMotor.class, "lift2");
//        liftPivot = hardwareMap.get(DcMotor.class, "liftPivot");
//        liftPivot2 = hardwareMap.get(DcMotor.class, "liftPivot2");
//        claw = hardwareMap.get(CRServo.class, "claw");
//        claw2 = hardwareMap.get(CRServo.class, "claw2");
//        wrist = hardwareMap.servo.get("wrist");
//
//
//        liftPivot.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        liftPivot2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//
//        liftPivot.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//        liftPivot2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//
//        liftPivot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        liftPivot2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//
//        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        lift2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//
//        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//        lift2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//
//        lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        lift2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//
//        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//
//        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
//        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
//        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
//
//        // Send telemetry message to signify robot waiting;
//        telemetry.addData(">", "Robot Ready.  Press START.");    //
//        telemetry.update();
//
//
//        // Wait for the game to start (driver presses START)
//        waitForStart();
//
//        // run until the end of the match (driver presses STOP)
//        while (opModeIsActive()) {
//            double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
//            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
//            double rx = gamepad1.right_stick_x;
//
//            // Denominator is the largest motor power (absolute value) or 1
//            // This ensures all the powers maintain the same ratio,
//            // but only if at least one is out of the range [-1, 1]
//            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
//            double frontLeftPower = (y + x + rx) / denominator;
//            double backLeftPower = (y - x + rx) / denominator;
//            double frontRightPower = (y - x - rx) / denominator;
//            double backRightPower = (y + x - rx) / denominator;
//
//            leftFront.setPower(0.8 * frontLeftPower);
//            leftBack.setPower(0.8 * backLeftPower);
//            rightFront.setPower(0.8 * frontRightPower);
//            rightBack.setPower(0.8 * backRightPower);
//
//            // Check for joystick input to override automations
//            if (Math.abs(gamepad2.left_stick_y) > 0.1 || Math.abs(gamepad2.right_stick_y) > 0.1 || gamepad2.left_bumper || gamepad2.right_bumper) {
//                currentState = State.MANUAL_CONTROL;
//            }
//
//            // State machine logic
//            switch (currentState) {
//                case IDLE:
//                    // Check for button presses to start automations
//                    if (gamepad2.a) {
//                        // score in basket
//                        currentState = State.PIVOT_TO_BASKET;
//                    } else if (gamepad2.b) {
//                        // intake from sub
//                        currentState = State.PIVOT_TO_CHAMBER;
//                    }
//                    else if (gamepad2.x) {
//                        currentState = State.PIVOT_TO_INTAKE;
//                    }
//                    break;
//
//                case PIVOT_TO_BASKET:
//                    liftPivot.setPower(-1);
//                    liftPivot2.setPower(1);
//                    if (liftPivot.getCurrentPosition() <= -2295) {
//                        liftPivot.setPower(0);
//                        liftPivot2.setPower(0);
//                        currentState = State.LIFT_TO_BASKET;
//                    }
//                    break;
//
//                case LIFT_TO_BASKET:
//                    lift.setPower(1);
//                    lift2.setPower(1);
//                    if (lift.getCurrentPosition() >= 3000) {
//                        lift.setPower(0);
//                        lift2.setPower(0);
//                        currentState = State.OUTTAKE;
//                    }
//                    break;
//
//                case INTAKE:
//                    wrist.setPosition(0.5);
//                    claw.setPower(1);
//                    claw2.setPower(-1);
//                    if (!gamepad2.b) { // Stop automation when button is released
//                        currentState = State.IDLE;
//                    }
//                    break;
//
//                case OUTTAKE:
//                    wrist.setPosition(0.86);
//                    sleep(1000);
//
//                    break;
//
//                case PIVOT_TO_CHAMBER:
//                    liftPivot.setPower(-1);
//                    liftPivot2.setPower(1);
//                    if (liftPivot.getCurrentPosition() <= -1500) {
//                        liftPivot.setPower(0);
//                        liftPivot2.setPower(0);
//                        currentState = State.OUTTAKE_SPECIMEN;
//                    }
//                    break;
//
//                case OUTTAKE_SPECIMEN:
//                    wrist.setPosition(0.86);
//
//                    if (!gamepad2.b) { // Stop automation when button is released
//                        currentState = State.IDLE;
//                    }
//                    break;
//
//                case LIFT_TO_INTAKE:
//                    lift.setPower(1);
//                    lift2.setPower(1);
//                    if (lift.getCurrentPosition() >= 1400) {
//
//                        lift.setPower(0);
//                        lift2.setPower(0);
//                        currentState = State.WRIST_TO_INTAKE;
//                    }
//                    break;
//
//                case PIVOT_TO_INTAKE:
//                    liftPivot.setPower(-1);
//                    liftPivot2.setPower(1);
//                    if (liftPivot.getCurrentPosition() <= -500) {
//                        liftPivot.setPower(0);
//                        liftPivot2.setPower(0);
//                        currentState = State.LIFT_TO_INTAKE;
//                    }
//                    break;
//
//                case WRIST_TO_INTAKE:
//                    wrist.setPosition(0.652);
//                    if (!gamepad2.x) { // Stop automation when button is released
//                        currentState = State.IDLE;
//                    }
//                    break;
//
//
//                case MANUAL_CONTROL:
//                    // Manual control with joysticks
//                    liftPivot.setPower(gamepad2.right_stick_y);
//                    liftPivot2.setPower(-gamepad2.right_stick_y);
//                    lift.setPower(-gamepad2.left_stick_y);
//                    lift2.setPower(-gamepad2.left_stick_y);
//
//                    if (gamepad2.left_bumper) {
//                        claw.setPower(1);
//                        claw2.setPower(-1);
//                    }
//                    else if (gamepad2.right_bumper) {
//                        claw.setPower(-1);
//                        claw2.setPower(1);
//                    }
//                    else {
//                        claw.setPower(0);
//                        claw2.setPower(0);
//                    }
//
//                    if (gamepad2.dpad_right) {
//                        wrist.setPosition(0.86);
//                    }
//                    else if (gamepad2.dpad_up) {
//                        wrist.setPosition(0.99);
//                    }
//                    else if (gamepad2.dpad_left) {
//                        // new position for lower wheels
//                        wrist.setPosition(0.77);
//                    }
//                    else if (gamepad2.dpad_down) {
//                        wrist.setPosition(0.68);
//                    }
//
//                    // Check if automations should resume
//                    if (gamepad2.a || gamepad2.b || gamepad2.x) {
//                        currentState = State.IDLE;
//                    }
//                    break;
//            }
//
//            // Telemetry for debugging
//            telemetry.addData("State", currentState);
//            telemetry.addData("Pivot Position", liftPivot.getCurrentPosition());
//            telemetry.addData("Lift Position", lift.getCurrentPosition());
//            telemetry.update();
//
//
//        }
//    }
//}