package org.firstinspires.ftc.teamcode.opmodes;

// RR-specific imports

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.TankDrive;

//@Config
@Autonomous(name = "LM3 Blue")
public class LM3AutoBlue extends LinearOpMode {

    public final double FLYWHEEL_SPEED_LONG = -0.8;

    @Override
    public void runOpMode() throws InterruptedException {

        // telemetry.setAutoClear(false);
        // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(60, 0, Math.toRadians(180));
        TankDrive drive = new TankDrive(hardwareMap, initialPose);


        // actionBuilder builds from the drive steps passed to it

        TrajectoryActionBuilder toShoot = drive.actionBuilder(new Pose2d(-52, -46, Math.toRadians(-130)))
                .lineToY(-2)
                .turn(Math.toRadians(30))
                .splineToSplineHeading(new Pose2d(-10,-28, Math.toRadians(-90)),-50)
                .waitSeconds(0.1)
                .splineToSplineHeading(new Pose2d(-10,-50, Math.toRadians(-90)),-55)
                .waitSeconds(0.1)
                .splineToSplineHeading(new Pose2d(-16,-2, Math.toRadians(-130)),-55)
                .waitSeconds(0.1)
                .splineToSplineHeading(new Pose2d(20,-20,Math.toRadians(0)),-55);


        Action firstTraj = toShoot.build();


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
                        firstTraj

                )
        );

    }


}



