package org.firstinspires.ftc.teamcode.opmodes;

// RR-specific imports

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.TankDrive;

import org.firstinspires.ftc.teamcode.mechanismCode.IntakeRoadRunner;
import org.firstinspires.ftc.teamcode.mechanismCode.ShooterRoadRunner;
import org.firstinspires.ftc.teamcode.mechanismCode.TransferRoadRunner;

//@Config
@Autonomous(name = "LM3 Blue")
public class LM3AutoBlue extends LinearOpMode {

    public final double FLYWHEEL_SPEED_LONG = -0.8;

    @Override
    public void runOpMode() throws InterruptedException {

        // telemetry.setAutoClear(false);
        // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(60, 0, Math.toRadians(180));
        TankDrive drive = new TankDrive(hardwareMap, initialPose);
        ShooterRoadRunner shooter = new ShooterRoadRunner(hardwareMap, telemetry);
        IntakeRoadRunner intake = new IntakeRoadRunner(hardwareMap,telemetry);
        TransferRoadRunner transfer = new TransferRoadRunner(hardwareMap, telemetry);

        // actionBuilder builds from the drive steps passed to it

        TrajectoryActionBuilder toShoot = drive.actionBuilder(new Pose2d(-52, -46, Math.toRadians(-130)))
                .setReversed(false)
                .lineToY(-8);
        TrajectoryActionBuilder intakeBalls = drive.actionBuilder(new Pose2d(-52, -2, Math.toRadians(-130)))
                .turn(Math.toRadians(30))
                .splineTo(new Vector2d(-10, -50), Math.toRadians(-90))
                .waitSeconds(0.1);
        TrajectoryActionBuilder backToShoot = drive.actionBuilder(new Pose2d(10, 50, Math.toRadians(-90)))
                .setReversed(true)
                .splineTo(new Vector2d(-12, -8), Math.toRadians(45));
        Action outOfZone = backToShoot.endTrajectory().fresh()
                .turn(Math.toRadians(90))
                .lineToX(0)
                .build();


        Action firstTraj = toShoot.build();
        Action secondTraj = intakeBalls.build();
        Action thirdTraj = backToShoot.build();


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
                        firstTraj,
                        shooter.shootArtifact(),
                        new ParallelAction(
                                secondTraj,
                                intake.intakeArtifact(),
                                transfer.intakeArtifact()
                        ),
                        thirdTraj,
                        shooter.shootArtifact(),
                        outOfZone

                )
        );
    }



}


