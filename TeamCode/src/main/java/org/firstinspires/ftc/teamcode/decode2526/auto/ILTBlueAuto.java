package org.firstinspires.ftc.teamcode.decode2526.auto;

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
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.TankDrive;

import org.firstinspires.ftc.teamcode.decode2526.mechanismCode.IntakeRoadrunner;
import org.firstinspires.ftc.teamcode.decode2526.mechanismCode.ShooterRoadrunner;
import org.firstinspires.ftc.teamcode.decode2526.mechanismCode.TransferRoadrunner;
import org.firstinspires.ftc.teamcode.decode2526.mechanismCode.TurretRoadrunner;
import org.firstinspires.ftc.teamcode.sensors.limelight.Limelight3ASensor;


//@Config
@Autonomous(name = "ILT Blue")
public class ILTBlueAuto extends LinearOpMode {

    Pose2d initialPose;
    TankDrive drive;
    IntakeRoadrunner intake;
    ShooterRoadrunner shooter;
    TransferRoadrunner transfer;
    TurretRoadrunner turret;
    private Limelight3ASensor limelightSensor;

    @Override
    public void runOpMode() throws InterruptedException {

        initHardware();

        TrajectoryActionBuilder toShoot = drive.actionBuilder(initialPose)
                .setReversed(true)
                .splineTo(new Vector2d(-21,-16),Math.toRadians(48.9));

        TrajectoryActionBuilder goForward = toShoot.endTrajectory().fresh()
                .splineTo(new Vector2d(-5, -25), Math.toRadians(0));

        TrajectoryActionBuilder intakeBalls = toShoot.endTrajectory().fresh()
                .turn(Math.toRadians(25))
                .splineTo(new Vector2d(-16,-55),Math.toRadians(-90));

        TrajectoryActionBuilder backToShoot = intakeBalls.endTrajectory().fresh()
                .setReversed(true)
                .splineTo(new Vector2d(-8,-8),Math.toRadians(70.3));
        //.turn(Math.toRadians(90));

        Action outOfZone = backToShoot.endTrajectory().fresh()
                .turn(Math.toRadians(-90))
                .lineToX(0)
                .build();


        Action firstTraj = toShoot.build();
        Action extra = goForward.build();
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
                                turret.disableAiming(),
                                intake.intakeArtifact(),
                                shooter.shooterOff(),
                                new ParallelAction(
                                        secondTraj,
                                        intake.intakeArtifact(),
                                        transfer.outtakeArtifact()
                                ),
                                shooter.shooterOn(),
                                thirdTraj,
                                turret.enableAiming(),
                                new ParallelAction(
                                        transfer.intakeArtifact(),
                                        intake.intakeArtifact()
                                ),
                                shooter.shooterOff(),
                                outOfZone

                        )
                )
        );

        telemetry.update(); //FIXME check  if this is in the right place
    } // end of runOpMode

    private void initHardware() {
        initialPose = new Pose2d(-52, -46, Math.toRadians(-130));
        drive = new TankDrive(hardwareMap, initialPose);
        intake= new IntakeRoadrunner(hardwareMap, telemetry);
        shooter = new ShooterRoadrunner(hardwareMap, telemetry);
        transfer = new TransferRoadrunner(hardwareMap, telemetry);
        limelightSensor = new Limelight3ASensor();
        limelightSensor.initLimelightBlue(hardwareMap, telemetry);
        turret = new TurretRoadrunner(hardwareMap, telemetry, limelightSensor);
    }

}