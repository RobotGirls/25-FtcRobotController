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

import org.firstinspires.ftc.teamcode.MecanumDrive;

//@Config
@Autonomous(name = "RED DECODE LM1",group = "Red Auto")
public class LM1RedAuto extends LinearOpMode {

    public final double FLYWHEEL_SPEED_LONG = -0.8;

    @Override
    public void runOpMode() throws InterruptedException {

        // telemetry.setAutoClear(false);
        // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(62, 20, Math.toRadians(180));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        Shooter shooter = new Shooter(hardwareMap);
        Intake intake = new Intake(hardwareMap);
        // actionBuilder builds from the drive steps passed to it

        TrajectoryActionBuilder toShoot = drive.actionBuilder(initialPose)
                .strafeToLinearHeading(new Vector2d(60,20),Math.toRadians(170));

        TrajectoryActionBuilder toIntakeBalls = drive.actionBuilder(initialPose)
                .strafeToLinearHeading(new Vector2d(36,20),Math.toRadians(90));

        TrajectoryActionBuilder intakeBalls = drive.actionBuilder(initialPose)
                .strafeToLinearHeading(new Vector2d(36,60),Math.toRadians(90));

        Action toFinalShoot = intakeBalls.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(60,20),Math.toRadians(170))
                .build();


        Action firstTraj = toShoot.build();
        Action secondTraj = toIntakeBalls.build();
        Action thirdTraj = intakeBalls.build();


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
                        shooter.shootArtifact(),
                        new ParallelAction(
                                shooter.shootArtifact(),
                                intake.intakeArtifact()
                        ),
                        secondTraj,
                        new ParallelAction(
                                thirdTraj,
                                intake.intakeArtifact()
                        ),
                        toFinalShoot,
                        shooter.artifactOut(),
                        shooter.shootArtifact(),
                        new ParallelAction(
                                shooter.shootArtifact(),
                                intake.intakeArtifact()
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
            return new ShootArtifact();
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
            return new ArtifactOut();
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
            return new IntakeArtifact();
        }
    }

}



