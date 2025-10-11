package org.firstinspires.ftc.teamcode.opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
@Autonomous(name="Camera Stream")
public class CameraStreamLive extends LinearOpMode {

    @Override
    public void runOpMode() {
        // Initialize the webcam
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        OpenCvCamera webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);

        // Define your OpenCV pipeline
        OpenCvPipeline myPipeline = new MyOpenCvPipeline(); // Replace with your custom pipeline

        // Open the camera and start streaming to the dashboard
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
                // Stream to FTC Dashboard
                FtcDashboard.getInstance().startCameraStream(webcam, 10); // 10 FPS
            }

            @Override
            public void onError(int errorCode) {
                // Handle camera open error
            }
        });

        waitForStart();

        while (opModeIsActive()) {
            // Your robot logic here
        }

        webcam.stopStreaming();
    }

    // Example custom OpenCV pipeline
    class MyOpenCvPipeline extends OpenCvPipeline {
        @Override
        public Mat processFrame(Mat input) {
            // Implement your image processing here
            return input; // Return the processed frame
        }
    }
}