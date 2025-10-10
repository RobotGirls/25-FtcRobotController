package org.firstinspires.ftc.teamcode.opmodes;

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
@Autonomous(name = "Start Wall (left) and Intake",group = "Blue Auto")
public class BlueWallLeftStartIntakeBalls extends LinearOpMode {
    private boolean first = true;
    // Timer for our old lift mechanism - incorporate if necessary
   // ElapsedTime liftTimer = new ElapsedTime();

    @Override
    public void runOpMode() throws InterruptedException {
       // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(70, -16, Math.toRadians(180));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);


        // actionBuilder builds from the drive steps passed to it
        TrajectoryActionBuilder toBasket = drive.actionBuilder(initialPose)
                .strafeTo(new Vector2d(0, 0))
                .turn(Math.toRadians(45))
                .waitSeconds(.5)
                .turn(Math.toRadians(45))
                .strafeTo(new Vector2d(12, -30))
                .strafeTo(new Vector2d(0, 0))
                .waitSeconds(1.5);


        Action toSub = toBasket.endTrajectory().fresh()
                // example of a trajectory following another trajectory
//                .turn(Math.toRadians(45))
//                .strafeTo(new Vector2d(45,55))
//                .strafeTo(new Vector2d(45,15))
//                .turn(Math.toRadians(90))
//                .lineToX(26)
                .build();

        Action firstTraj = toBasket.build();

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
//                        liftPivot.liftPivotDown(),
                        firstTraj, // go to the basket, push samples, and then submersible
                        toSub // push samples, go to submersible
                )
        );

        // add mechanism code below

    }

}

