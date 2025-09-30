package org.firstinspires.ftc.teamcode.test;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.MecanumDrive;

@Config
@Autonomous (name = "AutoEsther")
public class AutoEsther extends LinearOpMode {

    @Override
    public void runOpMode() {
    Pose2d initialPose = new Pose2d(38, 62, Math.toRadians(-89));
    MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

    TrajectoryActionBuilder tab1 = drive.actionBuilder(initialPose)
            .lineToYSplineHeading(33, Math.toRadians(0));


    while (!isStopRequested() && !opModeIsActive()) {
        telemetry.addData("Robot position: ", drive.updatePoseEstimate());
        telemetry.update();
        }
        waitForStart();
        if (isStopRequested()) return;

        Action trajectoryActionChosen;
        trajectoryActionChosen = tab1.build();
        Actions.runBlocking(
                new SequentialAction(
                        trajectoryActionChosen
                )
        );
    }
}
