package org.firstinspires.ftc.teamcode.test;

import android.util.Size;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

public class JasmineAprilTag {
    private static final boolean USE_WEBCAM = true;  // true for webcam, false for phone camera

    /**
     * The variable to store our instance of the AprilTag processor.
     */
    private AprilTagProcessor aprilTag;

    /**
     * The variable to store our instance of the vision portal.
     */
    private VisionPortal visionPortal;

    private void initialTelemetry(Telemetry tlm1){

        // Wait for the DS start button to be touched.
        tlm1.addData("DS preview on/off", "3 dots, Camera Stream");
        tlm1.addData(">", "Touch START to start OpMode");
        tlm1.update();
    }

    /**
     * Initialize the AprilTag processor.
     */
    public void initAprilTag(HardwareMap hardwareMap, Telemetry tlm) {

        // Create the AprilTag processor.
        aprilTag = new AprilTagProcessor.Builder()

                // The following default settings are available to un-comment and edit as needed.
                .setDrawAxes(true) // When true draws on the tag where the axes are pointing.
                .setDrawCubeProjection(true) // Draws a cube projected off of the tag to further
                // help see where the camera thinks the tag is pointing.
                .setDrawTagID(true) // Draws the ID number on the tag.
                .setDrawTagOutline(true) // Draws the outline of the tag.
                //.setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                //.setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
                //.setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)

                // == CAMERA CALIBRATION ==
                // If you do not manually specify calibration parameters, the SDK will attempt
                // to load a predefined calibration for your camera.
                //.setLensIntrinsics(578.272, 578.272, 402.145, 221.506)
                // ... these parameters are fx, fy, cx, cy.

                .build();

        // Adjust Image Decimation to trade-off detection-range for detection-rate.
        // eg: Some typical detection data using a Logitech C920 WebCam
        // Decimation = 1 ..  Detect 2" Tag from 10 feet away at 10 Frames per second
        // Decimation = 2 ..  Detect 2" Tag from 6  feet away at 22 Frames per second
        // Decimation = 3 ..  Detect 2" Tag from 4  feet away at 30 Frames Per Second (default)
        // Decimation = 3 ..  Detect 5" Tag from 10 feet away at 30 Frames Per Second (default)
        // Note: Decimation can be changed on-the-fly to adapt during a match.
        //aprilTag.setDecimation(3);

        // Create the vision portal by using a builder.
        VisionPortal.Builder builder = new VisionPortal.Builder();

        // Set the camera (webcam vs. built-in RC phone camera).
        if (USE_WEBCAM) {
            builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
        } else {
            builder.setCamera(BuiltinCameraDirection.BACK);
        }

        // Choose a camera resolution. Not all cameras support all resolutions.
        builder.setCameraResolution(new Size(640, 480));

        // Enable the RC preview (LiveView).  Set "false" to omit camera monitoring.
        builder.enableLiveView(true);

        // Set the stream format; MJPEG uses less bandwidth than default YUY2.
        //builder.setStreamFormat(VisionPortal.StreamFormat.YUY2);

        // Choose whether or not LiveView stops if no processors are enabled.
        // If set "true", monitor shows solid orange screen if no processors enabled.
        // If set "false", monitor shows camera view without annotations.
        //builder.setAutoStopLiveView(false);

        // Set and enable the processor.
        builder.addProcessor(aprilTag);

        // Build the Vision Portal, using the above settings.
        visionPortal = builder.build();

        // Disable or re-enable the aprilTag processor at any time.
        //visionPortal.setProcessorEnabled(aprilTag, true);


        initialTelemetry(tlm);

    }   // end method initAprilTag()

    /**
     * Add telemetry about AprilTag detections.
     */
    public void telemetryAprilTag(Telemetry myTelemetry) {

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
            //myTelemetry.update();

        }   // end for() loop

        // Add "key" information to telemetry
        myTelemetry.addLine("\nKey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
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
