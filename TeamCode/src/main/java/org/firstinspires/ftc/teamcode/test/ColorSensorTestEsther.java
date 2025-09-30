package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;

@TeleOp(name = "Teleop ColorSensor")

// @Disabled
public class ColorSensorTestEsther extends LinearOpMode {
    private ColorSensor colorSensor;
    private double redValue;
    private double blueValue;
    private double greenValue;
    private double alphaValue; // light intensity

    @Override
    public void runOpMode() throws InterruptedException {
        initColorSensor();
            while (!isStarted()) {
                getColor();
                colorTelemetry();

            }

            // wait for PLAY button to be pushed
            waitForStart();
            while (opModeIsActive()) {
                getColor();
                colorTelemetry();
            }

    } // end runOpMode


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
        telemetry.addData("redValue", "%.2f", redValue);
        telemetry.addData("greenValue", "%.2f", greenValue);
        telemetry.addData("blueValue", "%.2f", blueValue);
        telemetry.addData("alphaValue", "%.2f", alphaValue);
        telemetry.update();
    }

    public void checkColor(double redColor, double greenColor, double blueColor) {

    }
}
