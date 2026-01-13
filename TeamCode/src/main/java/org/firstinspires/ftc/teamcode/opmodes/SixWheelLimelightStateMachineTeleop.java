package org.firstinspires.ftc.teamcode.opmodes;


import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

@TeleOp(name = "SIX WHEEL TELEOP")

public class SixWheelLimelightStateMachineTeleop extends LinearOpMode {

    public enum LimelightStates {
        IDLE,
        VALID_RESULTS,
        INVALID_RESULTS
    }

    LimelightStates limelightState = LimelightStates.IDLE;

    private final int ALIGN_THRESHOLD = 3;
    Servo hoodServo;
    private double lastError = 0;
    private double derivative = 0;
    private double integralSum = 0;

    private double Kp = 0.014; // Tx range is 0 to 26 --> at max offset 26, when Kp is 0.02, speed is half power
    private double Ki = 0;
    private double Kd = 0;
    private double shooterSpeed = -1500;

    Limelight3A limelight;
    DcMotorEx shooter;
    DcMotor turret;

    public DcMotor  leftMotor   = null;
    public DcMotor  rightMotor  = null;
    public final double TURRET_OFFSET = 90; // FIXME figure out how many degrees to the side the turret will be aiming relative to front of robot

    @Override
    public void runOpMode() throws InterruptedException {

        leftMotor = hardwareMap.get(DcMotor.class, "frontLeft");
        rightMotor = hardwareMap.get(DcMotor.class, "frontRight");

        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        /*
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
         */

        turret = hardwareMap.get(DcMotor.class, "turret");
        shooter = hardwareMap.get(DcMotorEx.class, "shooter");
        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        hoodServo = hardwareMap.get(Servo.class, "hoodServo");

        shooter.setVelocityPIDFCoefficients(10,3,3,2);
        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.setMsTransmissionInterval(11);

        limelight.pipelineSwitch(0);

        /*
         * Starts polling for data.  If you neglect to call start(), getLatestResult() will return null.
         */
        limelight.start();

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            double drive = -gamepad1.left_stick_y; // Remember, Y stick value is reversed; Forward/Backward
            double turn = gamepad1.right_stick_x; //Left/Right turn

            double leftPower = drive + turn;
            double rightPower = drive - turn;

            double power = 0; // initial turret power

            leftMotor.setPower(leftPower);
            rightMotor.setPower(rightPower);

            switch(limelightState) {
                case IDLE:
                    LLResult result = limelight.getLatestResult();

                    if (result.isValid()) {
                        limelightState = LimelightStates.VALID_RESULTS;
                    }
                case VALID_RESULTS:
                    LLResult result1 = limelight.getLatestResult();
                        /*
                        Access general information
                        double captureLatency = result.getCaptureLatency();
                        double targetingLatency = result.getTargetingLatency();
                        double parseLatency = result.getParseLatency();
                 */

                        telemetry.addData("tx", result1.getTx());
                        telemetry.addData("txnc", result1.getTxNC());
                        telemetry.addData("ty", result1.getTy());
                        telemetry.addData("tync", result1.getTyNC());
                        Pose3D botpose = result1.getBotpose();
                        double robotx = botpose.getPosition().x;
                        double roboty = botpose.getPosition().y;
                        telemetry.addData("MT1 Location", "(" + robotx + ", " + roboty + ")");
                        if (robotx < -0.5) {
                            // if robot is very close to the goal
                            shooterSpeed = 1250;
                            hoodServo.setPosition(0.6); // FIXME change servo values to those found in testing
                        } else if (robotx >= -0.5 && robotx < 0.5) {
                            // if robot is around the tip (farthest end) of the close launch zone
                            shooterSpeed = -1340;
                            hoodServo.setPosition(0.4); // FIXME change servo values to those found in testing

                        } else {
                            // if robot is in the far launch zone
                            shooterSpeed = -1620;
                            hoodServo.setPosition(0.2); // FIXME change servo values to those found in testing
                        }

                        double error = result1.getTx();
                        ElapsedTime timer = new ElapsedTime();
                        if (Math.abs(error) > ALIGN_THRESHOLD) {
                            error = -1 * result1.getTx();
                            derivative = (error - lastError) / timer.seconds();
                            integralSum = integralSum + (error * timer.seconds());
                            power = (Kp * error) + (Ki * integralSum) + (Kd * derivative);
                            turret.setPower(power);
                            lastError = error;
                        } else {
                            turret.setPower(0);  // aligned
                        }
                        telemetry.update();

                case INVALID_RESULTS:
                        // if we don't see an apriltag
                        telemetry.addData("Limelight", "No data available");
                        double turretPower = gamepad2.left_stick_x;

                        turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                        if (turret.getCurrentPosition() >= -2000 && turret.getCurrentPosition() <= 2000) { // FIXME change encoder value after testing
                            turret.setPower(turretPower);
                            telemetry.addData("Current Motor Position", turret.getCurrentPosition());
                        } else {
                            turret.setPower(0);
                            telemetry.addData("Current Motor Position", "Too Far!");
                        }
                        telemetry.update();
            }

        }
        limelight.stop();
    }
}