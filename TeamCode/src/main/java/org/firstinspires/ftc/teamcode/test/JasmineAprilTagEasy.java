package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

public class JasmineAprilTagEasy {
    private static final boolean USE_WEBCAM = true;  // true for webcam, false for phone camera

    /**
     * The variable to store our instance of the AprilTag processor.
     */
    private AprilTagProcessor aprilTag;

    /**
     * The variable to store our instance of the vision portal.
     */
    private VisionPortal visionPortal;
    private Telemetry myTelemetry;

    private void initialTelemetry(){

        // Wait for the DS start button to be touched.
        myTelemetry.addData("DS preview on/off", "3 dots, Camera Stream");
        myTelemetry.addData(">", "Touch START to start OpMode");
        myTelemetry.update();
    }

    /**
     * Initialize the AprilTag processor.
     */
    public void initAprilTag(HardwareMap hardwareMap, Telemetry tlm) {

        // Create the AprilTag processor the easy way.
        aprilTag = AprilTagProcessor.easyCreateWithDefaults();

        // Create the vision portal the easy way. If use webcam is true,
        // then we are using the webcam for the camera otherwise we are using
        // a phone camera.
        if (USE_WEBCAM) {
            visionPortal = VisionPortal.easyCreateWithDefaults(
                    hardwareMap.get(WebcamName.class, "Webcam 1"), aprilTag);
        } else {
            visionPortal = VisionPortal.easyCreateWithDefaults(
                    BuiltinCameraDirection.BACK, aprilTag);
        }
        myTelemetry = tlm;
        initialTelemetry();

    }   // end method initAprilTag()

    /**
     * Add telemetry about AprilTag detections.
     */
    public void telemetryAprilTag() {

        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        myTelemetry.addData("# AprilTags Detected", currentDetections.size());

        // Step through the listv of detections and display info for each one.
        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata != null) {
                myTelemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
                myTelemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z));
                myTelemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw));
                myTelemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation));
            } else {
                myTelemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
                myTelemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
            }
        }   // end for() loop

        // Add "key" information to telemetry
        myTelemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
        myTelemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)");
        myTelemetry.addLine("RBE = Range, Bearing & Elevation");

        // Push telemetry to the Driver Station.
        myTelemetry.update();
    }   // end method telemetryAprilTag()

    public void cameraStreaming(Gamepad gamepad) {

        // Save CPU resources; can resume streaming when needed.
        if (gamepad.dpad_down) {
            visionPortal.stopStreaming();
        } else if (gamepad.dpad_up) {
            visionPortal.resumeStreaming();
        }
    }

    public void stopAprilTagProcessing(){
        // Save more CPU resources when camera is no longer needed.
        visionPortal.close();
    }
}
