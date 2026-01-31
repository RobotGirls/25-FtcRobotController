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
import org.firstinspires.ftc.teamcode.mechanismCode.ShooterRoadRunner;
import org.firstinspires.ftc.teamcode.mechanismCode.TransferRoadRunner;

//@Config
@Autonomous(name = "ILT Auto Only Shoot")
public class ILTAutoONLYSHOOT extends LinearOpMode {



    @Override
    public void runOpMode() throws InterruptedException {

        Pose2d initialPose = new Pose2d(-52, -46, Math.toRadians(-130));
        TankDrive drive = new TankDrive(hardwareMap, initialPose);
        IntakeRoadRunner intake= new IntakeRoadRunner(hardwareMap,telemetry);
        ShooterRoadRunner shooter = new ShooterRoadRunner(hardwareMap, telemetry);
        TransferRoadRunner transfer = new TransferRoadRunner(hardwareMap,telemetry);




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

                        new SleepAction(2.5),
                        new ParallelAction(
                                transfer.intakeArtifact(),
                                intake.intakeArtifact()

                        ),
                        shooter.shooterOff()


                )
        );

    }

}

