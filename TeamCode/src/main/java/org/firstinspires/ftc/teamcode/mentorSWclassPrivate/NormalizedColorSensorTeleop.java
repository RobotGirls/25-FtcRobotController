package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;

// I2C port ??  colorSensor
// Sero Port ??: servoOne
// https://www.revrobotics.com/rev-31-1557/
@TeleOp(name = "Teleop ColorSensor")

// @Disabled
public class NormalizedColorSensorTeleop extends LinearOpMode {
    private NormalizedColorSensor colorSensor;
    private float normRed;
    private float normGreen;
    private float normBlue;
    private double targetValue = 1000;

    private Servo servoOne;
    private double servoOneInitPosition = 0.5;
    private double servoOnePositionOne = 0.0;
    private double servoOnePositionTwo = 1.0;

    public enum DetectedColor {
        PURPLE,
        GREEN,
        UNKNOWN
    }
    private DetectedColor currentDetectedColor = DetectedColor.UNKNOWN;
    private DetectedColor latchedDetectedColor = DetectedColor.UNKNOWN;

    @Override
    public void runOpMode() {
        initColorSensor();
        initHardware();
            while (!isStarted()) {
                getDetectedColor();
                colorTelemetry();

            }

            // wait for PLAY button to be pushed
            waitForStart();
            while (opModeIsActive()) {
                getDetectedColor();
                colorTelemetry();
                teleopControls();
            }

    } // end runOpMode

    public void initHardware() {
        //initServoOne();
        initColorSensor();
    }

    public void initServoOne() {
        servoOne = hardwareMap.get(Servo.class, "servoOne");
        servoOne.setDirection(Servo.Direction.FORWARD);
        servoOne.setPosition(servoOneInitPosition);
    }

    public void initColorSensor() {
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "colorSensorV3");
        colorSensor.setGain(8);
    }

    public void getDetectedColor() {
        NormalizedRGBA colors = colorSensor.getNormalizedColors();
        normRed = colors.red / colors.alpha;
        normGreen = colors.green / colors.alpha;
        normBlue = colors.blue / colors.alpha;

        if (normRed < 0.1 && normGreen > 0.35 && normBlue > 0.25) {
            currentDetectedColor = DetectedColor.GREEN;
            latchedDetectedColor = currentDetectedColor;
        }
        else if (normRed < 0.3 && normGreen < 0.3 && normBlue > 0.4) {
            currentDetectedColor = DetectedColor.PURPLE;
            latchedDetectedColor = currentDetectedColor;
        }
        else {
            currentDetectedColor = DetectedColor.UNKNOWN;
            // do not change latchedDetectedColor here
        }
        // Green artifact  R=.08, G=0.4, B=.3
        // Purple artifact R=.25, G=.25, B=.5
    }

    public void colorTelemetry() {
        telemetry.addData("normRed", normRed);
        telemetry.addData("normGreen", normGreen);
        telemetry.addData("normBlue", normBlue);
        telemetry.addData("currentDetectedColor", currentDetectedColor);
        telemetry.addData("latchedDetectedColor", latchedDetectedColor);
        telemetry.update();
    }

    public void teleopControls() {
//        if (alphaValue > targetValue) {
//            servoOne.setPosition(servoOnePositionTwo);
//
//        } else {
//            servoOne.setPosition(servoOnePositionOne);
//        }
    }
}
