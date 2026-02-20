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
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.TankDrive;

import org.firstinspires.ftc.teamcode.mechanismCode.IntakeRoadRunner;
import org.firstinspires.ftc.teamcode.mechanismCode.Limelight3ASensor;
import org.firstinspires.ftc.teamcode.mechanismCode.ShooterRoadRunner;
import org.firstinspires.ftc.teamcode.mechanismCode.TransferRoadRunner;
import org.firstinspires.ftc.teamcode.mechanismCode.TurretRoadRunner;

//@Config
@Autonomous(name = "ILT Red")
public class ILTRedAuto extends LinearOpMode {

    TankDrive drive;
    IntakeRoadRunner intake;
    ShooterRoadRunner shooter;
    TransferRoadRunner transfer;
    TurretRoadRunner turret;
    Pose2d initialPose;
    private Limelight3ASensor limelightSensor;
    @Override

    public void runOpMode() throws InterruptedException {

        initHardware();
        // telemetry.setAutoClear(false);
        // liftTimer.reset();
        // instantiating the robot at a specific pose

        // actionBuilder builds from the drive steps passed to it

        TrajectoryActionBuilder toShoot = drive.actionBuilder(initialPose)
                .setReversed(true)
                .splineTo(new Vector2d(-10,8),Math.toRadians(-36));
        TrajectoryActionBuilder intakeBalls = toShoot.endTrajectory().fresh()
                .lineToX(-3)
                .turn(Math.toRadians(-38))
                .splineTo(new Vector2d(-4,38.5),Math.toRadians(90));

        TrajectoryActionBuilder backToShoot = intakeBalls.endTrajectory().fresh()
                .setReversed(true)
                .splineTo(new Vector2d(-8,8),Math.toRadians(-38));


        Action outOfZone = backToShoot.endTrajectory().fresh()
                .turn(Math.toRadians(90))
                .lineToX(2)
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
                new ParallelAction(
                        turret.aimTurretContinuous(),
                        new SequentialAction(
                                shooter.shooterOn(),
                                firstTraj,
                                new SleepAction(3.5),
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
                )
        );


    }
    private void initHardware() {
        initialPose = new Pose2d(-52, 46, Math.toRadians(130));
        drive = new TankDrive(hardwareMap, initialPose);
        intake = new IntakeRoadRunner(hardwareMap, telemetry);
        shooter = new ShooterRoadRunner(hardwareMap, telemetry);
        transfer = new TransferRoadRunner(hardwareMap, telemetry);
        limelightSensor = new Limelight3ASensor();
        limelightSensor.initLimelight(hardwareMap, telemetry);
        turret = new TurretRoadRunner(hardwareMap, telemetry, limelightSensor);
    }
}





