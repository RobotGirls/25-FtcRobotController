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

    double x;
    boolean validShooting;

    double distanceToTag;

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
    }

    public boolean isShootingValid() {
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

                for (int targetId : targetIds) {
                    if (tagID == targetId) {
                        //tag1 = tag;
                        result1 = result;
                        return checkShooting(tag);
                    } // end if
                }  // inner for
            } // outer for
        } // if

        return false;

    } // end of method



    public boolean checkShooting(LLResultTypes.FiducialResult myTag) {

        telemetry1.addData("Entered getDistance","method");

        Pose3D tagPose = myTag.getRobotPoseTargetSpace();

        x = tagPose.getPosition().x;
        double y = tagPose.getPosition().y;
        double z = tagPose.getPosition().z;


         distanceToTag = Math.sqrt(
                (x*x) + (y*y) + (z*z));

        telemetry1.addData("X", "%.3f m", x);
        telemetry1.addData("Y", "%.3f m", y);
        telemetry1.addData("Z", "%.3f m", z);

        if (x >= MIN_X && x <= MAX_X && distanceToTag >= MIN_D && distanceToTag <= MAX_D) {
            validShooting = true;
        } else {
            validShooting = false;
        }

        return validShooting;

    }

    public double getDistanceToTag() {
        return distanceToTag;
    }

    public void turretAdjustment(DcMotor turret1) {
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