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

import java.util.Timer;

//@Config
@Autonomous(name = "BlueLeftBasketThenPark")
public class BlueLeftBasketThenPark extends LinearOpMode {
    private boolean first = true;
    private double currLiftPos = 0.0;


    @Override
    public void runOpMode() throws InterruptedException {

        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(38, 62, Math.toRadians(279));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        Lift lift = new Lift(hardwareMap);
        lift.resetTimer();
        Claw claw = new Claw(hardwareMap);
        LiftPivot liftPivot = new LiftPivot(hardwareMap);

        // actionBuilder builds from the drive steps passed to it
        TrajectoryActionBuilder toBasket = drive.actionBuilder(initialPose)
                .lineToY(52)
                .turn(Math.toRadians(90))
                .lineToX(58)
                .turn(Math.toRadians(45))
                .strafeToLinearHeading(new Vector2d(62,58), Math.toRadians(0));



        TrajectoryActionBuilder toSub = toBasket.endTrajectory().fresh()
                .turn(Math.toRadians(45))
                .strafeToLinearHeading(new Vector2d(-45,-37), Math.toRadians(0))
                .strafeToLinearHeading(new Vector2d(-45,-15), Math.toRadians(0))
                .turn(Math.toRadians(90))
                .lineToX(-26);

        // ON INIT:
        //      Actions.runBlocking(claw.closeClaw());

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
                        firstTraj, // go to the basket, push samples, and then submersible
                        liftPivot.liftPivotUp(),
                        lift.liftUp(),
                        claw.openClaw(), // drop the sample
                        lift.liftDown(),
                        toSub, // push samples, go to submersible
                        liftPivot.liftPivotUp(),
                        lift.liftUpLittle(),
                        liftPivot.liftPivotDown()
                )
        );
    }

}