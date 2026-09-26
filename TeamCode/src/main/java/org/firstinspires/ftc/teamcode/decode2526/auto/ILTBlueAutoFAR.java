package org.firstinspires.ftc.teamcode.decode2526.auto;

// RR-specific imports

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.SleepAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.TankDrive;
import org.firstinspires.ftc.teamcode.decode2526.mechanismCode.IntakeRoadrunner;
import org.firstinspires.ftc.teamcode.sensors.limelight.Limelight3ASensor;
import org.firstinspires.ftc.teamcode.decode2526.mechanismCode.ShooterRoadrunner;
import org.firstinspires.ftc.teamcode.decode2526.mechanismCode.TransferRoadrunner;
import org.firstinspires.ftc.teamcode.decode2526.mechanismCode.TurretRoadrunner;

@Disabled
@Autonomous(name = "ILT far blue")
public class ILTBlueAutoFAR extends LinearOpMode {

    public final double FLYWHEEL_SPEED_LONG = -0.8;

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
        // telemetry.setAutoClear(false);
        // liftTimer.reset();
        // instantiating the robot at a specific pose

        TankDrive drive = new TankDrive(hardwareMap, initialPose);
        ShooterRoadrunner shooter = new ShooterRoadrunner(hardwareMap, telemetry);
        IntakeRoadrunner intake = new IntakeRoadrunner(hardwareMap,telemetry);
        TransferRoadrunner transfer = new TransferRoadrunner(hardwareMap, telemetry);

        // actionBuilder builds from the drive steps passed to it

        TrajectoryActionBuilder toShoot = drive.actionBuilder(initialPose)
                .lineToX(40);



        Action firstTraj = toShoot.build();

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

                                shooter.shooterOnFar(),
                                new SleepAction(3),
                                new ParallelAction(
                                        intake.intakeArtifact(),
                                        transfer.intakeArtifact()
                                ),
                                shooter.shooterOff(),
                                firstTraj


                        )
                )

        );
    }
    private void initHardware() {
        initialPose = new Pose2d(55, -16, Math.toRadians(180));
        drive = new TankDrive(hardwareMap, initialPose);
        intake= new IntakeRoadrunner(hardwareMap, telemetry);
        shooter = new ShooterRoadrunner(hardwareMap, telemetry);
        transfer = new TransferRoadrunner(hardwareMap, telemetry);
        limelightSensor = new Limelight3ASensor();
        limelightSensor.initLimelightRed(hardwareMap, telemetry);
        turret = new TurretRoadrunner(hardwareMap, telemetry, limelightSensor);
    }


}
