package org.firstinspires.ftc.teamcode.opmodes;


import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.test.HSVColorSensor;

@TeleOp(name = "Color Sensor Sorting Teleop")

public class ColorSensorSortingTeleop extends LinearOpMode {

    HSVColorSensor colorSensor1 = new HSVColorSensor();
    HSVColorSensor colorSensor2 = new HSVColorSensor();
    HSVColorSensor colorSensor3 = new HSVColorSensor();

    private HSVColorSensor.DetectedColor artifactColor1 = HSVColorSensor.DetectedColor.UNKNOWN;
    private HSVColorSensor.DetectedColor artifactColor2 = HSVColorSensor.DetectedColor.UNKNOWN;
    private HSVColorSensor.DetectedColor artifactColor3 = HSVColorSensor.DetectedColor.UNKNOWN;

    public DcMotor  leftFront   = null;
    public DcMotor  rightFront  = null;
    public DcMotor  rightBack  = null;
    public DcMotor  leftBack  = null;
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

            //drivingControls();
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

        if (gamepad2.dpad_left) {
            desiredColor = HSVColorSensor.DetectedColor.PURPLE;
        } else if (gamepad2.dpad_right) {
            desiredColor = HSVColorSensor.DetectedColor.GREEN;
        } else if (gamepad2.dpad_up) {
            desiredColor = HSVColorSensor.DetectedColor.PURPLE_OR_GREEN;
        }
    }

    public void colorSensing() {
        colorSensor1.getDetectedColor(telemetry);
        artifactColor1 = colorSensor1.getCurrentDetectedColor();
        colorSensor2.getDetectedColor(telemetry);
        artifactColor2 = colorSensor2.getCurrentDetectedColor();
        colorSensor3.getDetectedColor(telemetry);
        artifactColor3 = colorSensor3.getCurrentDetectedColor();

        if (desiredColor == HSVColorSensor.DetectedColor.PURPLE_OR_GREEN) {
            if ((artifactColor1 == HSVColorSensor.DetectedColor.GREEN) ||
                    (artifactColor1 == HSVColorSensor.DetectedColor.PURPLE)) {
                ballServo1.setPosition(SERVO_UP);
                sleep(1000);
                desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                ballServo1.setPosition(SERVO_DOWN);
            } else if ((artifactColor2 == HSVColorSensor.DetectedColor.GREEN) ||
                    (artifactColor2 == HSVColorSensor.DetectedColor.PURPLE)) {
                ballServo2.setPosition(SERVO_UP);
                sleep(1000);
                desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                ballServo2.setPosition(SERVO_DOWN);
            } else if ((artifactColor3 == HSVColorSensor.DetectedColor.GREEN) ||
                    (artifactColor3 == HSVColorSensor.DetectedColor.PURPLE)) {
                ballServo3.setPosition(SERVO_UP);
                sleep(1000);
                desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                ballServo3.setPosition(SERVO_DOWN);
            }

        } else if (desiredColor != HSVColorSensor.DetectedColor.UNKNOWN) {
                if (artifactColor1 == desiredColor) {
                    ballServo1.setPosition(SERVO_UP);
                    sleep(1000);
                    ballServo1.setPosition(SERVO_DOWN);
                    desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                } else if (artifactColor2 == desiredColor) {
                    ballServo2.setPosition(SERVO_UP);
                    sleep(1000);
                    ballServo2.setPosition(SERVO_DOWN);
                    desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                } else if (artifactColor3 == desiredColor) {
                    ballServo3.setPosition(SERVO_UP);
                    sleep(1000);
                    ballServo3.setPosition(SERVO_DOWN);
                    desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                }
            }
        }
}