package org.firstinspires.ftc.teamcode.opmodes;


import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.test.HSVColorSensor;

@TeleOp(name = "Sensor: Limelight3A", group = "Sensor")

public class LimelightTurretAiming extends LinearOpMode {

    HSVColorSensor colorSensor1 = new HSVColorSensor();
    HSVColorSensor colorSensor2 = new HSVColorSensor();
    HSVColorSensor colorSensor3 = new HSVColorSensor();
    private HSVColorSensor.DetectedColor artifactColor1 = HSVColorSensor.DetectedColor.UNKNOWN;
    private HSVColorSensor.DetectedColor artifactColor2 = HSVColorSensor.DetectedColor.UNKNOWN;
    private HSVColorSensor.DetectedColor artifactColor3 = HSVColorSensor.DetectedColor.UNKNOWN;
    private Limelight3A limelight;
    private DcMotor turret;
    private final int ALIGN_THRESHOLD = 3;
    private double lastError = 0;
    private double derivative;
    private double integralSum = 0;

    private double Kp = 0.014; // Tx range is 0 to 26 --> at max offset 26, when Kp is 0.02, speed is half power
    private double Ki = 0;
    private double Kd = 0;

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

            drivingControls();
            mechanismControls();

            // Pace this loop so jaw action is reasonable speed.
            sleep(50);

            limelightPIDController();
            colorSensing();

        }
        limelight.stop();
    }

    public void initHardware() {
        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
        rightBack = hardwareMap.get(DcMotor.class, "backRight");
        leftBack = hardwareMap.get(DcMotor.class, "backLeft");

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);

        shooter = hardwareMap.get(DcMotor.class, "shooter");
        transfer = hardwareMap.get(DcMotor.class, "transfer");
        intake = hardwareMap.get(DcMotor.class, "intake");

        ballServo1 = hardwareMap.get(Servo.class, "servo1");
        ballServo2 = hardwareMap.get(Servo.class,"servo2");
        ballServo3 = hardwareMap.get(Servo.class,"servo3");

        ballServo1.setPosition(SERVO_DOWN);
        ballServo2.setPosition(SERVO_DOWN);
        ballServo3.setPosition(SERVO_DOWN);

        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        turret = hardwareMap.get(DcMotor.class, "turret");
        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        telemetry.setMsTransmissionInterval(11);

        limelight.pipelineSwitch(0);
        /*
         * Starts polling for data.  If you neglect to call start(), getLatestResult() will return null.
         */
        limelight.start();

        colorSensor1.initColorSensor(hardwareMap);
        colorSensor2.initColorSensor(hardwareMap);
        colorSensor3.initColorSensor(hardwareMap);


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
        if (gamepad2.left_bumper) {
            intake.setPower(1);
            transfer.setPower(1);
        }
        else if (gamepad2.right_bumper) {
            intake.setPower(-1);
            transfer.setPower(-1);
        }
        else {
            intake.setPower(0);
            transfer.setPower(0);
        }

        if (gamepad2.x) {
            shooter.setPower(-0.72);
            flywheelState = FlywheelState.ON;
        }
        else if (gamepad2.b) {
            shooter.setPower(-0.44);
            flywheelState = FlywheelState.ON;
        }
        else if (gamepad2.a) {
            shooter.setPower(0.5);
        }
        else {
            shooter.setPower(0);
        }

        if (gamepad2.dpad_left) {
            desiredColor = HSVColorSensor.DetectedColor.PURPLE;
        } else if (gamepad2.dpad_right) {
            desiredColor = HSVColorSensor.DetectedColor.GREEN;
        } else if (gamepad2.dpad_up) {
            desiredColor = HSVColorSensor.DetectedColor.PURPLE_OR_GREEN;
        }
    }
    public void limelightPIDController() {
        LLStatus status = limelight.getStatus();
        telemetry.addData("Name", "%s",
                status.getName());
        telemetry.addData("LL", "Temp: %.1fC, CPU: %.1f%%, FPS: %d",
                status.getTemp(), status.getCpu(),(int)status.getFps());
        telemetry.addData("Pipeline", "Index: %d, Type: %s",
                status.getPipelineIndex(), status.getPipelineType());


        LLResult result = limelight.getLatestResult();

        if (result.isValid()) {

            // Access general information
            Pose3D botpose = result.getBotpose();
            double captureLatency = result.getCaptureLatency();
            double targetingLatency = result.getTargetingLatency();
            double parseLatency = result.getParseLatency();

            telemetry.addData("tx", result.getTx());
            telemetry.addData("txnc", result.getTxNC());
            telemetry.addData("ty", result.getTy());
            telemetry.addData("tync", result.getTyNC());

            telemetry.addData("Botpose", botpose.toString());

            double error = result.getTx();
            ElapsedTime timer = new ElapsedTime();
            if (Math.abs(error) > ALIGN_THRESHOLD) {
                error = -1 * result.getTx();
                derivative = (error - lastError) / timer.seconds();
                integralSum = integralSum + (error * timer.seconds());
                double power = (Kp * error) + (Ki * integralSum) + (Kd * derivative);
                turret.setPower(power);
                lastError = error;
            } else {
                turret.setPower(0);  // aligned
            }
        } else {
            turret.setPower(0);
            telemetry.addData("Limelight", "No data available");
        }

        telemetry.update();
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
                    ballServo1.setPosition(SERVO_UP);
                    sleep(1000);
                    ballServo1.setPosition(SERVO_DOWN);
                    desiredColor = HSVColorSensor.DetectedColor.UNKNOWN;
                }
            }
        }
}