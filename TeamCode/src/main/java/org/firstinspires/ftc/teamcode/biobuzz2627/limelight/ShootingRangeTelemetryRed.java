package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

import java.util.Arrays;
import java.util.List;

@TeleOp(name = "ShootingRangeTeleop Red Test")
public class ShootingRangeTelemetryRed extends LinearOpMode {

    public DcMotor leftFront = null;
    public DcMotor rightFront = null;
    public DcMotor rightBack = null;
    public DcMotor leftBack = null;

    public DcMotor shooter;
    // public DcMotor transfer;
    public DcMotor intake;
    private Limelight3A limelight;

     double targetVelocity;

    boolean shootBalls;
    final double minDistanceToTag = 1.5;

    int[] blueTags = {38, 39, 40, 41, 42, 43, 44, 45};

    int[] redTags = {30, 31, 32, 33, 34, 35, 36, 37};

    final double maxDistanceToTag = 3;

    final private double minX = -2.5;
    final private double maxX = 2.5;


    @Override
    public void runOpMode() {

        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");

        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        shooter = hardwareMap.get(DcMotorEx.class, "shooter");

        telemetry.setMsTransmissionInterval(11);

        // Pipeline 0: blue
        limelight.pipelineSwitch(0);

        limelight.start();

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            double yd = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
            double xd = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x;

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio,
            // but only if at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(yd) + Math.abs(xd) + Math.abs(rx), 1);
            double frontLeftPower = (yd + xd + rx) / denominator;
            double backLeftPower = (yd - xd + rx) / denominator;
            double frontRightPower = (yd - xd - rx) / denominator;
            double backRightPower = (yd + xd - rx) / denominator;

            leftFront.setPower(0.8 * frontLeftPower);
            leftBack.setPower(0.8 * backLeftPower);
            rightFront.setPower(0.8 * frontRightPower);
            rightBack.setPower(0.8 * backRightPower);

            LLResult result = limelight.getLatestResult();

            if (result.isValid()) {

                List<LLResultTypes.FiducialResult> fiducials =
                        result.getFiducialResults();

                for (LLResultTypes.FiducialResult tag : fiducials) {

                    // AprilTag ID
                    int tagID = tag.getFiducialId();

                    if (Arrays.stream(redTags).anyMatch(x -> x == tagID)) {
                        // Get the AprilTag's 3D pose relative to target space
                        // Index 2 of translation array corresponds to z-axis (distance)
                        Pose3D tagPose = tag.getRobotPoseTargetSpace();

                        // z-axis represents direct forward distance to tag face


                        // Get the position values
                        double x = tagPose.getPosition().x;
                        double y = tagPose.getPosition().y;
                        double z = tagPose.getPosition().z;


                        double distanceToTag = Math.sqrt(
                                (x*x) + (y*y) + (z*z));


                        // tx and ty are still useful for debugging/testing.
                        // get turning error - horizontal angle in degrees
                        double tx = tag.getTargetXDegrees();
                        double ty = tag.getTargetYDegrees();


                        // TELEMETRY
                        telemetry.addData("AprilTag ID", tagID);

                        telemetry.addData("X", "%.3f m", x);
                        telemetry.addData("Y", "%.3f m", y);
                        telemetry.addData("Z", "%.3f m", z);

                        telemetry.addData("tx", "%.2f deg", tx);
                        telemetry.addData("ty", "%.2f deg", ty);

                        if (x >= minX && x <= maxX && distanceToTag >= minDistanceToTag && distanceToTag <= maxDistanceToTag) {
                            shootBalls = true;
                        } else {
                            telemetry.addData("Invalid Shooting", "Move Robot");
                        }

                    }

                    }

            }
            else {
                telemetry.addLine("No valid Limelight result");
            }

            telemetry.update();


        }


    }
}
