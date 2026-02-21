package org.firstinspires.ftc.teamcode.opmodes;


import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.test.HSVColorSensor;
@Disabled
@TeleOp(name = "Color Sensor Sorting Testing With Ball Holes")

public class ColorSensorSortingTestHoles extends LinearOpMode {

    HSVColorSensor colorSensor1 = new HSVColorSensor();

    private HSVColorSensor.DetectedColor artifactColor1 = HSVColorSensor.DetectedColor.UNKNOWN;

    private Servo ballServo1;

    public DcMotor shooter;
    public DcMotor transfer;
    public DcMotor intake;



    private static final double SERVO_UP = 0.8;
    private static final double SERVO_DOWN = 0.0;

    private HSVColorSensor.DetectedColor desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;

    @Override
    public void runOpMode() throws InterruptedException {

        initHardware();

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            //drivingControls();
           // mechanismControls();

            // Pace this loop so jaw action is reasonable speed.
            sleep(50);

            colorSensing(HSVColorSensor.DetectedColor.PURPLE);

        }
    }

    public void initHardware() {

        ballServo1 = hardwareMap.get(Servo.class, "servo1");

        ballServo1.setPosition(SERVO_DOWN);

        colorSensor1.initColorSensor(hardwareMap);


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

    }

    public void mechanismControls() {

        if (gamepad2.dpad_left) {
            desiredColor = HSVColorSensor.DetectedColor.PURPLE;
        } else if (gamepad2.dpad_right) {
            desiredColor = HSVColorSensor.DetectedColor.GREEN;
        } else if (gamepad2.dpad_up) {
            desiredColor = HSVColorSensor.DetectedColor.PURPLE;
        }
    }

    public void colorSensing(HSVColorSensor.DetectedColor desiredColor1) {
        colorSensor1.getDetectedColor(telemetry);

        if (desiredColor1 != HSVColorSensor.DetectedColor.UNKNOWN) {
            if (artifactColor1 == desiredColor1) {
                ballServo1.setPosition(SERVO_UP);
                sleep(1000);
                ballServo1.setPosition(SERVO_DOWN);
                desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
            }
        }
    }
}