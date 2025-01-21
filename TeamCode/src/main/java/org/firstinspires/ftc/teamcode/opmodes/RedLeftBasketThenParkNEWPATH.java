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
        Pose2d initialPose = new Pose2d(-38, -62, Math.toRadians(179));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        Lift lift = new Lift(hardwareMap);
        Claw claw = new Claw(hardwareMap);
        LiftPivot liftPivot = new LiftPivot(hardwareMap);

        // actionBuilder builds from the drive steps passed to it
        TrajectoryActionBuilder toBasket = drive.actionBuilder(initialPose)
                .splineToConstantHeading(new Vector2d(-56,-57), Math.toRadians(225))
                .turn(Math.toRadians(45));

        TrajectoryActionBuilder toSampleOne = toBasket.endTrajectory().fresh()
                .setTangent(Math.toRadians(0))
                .splineToLinearHeading(new Pose2d(-36,-39, Math.toRadians(270)), Math.toRadians(225))
                .strafeTo(new Vector2d(-36, -10))
                .setTangent(Math.toRadians(180))
                .splineToConstantHeading(new Vector2d(-48,-15),Math.toRadians(225));

        TrajectoryActionBuilder toBasketAgain = toSampleOne.endTrajectory().fresh()
                .setTangent(Math.toRadians(270))
                .splineTo(new Vector2d(-56,-57), Math.toRadians(225));

        Action backToSub = toBasketAgain.endTrajectory().fresh()
                .setTangent(Math.toRadians(0))
                .splineToLinearHeading(new Pose2d(-43, -10,Math.toRadians(0)),Math.toRadians(225))
                .strafeTo(new Vector2d(-28,-10))
                .build();


        // ON INIT:
  //      Actions.runBlocking(claw.closeClaw())

        Action firstTraj = toBasket.build();
        Action secondTraj = toSampleOne.build();
        Action thirdTraj = toBasketAgain.build();

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
                        firstTraj,  // get to basket to drop preload
                        liftPivot.liftPivotUp(),
                        lift.liftUp(),
                        claw.openClaw(), // drop the sample
                        lift.liftDown(),
                        liftPivot.liftPivotDown(),
                        secondTraj, // get to the first sample to intake
                        lift.liftUpLittle(),
                        claw.closeClaw(), // lift out lift a little and intake the sample
                        lift.liftDown(),
                        thirdTraj, // get back to the basket to drop other sample
                        liftPivot.liftPivotUp(),
                        lift.liftUp(),
                        claw.openClaw(), // drop the sample
                        lift.liftDown(),
                        backToSub, // head back to the submersible to park
                        lift.liftUpLittle()

                )
        );
    }

}