package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;

@TeleOp(name = "Teleop Color Sensor Anneke")
public class ColorSensorTestAnneke extends LinearOpMode {
    private ColorSensor colorSensor;
    int redValue;
    int blueValue;
    int greenValue;
    int alphaValue;
   @Override
   public void runOpMode() throws InterruptedException {
      initColor();
      getColor();
      colorTelemetry();

   }

   public void initColor() {
       colorSensor = hardwareMap.get(ColorSensor.class,"colorSensorArtifacts");
   }

   public void getColor() {
       redValue = colorSensor.red();
       blueValue= colorSensor.blue();
       greenValue = colorSensor.green();
       alphaValue = colorSensor.alpha();
   }

   public void colorTelemetry() {
       telemetry.addData("Red Value","%0.2f",redValue);
       telemetry.addData("Blue Value", "%0.2f",blueValue);
       telemetry.addData("Green Value", "%0.2f",greenValue);
       telemetry.addData("Alpha Value", "%0.2f",alphaValue);
       telemetry.update();
   }


}