package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

import java.util.List;

@TeleOp(name = "ShootingRangeTeleop")
public class ShootingRangeTeleopTelemetryOnly extends LinearOpMode {
    private Limelight3A limelight;
    boolean shootBalls;
    final double minDistanceToTag = 0;

    final double maxDistanceToTag = 0;

    final private double minX = 0;
    final private double maxX = 0;


    @Override
    public void runOpMode() {

        limelight = hardwareMap.get(Limelight3A.class, "limelight");

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

                    // OPTIONAL:
                    // If you only want one particular HIVE AprilTag,
                    // replace 20 with your actual AprilTag ID.
        /*
        if (tagID != 20) {
            continue;
        }
        */

                    // Get the AprilTag's 3D pose RELATIVE TO THE LIMELIGHT CAMERA
                    Pose3D tagPose = tag.getTargetPoseCameraSpace();


                    // Get the position values
                    double x = tagPose.getPosition().x;
                    double y = tagPose.getPosition().y;
                    double z = tagPose.getPosition().z;

                    /*
                     * Straight-line 3D distance from the center of the
                     * Limelight camera to the AprilTag.
                     *
                     * This is the "hypotenuse" / direct camera-to-tag distance.
                     */

                    double distanceToTag = Math.sqrt(
                            (x * x) + (y * y) + (z * z)
                    );


                    // tx and ty are still useful for debugging/testing.
                    double tx = tag.getTargetXDegrees();
                    double ty = tag.getTargetYDegrees();


                    // TELEMETRY
                    telemetry.addData("AprilTag ID", tagID);

                    telemetry.addData("X", "%.3f m", x);
                    telemetry.addData("Y", "%.3f m", y);
                    telemetry.addData("Z", "%.3f m", z);

                    telemetry.addData(
                            "Camera -> Tag Distance", "%.3f m", distanceToTag
                    );

                    telemetry.addData("tx", "%.2f deg", tx);
                    telemetry.addData("ty", "%.2f deg", ty);

                    if (x >= minX && x <= maxX && distanceToTag >= minDistanceToTag && distanceToTag <= maxDistanceToTag) {
                        shootBalls = true;
                    } else {
                        telemetry.addData("Invalid Shooting", "Move Robot");
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
