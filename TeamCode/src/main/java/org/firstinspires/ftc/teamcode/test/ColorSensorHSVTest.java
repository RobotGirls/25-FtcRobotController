package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Color Sensor HSV Test A")
public class ColorSensorHSV extends LinearOpMode {

    HSVColorSensorTest colorSensor;
    colorSensor = new HSVColorSensor();
    @Override
    public void runOpMode() throws InterruptedException {
        colorSensor.initColorSensor();
        while (!isStarted()) {
            getDetectedColor();
            colorTelemetry();
        }
        // wait for PLAY button to be pushed
        waitForStart();
        while (opModeIsActive()) {
            getDetectedColor();
            colorTelemetry();
        }
    } // end runOpMode
}

