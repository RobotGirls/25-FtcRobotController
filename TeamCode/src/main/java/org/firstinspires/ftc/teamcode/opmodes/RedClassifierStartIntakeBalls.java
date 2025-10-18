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
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.MecanumDrive;

import java.util.Timer;

//@Config
@Autonomous(name = "Start at Classifier and Intake",group = "Red Auto")
public class RedClassifierStartIntakeBalls extends LinearOpMode {
    private boolean first = true;


    @Override
    public void runOpMode() throws InterruptedException {
       // liftTimer.reset();
        // instantiating the robot at a specific pose
        Pose2d initialPose = new Pose2d(-47, 50, Math.toRadians(135));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        Shooter shooter = new Shooter(hardwareMap);
        Intake intake = new Intake(hardwareMap);
        Transfer transfer = new Transfer(hardwareMap);
        // actionBuilder builds from the drive steps passed to it
        TrajectoryActionBuilder toBasket = drive.actionBuilder(initialPose)
                .strafeToLinearHeading(new Vector2d(0,30),Math.toRadians(135))
                .strafeToLinearHeading(new Vector2d(-3.6,59),Math.toRadians(90));



        Action toSub = toBasket.endTrajectory().fresh()
                .strafeToLinearHeading(new Vector2d(-34,30),Math.toRadians(135))
                .waitSeconds(.5)
                .build();

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
                        new ParallelAction(
                                shooter.shootArtifact(),
                                transfer.transferArtifact(),
                                intake.intakeArtifact()
                        ),
                        firstTraj,
                        new ParallelAction(
                                intake.intakeArtifact(),
                                transfer.transferArtifact()
                        ),
                        toSub,
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
        private ElapsedTime timer;


        public Shooter(HardwareMap hardwareMap) {
            shooter = hardwareMap.get(DcMotor.class, "shooter");
            timer = new ElapsedTime();

        }

        public class ShootArtifact implements Action {

            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                if (!initialized) {
                    shooter.setPower(-1);
                    initialized = true;
                }

                if (timer.milliseconds() < 3000) {
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
    }

    public class Intake {
        private DcMotor intake;
        private ElapsedTime timer;


        public Intake(HardwareMap hardwareMap) {
            intake = hardwareMap.get(DcMotor.class, "intake");
            timer = new ElapsedTime();

        }

        public class IntakeArtifact implements Action {

            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                if (!initialized) {
                    intake.setPower(-1);
                    initialized = true;
                }

                if (timer.milliseconds() < 5000) {
                    return true;
                }
                else {
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
                }

                if (timer.milliseconds() < 3000) {
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



