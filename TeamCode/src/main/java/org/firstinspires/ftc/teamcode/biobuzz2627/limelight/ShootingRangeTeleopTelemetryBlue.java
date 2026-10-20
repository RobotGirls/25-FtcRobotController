package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

import org.firstinspires.ftc.teamcode.decode2526.mechanismCode.ShooterRoadrunner;

import java.util.Arrays;
import java.util.List;

@TeleOp(name = "ShootingRangeTeleop Red")
public class ShootingRangeTeleopTelemetryBlue extends LinearOpMode {
    private Limelight3A limelight;

    ShooterRoadrunner shooter;

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

        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        shooter = new ShooterRoadrunner(hardwareMap,telemetry);

        telemetry.setMsTransmissionInterval(11);

        // Pipeline 0: blue
        limelight.pipelineSwitch(0);

        limelight.start();

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            LLResult result = limelight.getLatestResult();

            if (result.isValid()) {

                List<LLResultTypes.FiducialResult> fiducials =
                        result.getFiducialResults();

                for (LLResultTypes.FiducialResult tag : fiducials) {

                    // AprilTag ID
                    int tagID = tag.getFiducialId();

                    if (Arrays.stream(blueTags).anyMatch(x -> x == tagID)) {
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
