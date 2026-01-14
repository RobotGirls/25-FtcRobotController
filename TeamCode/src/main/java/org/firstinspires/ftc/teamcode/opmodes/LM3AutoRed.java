package org.firstinspires.ftc.teamcode.opmodes;

// RR-specific imports

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.TankDrive;

//@Config
@Autonomous(name = "LM3 Red")
public class LM3AutoRed extends LinearOpMode {

    public final double FLYWHEEL_SPEED_LONG = -0.8;

    @Override
    public void runOpMode() throws InterruptedException {

        // telemetry.setAutoClear(false);
        // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(60, 0, Math.toRadians(180));
        TankDrive drive = new TankDrive(hardwareMap, initialPose);
        Intake intake= new Intake(hardwareMap);
        Shooter shooter = new Shooter(hardwareMap);

        // actionBuilder builds from the drive steps passed to it

        TrajectoryActionBuilder toShoot = drive.actionBuilder(new Pose2d(-52, -46, Math.toRadians(-130)))
                .setReversed(false)
                .lineToY(8);
        TrajectoryActionBuilder intakeBalls = drive.actionBuilder(new Pose2d(-52, -2, Math.toRadians(-130)))
                .turn(Math.toRadians(-30))
                .splineTo(new Vector2d(-10,50), Math.toRadians(90))
                .waitSeconds(0.1);
        TrajectoryActionBuilder backToShoot = drive.actionBuilder(new Pose2d(10, 50, Math.toRadians(-90)))
                .setReversed(true)
                .splineTo(new Vector2d(-12,8), Math.toRadians(-45));
        Action outOfZone = backToShoot.endTrajectory().fresh()
                .turn(Math.toRadians(-90))
                .lineToX(0)
                .build();


        Action firstTraj = toShoot.build();
        Action secondTraj = intakeBalls.build();
        Action fourthTraj = backToShoot.build();


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
                        firstTraj,
                        secondTraj,
                        new ParallelAction(
                                thirdTraj
                        )

                )
        );

    }
    public class Shooter {
        private DcMotor shooter;
        private ElapsedTime timer2;


        public Shooter(HardwareMap hardwareMap) {
            shooter = hardwareMap.get(DcMotor.class, "shooter");
            timer2 = new ElapsedTime();

        }

        public class ShootArtifact implements Action {

            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                if (!initialized) {
                    shooter.setPower(FLYWHEEL_SPEED_LONG);
                    initialized = true;
                    timer2.reset();
                }
                double timerValue = timer2.milliseconds();
                telemetry.addData("Shooter Timer",timerValue);
                telemetry.update();
                if (timer2.milliseconds() < 5000) {
                    return true;
                }
                else {
                    shooter.setPower(0);
                    return false;
                }
            }
        }
        public Action shootArtifact() {
            return new LM3AutoRed.Shooter.ShootArtifact();
        }

        public class ArtifactOut implements Action {

            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                if (!initialized) {
                    shooter.setPower(1);
                    initialized = true;
                    timer2.reset();
                }
                double timerValue = timer2.milliseconds();
                telemetry.addData("Shooter Timer",timerValue);
                telemetry.update();
                if (timer2.milliseconds() < 500) {
                    return true;
                }
                else {
                    shooter.setPower(0);
                    return false;
                }
            }
        }
        public Action artifactOut() {
            return new LM3AutoRed.Shooter.ArtifactOut();
        }
    }

    public class Intake {
        private DcMotor intake;
        private ElapsedTime timer1;


        public Intake(HardwareMap hardwareMap) {
            intake = hardwareMap.get(DcMotor.class, "intake");
            timer1 = new ElapsedTime();
        }

        public class IntakeArtifact implements Action {

            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                if (!initialized) {
                    intake.setPower(-1);
                    initialized = true;
                    timer1.reset();
                }
                double timerValue = timer1.milliseconds();
                telemetry.addData("Intake Timer",timerValue);
                telemetry.update();
                if (timerValue < 5000) {
                    return true;
                } else {
                    intake.setPower(0);
                    return false;
                }
            }
        }
        public Action intakeArtifact() {
            return new LM3AutoRed.Intake.IntakeArtifact();
        }
    }

}



