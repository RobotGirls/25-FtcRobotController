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
                .strafeTo(new Vector2d(-38, -60))
                .waitSeconds(0.3)
                .splineTo(new Vector2d(-51.2,-58.4), Math.toRadians(225));

        TrajectoryActionBuilder toSampleOne = toBasket.endTrajectory().fresh()
            .strafeToLinearHeading(new Vector2d(-36,-44.5), Math.toRadians(90));

        TrajectoryActionBuilder toBasketAgain = toSampleOne.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(-38,-57.5), Math.toRadians(225));

        Action backToSub = toBasketAgain.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(-31,-10),Math.toRadians(0))
                .strafeToLinearHeading(new Vector2d(-25,-20),Math.toRadians(0))
                .build();


        // ON INIT:
  //      Actions.runBlocking(claw.closeClaw())
       // lift.setModeOnInit();
        Actions.runBlocking(liftPivot.liftPivotUpInit());



        Action firstTraj = toBasket.build();
        Action secondTraj = toSampleOne.build();
        Action thirdTraj = toBasketAgain.build();

        while (!isStopRequested() && !opModeIsActive()) {

        }
        waitForStart();
        if (isStopRequested()) return;

        // IN RUNTIME
        // running the action sequence!
        Actions.runBlocking(
                new SequentialAction(
                        firstTraj,  // get to basket to drop preload
                        liftPivot.liftPivotUp(),
                        lift.liftUpNoTimer(),
                        claw.closeClaw(), // drop the sample
                        lift.liftDown(),
                        liftPivot.liftPivotDown(),
                        secondTraj, // get to the first sample to intake
                        liftPivot.liftPivotUpLittle(),
                        lift.liftUpLittle(),
                        claw.openClaw(), // lift out lift a little and intake the sample
                        liftPivot.liftPivotUpLittle(),
                        thirdTraj, // get back to the basket to drop other sample
                        liftPivot.liftPivotUpLittle(),
                        lift.liftDown(),
                        liftPivot.liftPivotUp(),
                        lift.liftUpNoTimer(),
                        claw.closeClaw(), // drop the sample
                        lift.liftDown()
                        //backToSub, // head back to the submersible to park
                        //lift.liftUpLittle()


                )
        );
    }

}