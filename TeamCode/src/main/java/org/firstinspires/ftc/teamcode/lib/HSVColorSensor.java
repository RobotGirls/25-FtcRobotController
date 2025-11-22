package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.JavaUtil;
import org.firstinspires.ftc.robotcore.external.Telemetry;

// @Disabled
public class HSVColorSensor {
    private NormalizedColorSensor colorSensor;
    private float hue;
    private float saturation;
    private float value;

    public enum DetectedColor {
        PURPLE,
        GREEN,
        PURPLE_OR_GREEN,
        UNKNOWN
    }
    private DetectedColor currentDetectedColor = DetectedColor.UNKNOWN;
    private DetectedColor latchedDetectedColor = DetectedColor.UNKNOWN;


    public void initColorSensor(HardwareMap hardwareMap) {
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "colorSensorV3");
    }

    public void colorTelemetry(Telemetry telemetry) {
        telemetry.addData("hue", hue);
        telemetry.addData("saturation", saturation);
        telemetry.addData("value", value);
        telemetry.addData("Current Detected Color", currentDetectedColor);
        telemetry.addData("Latched (Last) Detected Color", latchedDetectedColor);
        telemetry.update();
    }

    public void getDetectedColor(Telemetry telemetry) {
        NormalizedRGBA colors = colorSensor.getNormalizedColors(); //return 4 values
        hue = JavaUtil.colorToHue(colors.toColor());
        saturation = JavaUtil.colorToSaturation(colors.toColor());
        value = JavaUtil.colorToValue(colors.toColor());
        if (hue >=220 && hue <=240) {
            currentDetectedColor = DetectedColor.PURPLE;
            latchedDetectedColor = DetectedColor.PURPLE;
        } else if (hue >= 150 && hue <=160) {
            currentDetectedColor = DetectedColor.GREEN;
            latchedDetectedColor = DetectedColor.GREEN;
        } else {
            currentDetectedColor = DetectedColor.UNKNOWN;
        }
        colorTelemetry(telemetry);
    }
    public DetectedColor getCurrentDetectedColor() {
        return currentDetectedColor;
    }
    public DetectedColor getLatchedDetectedColor() {
        return latchedDetectedColor;
    }
}
