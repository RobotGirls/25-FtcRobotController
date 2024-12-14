package org.firstinspires.ftc.teamcode.opmodes;


import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.RNRRMecanumDrive;

@Config
@Autonomous(name = "AutoTemplate" , group = "Autonomous")
public class AutoTemplate extends LinearOpMode {
    RNRRMecanumDrive drive;
    Pose2d initialPose;


    @Override
    public void runOpMode() throws InterruptedException {
        // instantiate your MecanumDrive at a particular pose.
        initialPose = new Pose2d(11.8, 61.7, Math.toRadians(90));
        drive = new RNRRMecanumDrive(hardwareMap, initialPose);
        // make a Claw instance
        Claw claw = new Claw(hardwareMap);
        // make a Lift instance
        Lift lift = new Lift(hardwareMap);

        TrajectoryActionBuilder tab1 = drive.actionBuilder(initialPose)
                .lineToYSplineHeading(33, Math.toRadians(0))
                .waitSeconds(2)
                .setTangent(Math.toRadians(90))
                .lineToY(48)
                .setTangent(Math.toRadians(0))
                .lineToX(32)
                .strafeTo(new Vector2d(44.5, 30))
                .turn(Math.toRadians(180))
                .lineToX(47.5)
                .waitSeconds(3);

        TrajectoryActionBuilder tab2 = drive.actionBuilder(initialPose)
                .lineToY(37)
                .setTangent(Math.toRadians(0))
                .lineToX(18)
                .waitSeconds(3)
                .setTangent(Math.toRadians(0))
                .lineToXSplineHeading(46, Math.toRadians(180))
                .waitSeconds(3);
        TrajectoryActionBuilder tab3 = drive.actionBuilder(initialPose)
                .lineToYSplineHeading(33, Math.toRadians(180))
                .waitSeconds(2)
                .strafeTo(new Vector2d(46, 30))
                .waitSeconds(3);

        Action trajectoryActionCloseOut = tab1.endTrajectory().fresh()
                .strafeTo(new Vector2d(48, 12))
                .build();

        Actions.runBlocking(claw.closeClaw());

        while (!isStopRequested() && !opModeIsActive()) {
            telemetry.update();
        }
        telemetry.update();
        waitForStart();

        if (isStopRequested()) return;

        Actions.runBlocking(
                new SequentialAction(

                        lift.liftUp(),
                        claw.openClaw(),
                        lift.liftDown(),
                        trajectoryActionCloseOut
                )
        );

    }






    public class Lift {
        private DcMotorEx lift;

        public Lift(HardwareMap hardwareMap) {

            lift = hardwareMap.get(DcMotorEx.class, "liftMotor");
            lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            lift.setDirection(DcMotorSimple.Direction.FORWARD);
        }

        public class LiftUp implements Action {
            public boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                if (!initialized) {
                    lift.setPower(0.8);
                    boolean initizalized = true;
                }

                double pos =  lift.getCurrentPosition();
                packet.put("liftPos", pos);
                if(pos < 3000.0) {
                    return true;
                } else {
                    lift.setPower(0);
                    return false;
                }
            }
        }
        public Action liftUp() {
            return new LiftUp();
        }

        public class LiftDown implements Action {
            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                if (!initialized) {
                    lift.setPower(-0.8);
                    initialized = true;
                }

                double pos = lift.getCurrentPosition();
                packet.put("liftPos", pos);
                if (pos > 100.0) {
                    return true;
                } else {
                    lift.setPower(0);
                    return false;
                }
            }
        }
            public Action liftDown() {
                return new LiftDown();
            }



    }
    public class Claw {
        private Servo claw;

        public Claw(HardwareMap hardwareMap) {
            claw = hardwareMap.get(Servo.class, "claw");
        }

        public class CloseClaw implements Action {
            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                claw.setPosition(0.55);
                return false;
            }
        }

        public Action closeClaw() {
            return new CloseClaw();
        }


        public class OpenClaw implements Action {
            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                claw.setPosition(1.0);
                return false;
            }
        }

        public Action openClaw() {
            return new OpenClaw();
        }
    }


}
