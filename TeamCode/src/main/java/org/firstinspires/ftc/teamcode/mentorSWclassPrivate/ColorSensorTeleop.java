package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.Servo;

// I2C port ??  colorSensor
// Sero Port ??: servoOne
// https://www.revrobotics.com/rev-31-1557/
@TeleOp(name = "Teleop ColorSensor")

// @Disabled
public class ColorSensorTeleop extends LinearOpMode {
    private ColorSensor colorSensor;
    private double redValue;
    private double blueValue;
    private double greenValue;
    private double alphaValue; // light intensity
    private double targetValue = 1000;

    private Servo servoOne;
    private double servoOneInitPosition = 0.5;
    private double servoOnePositionOne = 0.0;
    private double servoOnePositionTwo = 1.0;

    @Override
    public void runOpMode() {
        initColorSensor();
        initHardware();
            while (!isStarted()) {
                getColor();
                colorTelemetry();

            }

            // wait for PLAY button to be pushed
            waitForStart();
            while (opModeIsActive()) {
                getColor();
                colorTelemetry();
                teleopControls();
            }

    } // end runOpMode

    public void initHardware() {
        initServoOne();
        initColorSensor();
    }

    public void initServoOne() {
        servoOne = hardwareMap.get(Servo.class, "servoOne");
        servoOne.setDirection(Servo.Direction.FORWARD);
        servoOne.setPosition(servoOneInitPosition);
    }

    public void initColorSensor() {
        colorSensor = hardwareMap.get(ColorSensor.class, "colorSensor");
    }

    public void getColor() {
        redValue = colorSensor.red();
        blueValue = colorSensor.blue();
        greenValue = colorSensor.green();
        alphaValue = colorSensor.alpha(); // light intensity
    }
    public void colorTelemetry() {
        telemetry.addData("redValue", "%0.2f", redValue);
        telemetry.addData("greenValue", "%0.2f", greenValue);
        telemetry.addData("blueValue", "%0.2f", blueValue);
        telemetry.addData("alphaValue", "%0.2f", alphaValue);
        telemetry.update();
    }

    public void teleopControls() {
        if (alphaValue > targetValue) {
            servoOne.setPosition(servoOnePositionTwo);

        } else {
            servoOne.setPosition(servoOnePositionOne);
        }
    }
}
