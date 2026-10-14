package org.firstinspires.ftc.teamcode.biobuzz2627.auto;

// RR-specific imports

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.MecanumDrive;

//@Config
@Autonomous(name = "LM0 Blue Park", group = "Blue Auto")
public class LM0ParkBlue extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(60, 0, Math.toRadians(180));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        TrajectoryActionBuilder toPark = drive.actionBuilder(initialPose)
                .lineToX(59.5)
                .setTangent(Math.toRadians(90))
                .strafeToLinearHeading(
                        new Vector2d(59.5, -25),
                        Math.toRadians(180)
                );

        Action firstTraj = toPark.build();

        while (!isStopRequested() && !opModeIsActive()) {
            telemetry.addData("Robot position: ", drive.updatePoseEstimate());
            telemetry.update();
        }

        waitForStart();

        if (isStopRequested()) return;

        Actions.runBlocking(
                new SequentialAction(
                        firstTraj
                )
        );
    }
}

//        myBot.runAction(myBot.getDrive().actionBuilder(new Pose2d(-62, 35, Math.toRadians(0)))
//        .strafeToLinearHeading(new Vector2d(-50, 15), 0)
//        .waitSeconds(2)
//                .setTangent(Math.toRadians(90))
//        .splineToLinearHeading(new Pose2d(new Vector2d(30, 58), 0), Math.toRadians(0))
//        .build());