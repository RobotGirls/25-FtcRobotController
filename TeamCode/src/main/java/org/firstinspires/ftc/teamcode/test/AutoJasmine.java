package org.firstinspires.ftc.teamcode.test;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.MecanumDrive;
@Autonomous (name = "AutoJasmine")
public class AutoJasmine extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {

        Pose2d initialPose = new Pose2d(38, 62, Math.toRadians(89));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        while (!isStopRequested() && !opModeIsActive()) return; {
            telemetry.addData("Robot position:", drive.updatePoseEstimate());
            telemetry.update();
        }
        waitForStart();
        if (isStopRequested()) return;
    }

}
