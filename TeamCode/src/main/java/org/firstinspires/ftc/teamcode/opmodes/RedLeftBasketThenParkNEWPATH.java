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

import org.firstinspires.ftc.teamcode.MecanumDrive;

//@Config
@Autonomous(name = "RedLeftBasketThenParkNEWPATH")
public class RedLeftBasketThenParkNEWPATH extends LinearOpMode {
    private boolean first = true;
    private double currLiftPos = 0.0;

    @Override
    public void runOpMode() throws InterruptedException {
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(-38, -62, Math.toRadians(89));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        Lift lift = new Lift(hardwareMap);
        Claw claw = new Claw(hardwareMap);
        LiftPivot liftPivot = new LiftPivot(hardwareMap);

        // actionBuilder builds from the drive steps passed to it
        TrajectoryActionBuilder toBasket = drive.actionBuilder(initialPose)
                .lineToY(-33.5)
                .turn(Math.toRadians(-90))
                .lineToX(34)
                .splineTo(new Vector2d(34,-4),Math.toRadians(0))
                .turn(Math.toRadians(-90))
                //  .strafeTo(new Vector2d(34,-4))
                .strafeTo(new Vector2d(48,-4))
                .strafeTo(new Vector2d(48,-58))
                .strafeTo(new Vector2d(48,-4))
                .strafeTo(new Vector2d(56,-4))
                .strafeTo(new Vector2d(56,-58))
                .strafeTo(new Vector2d(56,-4))
                .strafeTo(new Vector2d(61,-4))
                .strafeTo(new Vector2d(61,-58));




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
                        firstTraj // go to the basket, push samples, and then submersible
                        /*
                        liftPivot.liftPivotUp(),
                        lift.liftUp(),
                        claw.openClaw(), // drop the sample
                        lift.liftDown(),
                         // push samples, go to submersible
                        liftPivot.liftPivotUp(),
                        lift.liftUp()

                         */
                )
        );
    }

}