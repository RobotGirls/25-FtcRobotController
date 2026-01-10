package org.firstinspires.ftc.teamcode.opmodes;


import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

@TeleOp(name = "SIX WHEEL TELEOP")

public class SixWheelLimelightStateMachineTeleop extends LinearOpMode {

    public enum TankDriveStates {
        DRIVE,
        LIMELIGHT,
        COLOR_SENSOR,
        FLYWHEEL
    }

    TankDriveStates teleopState = TankDriveStates.DRIVE;

    private Limelight3A limelight;
    private DcMotor turret;
    private DcMotorEx shooter;
    private final int ALIGN_THRESHOLD = 3;
    private double lastError = 0;
    private double derivative;
    private double integralSum = 0;

    private double Kp = 0.014; // Tx range is 0 to 26 --> at max offset 26, when Kp is 0.02, speed is half power
    private double Ki = 0;
    private double Kd = 0;
    private double shooterSpeed = 0.75;

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

        shooter.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

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

            if (teleopState == TankDriveStates.DRIVE) {
                    double drive = -gamepad1.left_stick_y; // Remember, Y stick value is reversed; Forward/Backward
                    double turn = gamepad1.right_stick_x; //Left/Right turn

                    double leftPower = drive + turn;
                    double rightPower = drive - turn;

                    double power = 0; // initial turret power

                    leftMotor.setPower(leftPower);
                    rightMotor.setPower(rightPower);

                    if (gamepad2.x || gamepad2.y || gamepad2.a) {
                        teleopState = TankDriveStates.COLOR_SENSOR;
                    }

                    LLStatus status = limelight.getStatus();

                    LLResult result = limelight.getLatestResult();

                    if (result.isValid()) {
                        /*
                        Access general information
                        double captureLatency = result.getCaptureLatency();
                        double targetingLatency = result.getTargetingLatency();
                        double parseLatency = result.getParseLatency();
                 */

                        telemetry.addData("tx", result.getTx());
                        telemetry.addData("txnc", result.getTxNC());
                        telemetry.addData("ty", result.getTy());
                        telemetry.addData("tync", result.getTyNC());
                        Pose3D botpose = result.getBotpose();
                        double robotx = botpose.getPosition().x;
                        double roboty = botpose.getPosition().y;
                        telemetry.addData("MT1 Location", "(" + robotx + ", " + roboty + ")");
                        if (robotx < -0.5) {
                            // if robot is very close to the goal
                            shooterSpeed = 0.5;
                        } else if (robotx >= -0.5 && robotx < 0.5) {
                            // if robot is around the tip (farthest end) of the close launch zone
                            shooterSpeed = 0.6;
                        } else {
                            // if robot is in the far launch zone
                            shooterSpeed = 0.75;
                        }

                        double error = result.getTx();
                        ElapsedTime timer = new ElapsedTime();
                        if (Math.abs(error) > ALIGN_THRESHOLD) {
                            error = -1 * result.getTx();
                            derivative = (error - lastError) / timer.seconds();
                            integralSum = integralSum + (error * timer.seconds());
                            power = (Kp * error) + (Ki * integralSum) + (Kd * derivative);
                            turret.setPower(power);
                            lastError = error;
                        } else {
                            turret.setPower(0);  // aligned
                        }


                    } else {
                        // if we don't see an apriltag
                        turret.setPower(0);
                        telemetry.addData("Limelight", "No data available");

                        // turret.setTargetPosition(robot.getHeading() - TURRET_OFFSET);
                    }

            }

        }
        limelight.stop();
    }
}