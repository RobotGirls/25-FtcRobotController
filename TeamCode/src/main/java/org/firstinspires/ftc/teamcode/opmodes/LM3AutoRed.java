package org.firstinspires.ftc.teamcode.opmodes;

// RR-specific imports

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
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


        // actionBuilder builds from the drive steps passed to it

        TrajectoryActionBuilder toShoot = drive.actionBuilder(new Pose2d(-52, -46, Math.toRadians(-130)))
                .lineToY(-2)
                .turn(Math.toRadians(30))
                .splineToSplineHeading(new Pose2d(10,28, Math.toRadians(90)),-50)
                .waitSeconds(0.1)
                .splineToSplineHeading(new Pose2d(10,50, Math.toRadians(90)),-55)
                .waitSeconds(0.1)
                .splineToSplineHeading(new Pose2d(-16,2, Math.toRadians(130)),-55)
                .waitSeconds(0.1)
                .splineToSplineHeading(new Pose2d(20,20,Math.toRadians(0)),-55);


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



