package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

import java.util.List;

public class LimelightSensor {
    private Limelight3A limelight;
    LLResultTypes.FiducialResult tag1;

    LLResult result1;

    final double MIN_X = 1.5;
    final double MAX_X = 2.5;
    final double MIN_D = -2.5;
    final double MAX_D = 2.5;
    boolean validShooting;

    Telemetry telemetry1;
    boolean isRed;
    public static final int[] BLUE_TAGS = {38, 39, 40, 41, 42, 43, 44, 45};
    public static final int[] RED_TAGS = {30, 31, 32, 33, 34, 35, 36, 37};
    int[] targetIds;


    private final int ALIGN_THRESHOLD = 3;
    private double lastError = 0;
    private double derivative;
    private double integralSum = 0;
    private final double KP = 0.014; // Tx range is 0 to 26 --> at max offset 26, when Kp is 0.02, speed is half power
    private final double KI = 0;
    private final double KD = 0;

    public LimelightSensor(HardwareMap hardwareMap, Telemetry telemetry, boolean isRedAlliance) {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        telemetry.setMsTransmissionInterval(20);

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
                        result1 = result;
                        return true;
                    } // end if
                }  // inner for
            } // outer for
        } // if

        return false;

    } // end of method



    public double getDistance() {

        telemetry1.addData("Entered getDistance"," method");

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

        telemetry1.update();

        return distanceToTag;

    }

    public double getXValue() {

        telemetry1.addData("Entered getX"," method");

        Pose3D tagPose = tag1.getRobotPoseTargetSpace();

        telemetry1.update();

        return tagPose.getPosition().x;
    }

    public boolean isValidShooting(double xValue1, double dToTag1) {

        telemetry1.addData("Entered valid shooting "," method");

        telemetry1.addData("Distance to Tag", dToTag1);
        telemetry1.update();

        if (xValue1 >= MIN_X && xValue1 <= MAX_X && dToTag1 >= MIN_D && dToTag1 <= MAX_D) {
            validShooting = true;
        }

        return validShooting;
    }

    public void turretAdjustment(DcMotor turret1) {
        telemetry1.addData("Entered turretAdjustment "," method");
        double error = result1.getTx();
        ElapsedTime timer = new ElapsedTime();
        if (Math.abs(error) > ALIGN_THRESHOLD) {
            error = -1 * result1.getTx();
            derivative = (error - lastError) / timer.seconds();
            integralSum = integralSum + (error * timer.seconds());
            double power = (KP * error) + (KI * integralSum) + (KD * derivative);
            turret1.setPower(power);
            lastError = error;
        } else {
            turret1.setPower(0);  // aligned
        }
    }


}