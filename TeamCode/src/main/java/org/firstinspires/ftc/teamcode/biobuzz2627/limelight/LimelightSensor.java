package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.hardware.HardwareMap;

import com.qualcomm.hardware.limelightvision.Limelight3A;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

import java.util.List;

public class LimelightSensor {
    private Limelight3A limelight;

    LLResultTypes.FiducialResult tag1;

    double minX = 1.5;
    double maxX = 2.5;
    double minD = -2.5;
    double maxD = 2.5;
    boolean validShooting;

    Telemetry telemetry1;

    int[] targetIds;
    boolean isRed = false;


    public static final int[] BLUE_TAGS = {38, 39, 40, 41, 42, 43, 44, 45};

    public static final int[] RED_TAGS = {30, 31, 32, 33, 34, 35, 36, 37};

    public LimelightSensor(HardwareMap hardwareMap, Telemetry telemetry, boolean isRedAlliance) {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        telemetry.setMsTransmissionInterval(11);

        telemetry1 = telemetry;
        // Starts polling for data.  If you neglect to call start(), getLatestResult() will return null.
        limelight.start();

        isRed = isRedAlliance;

        if (isRed) {
            limelight.pipelineSwitch(1);

        } else {
            limelight.pipelineSwitch(0);
        }

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
    }

    public boolean checkTag() {
        LLResult result = limelight.getLatestResult();

        if (result != null && result.isValid()) {

            List<LLResultTypes.FiducialResult> fiducials =
                    result.getFiducialResults();

            if (isRed) {
                targetIds = RED_TAGS;
            } else {
                targetIds = BLUE_TAGS;
            }

            for (LLResultTypes.FiducialResult tag : fiducials) {

                // AprilTag ID
                int tagID = tag.getFiducialId();

                tag1 = tag;

                for (int targetId : targetIds) {
                    if (tagID == targetId) {
                        return true;
                    } // end if
                }  // inner for
            } // outer for
        } // if

        return false;

    } // end of method



    public double getDistance(Telemetry telemetry) {
        // Get the AprilTag's 3D pose relative to target space
        // Index 2 of translation array corresponds to z-axis (distance)
        Pose3D tagPose = tag1.getRobotPoseTargetSpace();

        double x = tagPose.getPosition().x;
        double y = tagPose.getPosition().y;
        double z = tagPose.getPosition().z;


        double distanceToTag = Math.sqrt(
                (x*x) + (y*y) + (z*z));

        telemetry1.addData("X", "%.3f m", x);
        telemetry1.addData("Y", "%.3f m", y);
        telemetry1.addData("Z", "%.3f m", z);

        return distanceToTag;

    }

    public double getXValue() {
        Pose3D tagPose = tag1.getRobotPoseTargetSpace();

        double x = tagPose.getPosition().x;

        return x;
    }

    public boolean isValidShooting(double xValue1, double dToTag1) {

        if (xValue1 >= minX && xValue1 <= maxX && dToTag1 >= minD && dToTag1 <= maxD) {
            validShooting = true;
        }

        return validShooting;
    }


}