package org.firstinspires.ftc.teamcode.opmodes;

// RR-specific imports

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.TankDrive;

//@Config
@Autonomous(name = "TankDriveAuto")
public class TankDriveAuto extends LinearOpMode {

    public final double FLYWHEEL_SPEED_LONG = -0.8;

    @Override
    public void runOpMode() throws InterruptedException {

        // telemetry.setAutoClear(false);
        // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(60, 0, Math.toRadians(180));
        TankDrive drive = new TankDrive(hardwareMap, initialPose);


        // actionBuilder builds from the drive steps passed to it

        TrajectoryActionBuilder toShoot = drive.actionBuilder(initialPose)
                .lineToX(0)
                .turn(Math.toRadians(90))
                .lineToY(-25)
                .setReversed(true)
                .lineToY(0)
                .turn(-90)
                .lineToX(60);


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
                        firstTraj

                )
        );

    }


}



