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
                .lineToY(-52)
                .turn(Math.toRadians(90))
                .lineToX(-52)
                .turn(Math.toRadians(45));

        TrajectoryActionBuilder sampleOne = toBasket.endTrajectory().fresh()
                .turn(Math.toRadians(45))
                //         .splineTo(new Vector2d(-35,-52),180) too wavy .
                .strafeTo(new Vector2d(-35,-52))
                .strafeTo(new Vector2d(-35,-10))
                .strafeTo(new Vector2d(-46,-10));

        TrajectoryActionBuilder toBasketAgain = sampleOne.endTrajectory().fresh()
                .splineTo(new Vector2d(-52,-52),Math.toRadians(-90))
                .turn(Math.toRadians(-45));

        TrajectoryActionBuilder sampleTwo = toBasketAgain.endTrajectory().fresh()
                .lineToX(-48)
                .splineTo(new Vector2d(-35,-10),Math.toRadians(0))
                //   .lineToY(-10)
                .lineToX(-54)
                .turn(Math.toRadians(90));

        TrajectoryActionBuilder finalToBasket = sampleTwo.endTrajectory().fresh()
                .splineTo(new Vector2d(-52,-52),Math.toRadians(-90))
                .turn(Math.toRadians(-45));

        Action backToSub = finalToBasket.endTrajectory().fresh()
                .turn(Math.toRadians(90))
                .splineTo(new Vector2d(-24,-5),Math.toRadians(0))
                .build();


        // ON INIT:
  //      Actions.runBlocking(claw.closeClaw())

        Action firstTraj = toBasket.build();
        Action secondTraj = sampleOne.build();
        Action thirdTraj = toBasketAgain.build();
        Action fourthTraj = sampleTwo.build();
        Action fifthTraj = finalToBasket.build();


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
                        liftPivot.liftPivotDown(),
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
                        liftPivot.liftPivotDown(),
                        fourthTraj, // get to the next sample to intake
                        lift.liftUpLittle(),
                        claw.closeClaw(), // lift out lift a little and intake the sample
                        lift.liftDown(),
                        fifthTraj, // go to the basket one final time to prepare to drop the sample
                        liftPivot.liftPivotUp(),
                        lift.liftUp(),
                        claw.openClaw(), // drop the sample
                        lift.liftDown(),
                        liftPivot.liftPivotDown(),
                        backToSub, // head back to the submersible to park
                        liftPivot.liftPivotUp() // level 1 ascent

                )
        );
    }

}