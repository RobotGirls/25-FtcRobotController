package org.firstinspires.ftc.teamcode.opmodes;


import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Lift{
    private DcMotorEx lift;
    ElapsedTime liftTimer = new ElapsedTime();
    private final double LIFT_PWR_OFF = 0.0;
    private final double LIFT_PWR_FAST = 1.0;
    private final double LIFT_PWR_SLOWER = 0.8;
    private final double POS_HIGH_BASKET = 3150.0;
    private final double POS_LIFT_TO_RUNG = 570.0;
    private final double POS_LIFT_DOWN = 100.0;
    private final double MAX_LIFT_TIMER = 10.0;

    public Lift(HardwareMap hardwareMap) {
        lift = hardwareMap.get(DcMotorEx.class, "lift");
        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //lift.setDirection(DcMotorSimple.Direction.REVERSE);

        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        liftTimer.reset();
    }

    public class LiftUp implements Action {
        // checks if the lift motor has been powered on
        private boolean initialized = false;

        // actions are formatted via telemetry packets as below
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {

            // powers on motor, if it is not on
            if (!initialized) {
                lift.setPower(LIFT_PWR_FAST);
                initialized = true;
            }
            // checks lift's current position
            double pos = lift.getCurrentPosition();
            packet.put("liftPos", pos);
            if (pos < POS_HIGH_BASKET && liftTimer.seconds() < MAX_LIFT_TIMER) {
                // true causes the action to rerun
                return true;
            } else {
                // false stops action rerun
                lift.setPower(LIFT_PWR_OFF);
                return false;
            }
            // overall, the action powers the lift until it surpasses
            // 3000 encoder ticks, then powers it off
        }
        // overall, the action powers the lift until it surpasses
        // 3000 encoder ticks, then powers it off

    }


    public Action liftUp() {
        return new LiftUp();
    }
    public void resetTimer() {
        liftTimer.reset();
    }


    public class LiftUpNoTimer implements Action {
        // checks if the lift motor has been powered on
        private boolean initialized = false;

        // actions are formatted via telemetry packets as below
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {

            // powers on motor, if it is not on
            if (!initialized) {
                lift.setPower(LIFT_PWR_FAST);
                initialized = true;
            }
            // checks lift's current position
            double pos = lift.getCurrentPosition();
            packet.put("liftPos", pos);
            if (pos < POS_HIGH_BASKET) {
                // true causes the action to rerun
                return true;
            } else {
                // false stops action rerun
                lift.setPower(LIFT_PWR_OFF);
                return false;
            }
            // overall, the action powers the lift until it surpasses
            // 3000 encoder ticks, then powers it off
        }


    }

    public Action liftUpNoTimer() {
        return new LiftUpNoTimer();
    }


    public class LiftUpLittle implements Action {
        // checks if the lift motor has been powered on
        private boolean initialized = false;

        // actions are formatted via telemetry packets as below
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            // powers on motor, if it is not on
            if (!initialized) {
                lift.setPower(LIFT_PWR_FAST);
                initialized = true;
            }
            // checks lift's current position
            double pos = lift.getCurrentPosition();
            packet.put("liftPos", pos);
            if (pos < POS_LIFT_TO_RUNG) {
                // true causes the action to rerun
                return true;
            } else {
                // false stops action rerun
                lift.setPower(LIFT_PWR_OFF);
                return false;
            }
            // overall, the action powers the lift until it surpasses
            // 3000 encoder ticks, then powers it off
        }

    }
    public Action liftUpLittle() {
        return new LiftUpLittle();
    }

    public class LiftDown implements Action {
        private boolean initialized = false;

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            if (!initialized) {
                lift.setPower(-1.0);
                initialized = true;
            }

            double pos = lift.getCurrentPosition();
            if (pos > POS_LIFT_DOWN) {
                // true causes the action to rerun
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


