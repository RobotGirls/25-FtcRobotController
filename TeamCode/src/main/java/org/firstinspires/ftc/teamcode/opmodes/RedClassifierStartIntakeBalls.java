package org.firstinspires.ftc.teamcode.opmodes;

// RR-specific imports

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.MecanumDrive;

//@Config
@Autonomous(name = "Start at Classifier and Intake",group = "Red Auto")
public class RedClassifierStartIntakeBalls extends LinearOpMode {
    private boolean first = true;
    // Timer for our old lift mechanism - incorporate if necessary
   // ElapsedTime liftTimer = new ElapsedTime();

    @Override
    public void runOpMode() throws InterruptedException {
       // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(-47, 50, Math.toRadians(135));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);


        // actionBuilder builds from the drive steps passed to it
        TrajectoryActionBuilder toBasket = drive.actionBuilder(initialPose)
                .strafeToLinearHeading(new Vector2d(0,30),Math.toRadians(135))
                .strafeToLinearHeading(new Vector2d(-3.6,59),Math.toRadians(90))
                .waitSeconds(.5)
                .strafeToLinearHeading(new Vector2d(-34,30),Math.toRadians(135))
                .waitSeconds(.5);


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
                        firstTraj
                       //  toSub // push samples, go to submersible
                )
        );

        // add mechanism code below


    }

}



