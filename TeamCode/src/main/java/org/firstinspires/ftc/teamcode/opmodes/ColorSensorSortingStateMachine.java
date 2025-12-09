package org.firstinspires.ftc.teamcode.opmodes;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.test.HSVColorSensor;

@TeleOp(name = "LM2 Color Sorting")

public class ColorSensorSortingStateMachine extends LinearOpMode {

    HSVColorSensor colorSensor1 = new HSVColorSensor();
    HSVColorSensor colorSensor2 = new HSVColorSensor();
    HSVColorSensor colorSensor3 = new HSVColorSensor();

    HSVColorSensor.DetectedColor artifactColor1 = HSVColorSensor.DetectedColor.UNKNOWN;
    HSVColorSensor.DetectedColor artifactColor2 = HSVColorSensor.DetectedColor.UNKNOWN;
    HSVColorSensor.DetectedColor artifactColor3 = HSVColorSensor.DetectedColor.UNKNOWN;

    private DcMotor  leftFront   = null;
    private DcMotor  rightFront  = null;
    private DcMotor  rightBack  = null;
    private DcMotor  leftBack  = null;
    private Servo ballServo1;
    private Servo ballServo2;
    private Servo ballServo3;

    public DcMotor shooter;
    public DcMotor transfer;
    public DcMotor intake;
    public enum FlywheelState {
        ON,
        OFF
    }
    public enum ColorSensingState {
        SEARCHING_FOR_PURPLE,
        SEARCHING_FOR_GREEN,
        SEARCHING_FOR_ANY_COLOR,
        IDLE
    }

    public enum MechState {
        INTAKE_IN,
        INTAKE_OUT,
        FLYWHEEL_LONG,
        FLYWHEEL_SHORT,
        FLYWHEEL_OUTTAKE,
        COLOR_PURPLE,
        COLOR_GREEN,
        COLOR_ANY,
        IDLE
    }

    ColorSensingState currentState = ColorSensingState.IDLE;


    MechState currentMechState = MechState.IDLE;

    private static final double SERVO_UP = 0.8;
    private static final double SERVO_DOWN = 0.0;

    FlywheelState flywheelState = FlywheelState.OFF;
    private HSVColorSensor.DetectedColor desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;

    @Override
    public void runOpMode() throws InterruptedException {

        initHardware();

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            drivingControls();
            mechanismControls();

            // Pace this loop so jaw action is reasonable speed.
            sleep(50);

            colorSensing();

        }
    }

    public void initHardware() {

        ballServo1 = hardwareMap.get(Servo.class, "servo1");
        ballServo2 = hardwareMap.get(Servo.class,"servo2");
        ballServo3 = hardwareMap.get(Servo.class,"servo3");

        ballServo1.setPosition(SERVO_DOWN);
        ballServo2.setPosition(SERVO_DOWN);
        ballServo3.setPosition(SERVO_DOWN);

        colorSensor1.initColorSensorName(hardwareMap,"colorSensor1");
        colorSensor2.initColorSensorName(hardwareMap,"colorSensor2");
        colorSensor3.initColorSensorName(hardwareMap,"colorSensor3");

        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
        rightBack = hardwareMap.get(DcMotor.class, "backRight");
        leftBack = hardwareMap.get(DcMotor.class, "backLeft");

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);

    }

    public void drivingControls() {
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

        leftFront.setPower(frontLeftPower);
        leftBack.setPower(backLeftPower);
        rightFront.setPower(frontRightPower);
        rightBack.setPower(backRightPower);
    }

    public void mechanismControls() {
        switch (currentMechState) {
            case IDLE:
                intake.setPower(0);
                transfer.setPower(0);
                shooter.setPower(0);
                if (gamepad2.left_bumper) {
                    currentMechState = MechState.INTAKE_IN;
                } else if (gamepad2.right_bumper) {
                    currentMechState = MechState.INTAKE_OUT;
                } else if (gamepad2.x) {
                    currentMechState = MechState.FLYWHEEL_LONG;
                } else if (gamepad2.b) {
                    currentMechState = MechState.FLYWHEEL_SHORT;
                } else if (gamepad2.a) {
                    currentMechState = MechState.FLYWHEEL_OUTTAKE;
                } else if (gamepad2.dpad_left) {
                    currentMechState = MechState.COLOR_PURPLE;
                } else if (gamepad2.dpad_right) {
                    currentMechState = MechState.COLOR_GREEN;
                } else if (gamepad2.dpad_up) {
                    currentMechState = MechState.COLOR_ANY;
                }
                break;
            case INTAKE_IN:
                intake.setPower(1);
                transfer.setPower(1);
                currentMechState = MechState.IDLE;
            case INTAKE_OUT:
                intake.setPower(-1);
                transfer.setPower(-1);
                currentMechState = MechState.IDLE;
            case FLYWHEEL_LONG:
                shooter.setPower(-0.72);
                flywheelState = ColorSensorSortingStateMachine.FlywheelState.ON;
                currentMechState = MechState.IDLE;
            case FLYWHEEL_SHORT:
                shooter.setPower(-0.44);
                flywheelState = ColorSensorSortingStateMachine.FlywheelState.ON;
                currentMechState = MechState.IDLE;
            case FLYWHEEL_OUTTAKE:
                shooter.setPower(0.5);
                currentMechState = MechState.IDLE;
            case COLOR_PURPLE:
                desiredColor = HSVColorSensor.DetectedColor.PURPLE;
                currentState = ColorSensingState.SEARCHING_FOR_PURPLE;
                currentMechState = MechState.IDLE;
            case COLOR_GREEN:
                desiredColor = HSVColorSensor.DetectedColor.GREEN;
                currentState = ColorSensingState.SEARCHING_FOR_GREEN;
                currentMechState = MechState.IDLE;
            case COLOR_ANY:
                desiredColor = HSVColorSensor.DetectedColor.PURPLE_OR_GREEN;
                currentState = ColorSensingState.SEARCHING_FOR_ANY_COLOR;
                currentMechState = MechState.IDLE;
        }

    }

    public void colorSensing() {
        switch (currentState) {
            case IDLE:
                colorSensor1.getDetectedColor(telemetry);
                artifactColor1 = colorSensor1.getCurrentDetectedColor();
                colorSensor2.getDetectedColor(telemetry);
                artifactColor2 = colorSensor2.getCurrentDetectedColor();
                colorSensor3.getDetectedColor(telemetry);
                artifactColor3 = colorSensor3.getCurrentDetectedColor();
                break;
            case SEARCHING_FOR_PURPLE:
                if (artifactColor1 == HSVColorSensor.DetectedColor.PURPLE) {
                        ballServo1.setPosition(SERVO_UP);
                        sleep(1000);
                        desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                        ballServo1.setPosition(SERVO_DOWN);
                    } else if (artifactColor2 == HSVColorSensor.DetectedColor.PURPLE) {
                        ballServo2.setPosition(SERVO_UP);
                        sleep(1000);
                        desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                        ballServo2.setPosition(SERVO_DOWN);
                    } else if (artifactColor3 == HSVColorSensor.DetectedColor.PURPLE) {
                        ballServo3.setPosition(SERVO_UP);
                        sleep(1000);
                        desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                        ballServo3.setPosition(SERVO_DOWN);
                    }
                    break;

            case SEARCHING_FOR_GREEN:
                if (artifactColor1 == HSVColorSensor.DetectedColor.GREEN) {
                    ballServo1.setPosition(SERVO_UP);
                    sleep(1000);
                    desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                    ballServo1.setPosition(SERVO_DOWN);
                } else if (artifactColor2 == HSVColorSensor.DetectedColor.GREEN) {
                    ballServo2.setPosition(SERVO_UP);
                    sleep(1000);
                    desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                    ballServo2.setPosition(SERVO_DOWN);
                } else if (artifactColor3 == HSVColorSensor.DetectedColor.GREEN) {
                    ballServo3.setPosition(SERVO_UP);
                    sleep(1000);
                    desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                    ballServo3.setPosition(SERVO_DOWN);
                }
                break;

            case SEARCHING_FOR_ANY_COLOR:
                if ((artifactColor1 == HSVColorSensor.DetectedColor.GREEN)
                        || (artifactColor1 == HSVColorSensor.DetectedColor.PURPLE)) {
                    ballServo1.setPosition(SERVO_UP);
                    sleep(1000);
                    desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                    ballServo1.setPosition(SERVO_DOWN);
                } else if ((artifactColor2 == HSVColorSensor.DetectedColor.GREEN)
                        || (artifactColor2 == HSVColorSensor.DetectedColor.PURPLE)) {
                    ballServo2.setPosition(SERVO_UP);
                    sleep(1000);
                    desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                    ballServo2.setPosition(SERVO_DOWN);
                } else if ((artifactColor3 == HSVColorSensor.DetectedColor.GREEN)
                        || (artifactColor3 == HSVColorSensor.DetectedColor.PURPLE)){
                    ballServo3.setPosition(SERVO_UP);
                    sleep(1000);
                    desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                    ballServo3.setPosition(SERVO_DOWN);
                }
                break;

        }
    }
}