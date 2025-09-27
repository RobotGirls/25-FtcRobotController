package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.test.HSVColorSensor;

@TeleOp(name = "Color Sensor HSV Test A")
public class ColorSensorHSVTest extends LinearOpMode {

    HSVColorSensor colorSensor = new HSVColorSensor();
    private HSVColorSensor.DetectedColor artifactColor = HSVColorSensor.DetectedColor.UNKNOWN;

    @Override
    public void runOpMode() throws InterruptedException {
        colorSensor.initColorSensor(hardwareMap);
        while (!isStarted()) {
        colorSensor.getDetectedColor(telemetry);
        artifactColor = colorSensor.getCurrentDetectedColor();
        telemetry.addData("Artifact Color",artifactColor);
        }
        // wait for PLAY button to be pushed
        waitForStart();
        while (opModeIsActive()) {
            colorSensor.getDetectedColor(telemetry);
        }
    } // end runOpMode
}

