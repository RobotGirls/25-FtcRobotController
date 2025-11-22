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
@Autonomous(name = "BLUE DECODE",group = "Blue Auto")
public class LM1AutoBlue extends LinearOpMode {
    private boolean first = true;


    @Override
    public void runOpMode() throws InterruptedException {

        // telemetry.setAutoClear(false);
        // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(-63, -66, Math.toRadians(225));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        Shooter shooter = new Shooter(hardwareMap);
        Intake intake = new Intake(hardwareMap);
        Transfer transfer = new Transfer(hardwareMap);
        // actionBuilder builds from the drive steps passed to it
        TrajectoryActionBuilder lineUp = drive.actionBuilder(initialPose)
                .strafeToLinearHeading(new Vector2d(-12,20),Math.toRadians(225));

        TrajectoryActionBuilder lineUpIntake = drive.actionBuilder(new Pose2d(-19, -30, Math.toRadians(225)))
                .strafeToLinearHeading(new Vector2d(-12,-28),Math.toRadians(-90));

        TrajectoryActionBuilder toIntake = drive.actionBuilder(new Pose2d(-22, -58, Math.toRadians(270)))
                .strafeToLinearHeading(new Vector2d(-12,-56),Math.toRadians(-90));

        Action toSub = toIntake.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(-12,-28),Math.toRadians(-90))
                .strafeToLinearHeading(new Vector2d(-22,-25),Math.toRadians(225))
                .build();

        Action firstTraj = lineUp.build();
        Action secondTraj = lineUpIntake.build();
        Action thirdTraj = toIntake.build();

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
                                transfer.transferArtifact(),
                                intake.intakeArtifact()
                        ),
                        secondTraj,
                        new ParallelAction(
                                thirdTraj,
                                intake.intakeArtifact(),
                                transfer.transferArtifact()
                        ),
                        shooter.artifactOut(),
                        toSub,
                        shooter.shootArtifact(),
                        new ParallelAction(
                                shooter.shootArtifact(),
                                transfer.transferArtifact(),
                                intake.intakeArtifact()
                        )

                        //  toSub // push samples, go to submersible
                )
        );

        // add mechanism code below



    }

    public class Shooter {
        private DcMotor shooter;
        private ElapsedTime timer2;


        public Shooter(HardwareMap hardwareMap) {
            shooter = hardwareMap.get(DcMotor.class, "shooter");
            shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            timer2 = new ElapsedTime();
        }

        public class ShootArtifact implements Action {

            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                if (!initialized) {
                    shooter.setPower(MecanumDrive.FLYWHEEL_SPEED);
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
                if (timer2.milliseconds() < 900) {
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

    public class Transfer {
        private DcMotor transfer;
        private ElapsedTime timer;


        public Transfer(HardwareMap hardwareMap) {
            transfer = hardwareMap.get(DcMotor.class, "transfer");
            timer = new ElapsedTime();

        }

        public class TransferArtifact implements Action {

            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                if (!initialized) {
                    transfer.setPower(-1);
                    initialized = true;
                    timer.reset();
                }
                double timerValue = timer.milliseconds();
                telemetry.addData("Transfer Timer",timerValue);
                telemetry.update();
                if (timerValue < 3000) {
                    return true;
                }
                else {
                    transfer.setPower(0);
                    return false;
                }
            }
        }
        public Action transferArtifact() {
            return new TransferArtifact();
        }
    }

}



