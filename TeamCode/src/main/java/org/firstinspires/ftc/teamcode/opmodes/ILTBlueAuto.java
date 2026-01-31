package org.firstinspires.ftc.teamcode.opmodes;

// RR-specific imports

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.SleepAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.TankDrive;

import org.firstinspires.ftc.teamcode.mechanismCode.IntakeRoadRunner;
import org.firstinspires.ftc.teamcode.mechanismCode.ShooterRoadRunner;
import org.firstinspires.ftc.teamcode.mechanismCode.TransferRoadRunner;

//@Config
@Autonomous(name = "ILT Blue")
public class ILTBlueAuto extends LinearOpMode {



    @Override
    public void runOpMode() throws InterruptedException {

        Pose2d initialPose = new Pose2d(-52, -46, Math.toRadians(-130));
        TankDrive drive = new TankDrive(hardwareMap, initialPose);
        IntakeRoadRunner intake= new IntakeRoadRunner(hardwareMap,telemetry);
        ShooterRoadRunner shooter = new ShooterRoadRunner(hardwareMap, telemetry);
        TransferRoadRunner transfer = new TransferRoadRunner(hardwareMap,telemetry);

        TrajectoryActionBuilder toShoot = drive.actionBuilder(initialPose)
                .setReversed(true)
                .splineTo(new Vector2d(-14.5,-8),Math.toRadians(48.9));

        TrajectoryActionBuilder intakeBalls = toShoot.endTrajectory().fresh()
                .turn(Math.toRadians(25))
                .splineTo(new Vector2d(-16,-55),Math.toRadians(-90));

        TrajectoryActionBuilder backToShoot = intakeBalls.endTrajectory().fresh()
                .setReversed(true)
                .splineTo(new Vector2d(-8,-8),Math.toRadians(70.3));
                //.turn(Math.toRadians(90));

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
                        shooter.shooterOn(),
                        firstTraj,
                        new SleepAction(2),
                        new ParallelAction(
                                transfer.intakeArtifact(),
                                intake.intakeArtifact()

                        ),
                        intake.intakeArtifact(),
                        new ParallelAction(
                                secondTraj,
                                intake.intakeArtifact(),
                                transfer.outtakeArtifact()
                        ),
                        thirdTraj,
                        new ParallelAction(
                                transfer.intakeArtifact(),
                                intake.intakeArtifact()
                        ),
                        shooter.shooterOff(),
                        outOfZone

                )
        );

    }

}

