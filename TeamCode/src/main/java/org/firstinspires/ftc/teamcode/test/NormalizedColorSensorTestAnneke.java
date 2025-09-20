package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.JavaUtil;

@TeleOp(name = "Teleop HSV Normalized Color Sensor Anneke")
public class NormalizedColorSensorTestAnneke extends LinearOpMode {
    private NormalizedColorSensor colorSensor;
    private float hue;
    private float saturation;
    private float value;
    public enum DetectedColor{
        PURPLE,
        GREEN,
        UNKNOWN
    }
    private DetectedColor currDetectedColor = DetectedColor.UNKNOWN;
    private DetectedColor latchedDetectedColor = DetectedColor.UNKNOWN;


    @Override
   public void runOpMode() throws InterruptedException {
      initColor();
      while(!isStarted()){
          getDetectedColor();
          colorTelemetry();
      }
      waitForStart();
      while(opModeIsActive()) {
          getDetectedColor();

      }
   }

    public void initColor() {
       colorSensor = hardwareMap.get(NormalizedColorSensor.class,"artifactColorSensor");
   }

   public void colorTelemetry() {
       telemetry.addData("Hue",hue);
       telemetry.addData("Saturation", saturation);
       telemetry.addData("Value", value);
       telemetry.addData("Current Detected Color",currDetectedColor);
       telemetry.addData("Latched (Last) Detected Color",latchedDetectedColor);
       telemetry.update();
   }

   public void getDetectedColor() {
        NormalizedRGBA colors = colorSensor.getNormalizedColors();
        hue = JavaUtil.colorToHue(colors.toColor());
        saturation = JavaUtil.colorToSaturation(colors.toColor());
        value = JavaUtil.colorToValue(colors.toColor());
        if (hue >= 220 && hue <= 240) {
            currDetectedColor = DetectedColor.PURPLE;
            latchedDetectedColor = DetectedColor.PURPLE;
        } else if (hue >= 150 && hue <= 160) {
            currDetectedColor = DetectedColor.GREEN;
            latchedDetectedColor = DetectedColor.GREEN;
        } else {
            currDetectedColor = DetectedColor.UNKNOWN;
        }

   }
}