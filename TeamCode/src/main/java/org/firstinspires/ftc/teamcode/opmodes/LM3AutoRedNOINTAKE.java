package org.firstinspires.ftc.teamcode.opmodes;

// RR-specific imports

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.TankDrive;
import org.firstinspires.ftc.teamcode.mechanismCode.IntakeRoadRunner;
import org.firstinspires.ftc.teamcode.mechanismCode.ShooterRoadRunner;
import org.firstinspires.ftc.teamcode.mechanismCode.TransferRoadRunner;

//@Config
@Autonomous(name = "LM3 Red NO INTAKE")
public class LM3AutoRedNOINTAKE extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {

        // telemetry.setAutoClear(false);
        // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(-52, 46, Math.toRadians(130));
        TankDrive drive = new TankDrive(hardwareMap, initialPose);
        IntakeRoadRunner intake= new IntakeRoadRunner(hardwareMap,telemetry);
        ShooterRoadRunner shooter = new ShooterRoadRunner(hardwareMap, telemetry);
        TransferRoadRunner transfer = new TransferRoadRunner(hardwareMap,telemetry);

        // actionBuilder builds from the drive steps passed to it

        TrajectoryActionBuilder toShoot = drive.actionBuilder(initialPose)
                .lineToY(8);

        Action outOfZone = toShoot.endTrajectory().fresh()
                .lineToY(30)
                .turn(Math.toRadians(-130))
                .lineToX(0)
                .build();


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
                new SequentialAction(
                        firstTraj,
                        shooter.shootArtifact(),
                        new ParallelAction(
                                shooter.shootArtifact(),
                                intake.intakeArtifact(),
                                transfer.intakeArtifact()
                        ),
                        outOfZone

                )
        );

    }

}
