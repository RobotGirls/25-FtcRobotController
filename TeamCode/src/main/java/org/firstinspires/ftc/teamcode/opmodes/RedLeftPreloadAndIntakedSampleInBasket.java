package org.firstinspires.ftc.teamcode.opmodes;

// RR-specific imports

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.MecanumDrive;

//@Config
@Autonomous(name = "RedLeftBasketThenParkNEWPATH")
public class RedLeftPreloadAndIntakedSampleInBasket extends LinearOpMode {
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
        Wrist wrist = new Wrist(hardwareMap);

        // actionBuilder builds from the drive steps passed to it
        TrajectoryActionBuilder toBasket = drive.actionBuilder(initialPose)
                .strafeTo(new Vector2d(-38, -58))
                .strafeToLinearHeading(new Vector2d(-63,-61), Math.toRadians(225));

        TrajectoryActionBuilder toSampleOne = toBasket.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(-47,-44.6), Math.toRadians(90));

        TrajectoryActionBuilder toBasketAgain = toSampleOne.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(-57,-57.3), Math.toRadians(35));

        Action backToSub = toBasketAgain.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(-31,-10),Math.toRadians(0))
                .strafeToLinearHeading(new Vector2d(-25,-20),Math.toRadians(0))
                .build();


        // ON INIT:
  //      Actions.runBlocking(claw.closeClaw())
       // lift.setModeOnInit();


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
                        new ParallelAction(
                                firstTraj,
                                liftPivot.liftPivotUp()
                        ),
                         // lift the pivot
                        lift.liftUp(), // lift the lift (w/ timer debug)
                        wrist.wristUp(),
                        claw.openClaw(), // drop the sample
                        new ParallelAction(
                                wrist.wristOutOfTheWay(),
                                lift.liftDown()
                        ),
                        new ParallelAction(
                                liftPivot.liftPivotDown(), // lift pivot down
                                secondTraj // get to the first sample to intake
                        ),
                        liftPivot.liftPivotUpLittle(),
                        wrist.wristIntake(),
                        lift.liftUpLittle(), // lift the lift slightly to touch the ground to reach sample
                        liftPivot.liftPivotDown(),
                        claw.closeClaw(), // intake the sample
                        liftPivot.liftPivotUpLittle(),
                        thirdTraj, // get back to the basket to drop other sample
                        liftPivot.liftPivotUpBackward(),
                        lift.liftUpNoTimer(),
                        wrist.wristScore(),
                        claw.openClaw(), // drop the sample
                        wrist.wristUp(),
                        lift.liftDown(), // lift down
                        liftPivot.liftPivotDown()
                )
        );
    }

}