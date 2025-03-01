package org.firstinspires.ftc.teamcode.opmodes;

// RR-specific imports

import com.acmerobotics.roadrunner.Action;
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
                .waitSeconds(0.3)
                .strafeToLinearHeading(new Vector2d(-61,-60), Math.toRadians(225));

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
                        liftPivot.liftPivotUp(), // lift the pivot
                        lift.liftUp(), // lift the lift (w/ timer debug)
                        wrist.wristUp(),
                        claw.openClaw(), // drop the sample
                        lift.liftDown(), // lift down
                        liftPivot.liftPivotDown() // lift pivot down
                        /*
                        secondTraj, // get to the first sample to intake
                        lift.liftUpLittle(), // lift the lift slightly to touch the ground to reach sample
                        claw.openClaw(), // intake the sample
                        liftPivot.liftPivotDown(),
                        thirdTraj, // get back to the basket to drop other sample
                        liftPivot.liftPivotUp(),
                        lift.liftUpNoTimer(),
                        claw.closeClaw(), // drop the sample
                        lift.liftDown(), // lift down
                        // backToSub, // head back to the submersible to park
                        lift.liftUpLittle()
*/

                )
        );
    }

}