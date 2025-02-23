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
@Autonomous(name = "RedLeftBasketFourSamples")
public class RedLeftBasketFourSamples extends LinearOpMode {
    private boolean first = true;
    private double currLiftPos = 0.0;



    @Override
    public void runOpMode() throws InterruptedException {

        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(-38, -62, Math.toRadians(89));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        Lift lift = new Lift(hardwareMap);
        lift.resetTimer();
        Claw claw = new Claw(hardwareMap);
        LiftPivot liftPivot = new LiftPivot(hardwareMap);

        // actionBuilder builds from the drive steps passed to it
        TrajectoryActionBuilder toBasket = drive.actionBuilder(initialPose)
                .strafeToLinearHeading(new Vector2d(-62,-55), Math.toRadians(-135))
                .waitSeconds(1.5);

        TrajectoryActionBuilder toSample1 = toBasket.endTrajectory().fresh()
                // samples (push)
                .strafeToLinearHeading(new Vector2d(-48,-40), Math.toRadians(90))
                .waitSeconds(1.5);

        TrajectoryActionBuilder toBasket1 = toSample1.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(-62,-55), Math.toRadians(-135))
                .waitSeconds(1.5);

        TrajectoryActionBuilder toSample2 = toBasket1.endTrajectory().fresh()
                // samples (push)
                .strafeToLinearHeading(new Vector2d(-58,-40), Math.toRadians(90))
                .waitSeconds(1.5);

        TrajectoryActionBuilder toBasket2 = toSample2.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(-62,-55), Math.toRadians(-135))
                .waitSeconds(1.5);

        TrajectoryActionBuilder toSample3 = toBasket2.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(-64,-40), Math.toRadians(115))
                .waitSeconds(1.5);

        TrajectoryActionBuilder toBasket3 = toSample3.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(-62,-55), Math.toRadians(-135))
                .waitSeconds(1.5);

        // ON INIT:
  //      Actions.runBlocking(claw.closeClaw());
        Action firstTraj = toBasket.build();
        Action toSampleOne = toSample1.build();
        Action toSampleTwo = toSample2.build();
        Action toSampleThree = toSample3.build();
        Action toBasketOne = toBasket1.build();
        Action toBasketTwo = toBasket2.build();
        Action toBasketThree = toBasket3.build();

        Actions.runBlocking(
                new SequentialAction(
                        firstTraj, // go to the basket
                        liftPivot.liftPivotUp(),
                        lift.liftUp(),
                        claw.openClaw(), // drop the sample
                        lift.liftDown(),
                        toSampleOne, // goes to the first neutral field sample
                        liftPivot.liftPivotDown(),
                        lift.liftUpLittle(),
                        claw.closeClaw(),
                        toBasketOne, //goes to basket
                        liftPivot.liftPivotUp(),
                        lift.liftUp(),
                        claw.openClaw(), // drop the sample
                        lift.liftDown(),
                        toSampleTwo, // goes to the second neutral field sample
                        liftPivot.liftPivotDown(),
                        lift.liftUpLittle(),
                        claw.closeClaw(),
                        toBasketTwo, //goes to basket
                        liftPivot.liftPivotUp(),
                        lift.liftUp(),
                        claw.openClaw(), // drop the sample
                        lift.liftDown(),
                        toSampleThree, // goes to the third neutral field sample
                        liftPivot.liftPivotDown(),
                        lift.liftUpLittle(),
                        claw.closeClaw(),
                        toBasketThree, //goes to basket
                        liftPivot.liftPivotUp(),
                        lift.liftUp(),
                        claw.openClaw(), // drop the sample
                        lift.liftDown(),
                        liftPivot.liftPivotDown() //random comment
                )
        );

    }

}