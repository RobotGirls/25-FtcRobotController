package org.firstinspires.ftc.teamcode.biobuzz2627.autoCode;

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
@Autonomous(name = "LM0 Blue Flower", group = "Blue Auto")
public class LM0BlueFlower extends LinearOpMode {
    private boolean first = true;

    @Override
    public void runOpMode() throws InterruptedException {
        // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(56, 15, Math.toRadians(-150));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);


        // actionBuilder builds from the drive steps passed to it
        TrajectoryActionBuilder toShootHive = drive.actionBuilder(initialPose)
                .splineToLinearHeading(new Pose2d(50, 15, Math.toRadians(180)), Math.toRadians(180));

        TrajectoryActionBuilder toIntakePollen = drive.actionBuilder(initialPose)
                .splineToLinearHeading(new Pose2d(55, 15, Math.toRadians(45)), Math.toRadians(45));

        TrajectoryActionBuilder toShootAgain = drive.actionBuilder(initialPose)
                .splineToLinearHeading(new Pose2d(54, 15, Math.toRadians(180)), Math.toRadians(180));

        Action toPark = toShootAgain.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(30, 60), Math.toRadians(180))

                .build();

        Action firstTraj = toShootHive.build();
        Action secondTraj = toIntakePollen.build();
        Action thirdTraj = toShootAgain.build();


        while (!isStopRequested() && !opModeIsActive()) {
            telemetry.addData("Robot position: ", drive.updatePoseEstimate());
            telemetry.update();
        }
        waitForStart();
        if (isStopRequested()) return;

        // IN RUNTIME
        // running the action sequence!
        Actions.runBlocking(
                new SequentialAction(
                        // wait to shoot
                        // go back to flower
                        // shoot
                        firstTraj,
                        secondTraj,
                        thirdTraj,
                        toPark
                )
        );

        // add mechanism code below

    }

}

//MeepMeep path
//        myBot.runAction(myBot.getDrive().actionBuilder(new Pose2d(56, 15, Math.toRadians(-150)))
//                .splineToLinearHeading(new Pose2d(50, 15, Math.toRadians(180)), Math.toRadians(180))
//                .waitSeconds(2)
//                .splineToLinearHeading(new Pose2d(55, 15, Math.toRadians(45)), Math.toRadians(45))
//                .waitSeconds(2)
//                .splineToLinearHeading(new Pose2d(54, 15, Math.toRadians(180)), Math.toRadians(180))
//                .waitSeconds(2)
//                .strafeToLinearHeading(new Vector2d(30, 60), Math.toRadians(180))
//                        .build());