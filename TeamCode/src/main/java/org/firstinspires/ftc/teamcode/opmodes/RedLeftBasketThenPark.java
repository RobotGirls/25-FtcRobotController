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
@Autonomous(name = "RedLeftBasketThenPark")
public class RedLeftBasketThenPark extends LinearOpMode {
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
                .lineToY(-52)
                .turn(Math.toRadians(90))
                .lineToX(-58)
                .turn(Math.toRadians(45))
                .strafeTo(new Vector2d(-62,-55))
                .waitSeconds(1.5);


        Action toSub = toBasket.endTrajectory().fresh()
                // samples (push)
                .turn(Math.toRadians(45))
                .strafeTo(new Vector2d(-45,-55))
                .strafeTo(new Vector2d(-45,-15))
                .turn(Math.toRadians(90))
                .lineToX(-26)
                .build();

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