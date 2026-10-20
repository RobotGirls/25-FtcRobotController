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
@Autonomous(name = "LM1 Blue Park Flower", group = "Blue Auto")
public class LM1BlueParkGarden extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(-10, 61, Math.toRadians(270));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        TrajectoryActionBuilder toShoot = drive.actionBuilder(initialPose)
                .lineToY(40);

        Action toPark = toShoot.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(48,40), Math.toRadians(180))
                .strafeToLinearHeading(new Vector2d(58,-40), Math.toRadians(180))
                .build();


        Action firstTraj = toShoot.build();

        while (!isStopRequested() && !opModeIsActive()) {
            telemetry.addData("Robot position: ", drive.updatePoseEstimate());
            telemetry.update();
        }

        waitForStart();

        if (isStopRequested()) return;

        Actions.runBlocking(
                new SequentialAction(
                        firstTraj,
                        toPark
                )
        );
    }
}