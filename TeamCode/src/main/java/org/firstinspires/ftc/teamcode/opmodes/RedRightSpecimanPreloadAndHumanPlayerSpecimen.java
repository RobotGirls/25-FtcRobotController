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
@Autonomous(name = "Red Right Specimen NEWPATH")
public class RedRightSpecimanPreloadAndHumanPlayerSpecimen extends LinearOpMode {
    private boolean first = true;
    private static final double FIRST_LIFT_DOWN_POS = 50.0;
    private static final double LAST_LIFT_DOWN_POS = 100.0;
    private double currLiftPos = 0.0;

    @Override
    public void runOpMode() throws InterruptedException {
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(8, -60, Math.toRadians(90));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        Lift lift = new Lift(hardwareMap);
        Claw claw = new Claw(hardwareMap);
        LiftPivot liftPivot = new LiftPivot(hardwareMap);

        // actionBuilder builds from the drive steps passed to it
        TrajectoryActionBuilder toChamber = drive.actionBuilder(initialPose)
                .lineToY(-33.5);

        TrajectoryActionBuilder toWall = toChamber.endTrajectory().fresh()
                .strafeTo(new Vector2d(34,-33.5))
                .strafeTo(new Vector2d(40,-4))
                .strafeTo(new Vector2d(45,-54))
                .turn(Math.toRadians(180))
                .strafeTo(new Vector2d(45, -59));

        TrajectoryActionBuilder toChamberTwo = toWall.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(8,-50),Math.toRadians(89))
                .strafeTo(new Vector2d(8,-34));

        Action park = toChamberTwo.endTrajectory().fresh()
                .strafeTo(new Vector2d(8,-34)) // NOT UPDATED
                .build();

        Actions.runBlocking(liftPivot.liftPivotUpInit());

        Action firstTraj = toChamber.build();
        Action getSpec = toWall.build();
        Action scoreSpec2 = toChamberTwo.build();


        while (!isStopRequested() && !opModeIsActive()) {
            telemetry.addData("Robot position: ", drive.updatePoseEstimate());
            telemetry.update();
        }
        waitForStart();
        if (isStopRequested()) return;

        // IN RUNTIME
        // running the action sequence
        Actions.runBlocking(
                new SequentialAction(
//                        liftPivot.liftPivotDown(),
                        firstTraj, // go to the chamber, push sample, park in observation zone
                        liftPivot.liftPivotUp(),
                        lift.liftUpLittle(),
                        //liftPivot.liftPivotDown(),
                        lift.liftDown(),
                        getSpec,
                        lift.liftUpLittle(),
                        claw.closeClaw(),
                        lift.liftDown(),
                        scoreSpec2,
                        liftPivot.liftPivotUp(),
                        lift.liftUpLittle(),
                        lift.liftDown(),
                        liftPivot.liftPivotDown(),
                        park
                )
        );
    }

}