package org.firstinspires.ftc.teamcode.opmodes;

// RR-specific imports

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.SleepAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
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
@Autonomous(name = "LM3 FAR")
public class LM3AutoFAR extends LinearOpMode {

    public final double FLYWHEEL_SPEED_LONG = -0.8;

    Pose2d initialPose;
    TankDrive drive;
    IntakeRoadRunner intake;
    ShooterRoadRunner shooter;
    TransferRoadRunner transfer;
    TurretRoadRunner turret;
    private Limelight3ASensor limelightSensor;

    @Override
    public void runOpMode() throws InterruptedException {

        initHardware();
        // telemetry.setAutoClear(false);
        // liftTimer.reset();
        // instantiating the robot at a specific pose

        TankDrive drive = new TankDrive(hardwareMap, initialPose);
        ShooterRoadRunner shooter = new ShooterRoadRunner(hardwareMap, telemetry);
        IntakeRoadRunner intake = new IntakeRoadRunner(hardwareMap,telemetry);
        TransferRoadRunner transfer = new TransferRoadRunner(hardwareMap, telemetry);

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
                                new SleepAction(5),
                                new ParallelAction(

                                        intake.intakeArtifact(),
                                        transfer.intakeArtifact()
                                ),
                                firstTraj


                        )
                )

        );
    }
    private void initHardware() {
        initialPose = new Pose2d(55, -16, Math.toRadians(180));
        drive = new TankDrive(hardwareMap, initialPose);
        intake= new IntakeRoadRunner(hardwareMap, telemetry);
        shooter = new ShooterRoadRunner(hardwareMap, telemetry);
        transfer = new TransferRoadRunner(hardwareMap, telemetry);
        limelightSensor = new Limelight3ASensor();
        limelightSensor.initLimelight(hardwareMap, telemetry);
        turret = new TurretRoadRunner(hardwareMap, telemetry, limelightSensor);
    }


}


