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
        // now presenting: concurrance!!! (hopefully0
        Actions.runBlocking(
                new SequentialAction(
                        new ParallelAction(
                                firstTraj,
                                liftPivot.liftPivotUp(),
                                lift.liftUpNoTimer()
                                ), // get to basket to drop preload, lift the pivot, lift the lift (w/ timer debug)
                        claw.closeClaw(),
                        // drop the sample
                        new ParallelAction(
                                  claw.closeClaw(),
                                  lift.liftDown(),
                                  liftPivot.liftPivotDown()
                          ), // lift down, lift pivot down
                        new ParallelAction(
                                secondTraj,
                                lift.liftUpLittle()
                        ), // get to the first sample to intake, lift the lift slightly to touch the ground to reach sample, intake the sample
                        claw.openClaw(),
                        lift.liftDown(),
                        new ParallelAction(
                                thirdTraj,
                                liftPivot.liftPivotUp(),
                                lift.liftUpNoTimer()
                        ),
                        // get back to the basket to drop other sample
                        claw.closeClaw(), // drop the sample
                        new ParallelAction(
                                lift.liftDown(), // lift down
                                backToSub
                        ), // lift down, head back to the submersible to park
                         lift.liftUpLittle()
                )
        );
    }

}