package org.firstinspires.ftc.teamcode.opmodes;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class LiftPivot {
    private DcMotorEx liftPivot;
    private final double LIFT_PIVOT_POWER_UP = 1.0;
    private final double LIFT_PIVOT_POWER_DOWN = -1.0;
    private final double LIFT_PIVOT_POWER_OFF = 0.0;
    private final double POS_LIFT_PIVOT_UP = 2000.0;
    private final double POS_LIFT_PIVOT_UP_CHAMBER = 2400.0;
    private final double POS_LIFT_PIVOT_DOWN = 500.0;
    private final double POS_LIFT_PIVOT_UP_LITTLE = 550.0;
    private final double POS_LIFT_PIVOT_UP_LITTLE_SPEC = 2200.0;
    private final double POS_LIFT_PIVOT_UP_INIT = 1000;

    public LiftPivot(HardwareMap hardwareMap) {
        liftPivot = hardwareMap.get(DcMotorEx.class, "liftPivot");
        liftPivot.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        liftPivot.setDirection(DcMotorSimple.Direction.REVERSE);

        liftPivot.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        liftPivot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

    }

    public class LiftPivotUp implements Action {
        // checks if the lift motor has been powered on
        private boolean initialized = false;

        // actions are formatted via telemetry packets as below
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            // powers on motor, if it is not on
            if (!initialized) {
                liftPivot.setPower(LIFT_PIVOT_POWER_UP);
                initialized = true;
            }
            // checks lift's current position
            double pos = liftPivot.getCurrentPosition();
            packet.put("liftPivotPos", pos);
            if (pos < POS_LIFT_PIVOT_UP) {
                // true causes the action to rerun
                return true;
            } else {
                // false stops action rerun
                liftPivot.setPower(LIFT_PIVOT_POWER_OFF);
                return false;
            }
            // overall, the action powers the lift until it surpasses
            // 3000 encoder ticks, then powers it off
        }
    }
    public Action liftPivotUp() {
        return new LiftPivotUp();
    }

    public class LiftPivotUpChamber implements Action {
        // checks if the lift motor has been powered on
        private boolean initialized = false;

        // actions are formatted via telemetry packets as below
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            // powers on motor, if it is not on
            if (!initialized) {
                liftPivot.setPower(LIFT_PIVOT_POWER_UP);
                initialized = true;
            }
            // checks lift's current position
            double pos = liftPivot.getCurrentPosition();
            packet.put("liftPivotPos", pos);
            if (pos < POS_LIFT_PIVOT_UP_CHAMBER) {
                // true causes the action to rerun
                return true;
            } else {
                // false stops action rerun
                liftPivot.setPower(LIFT_PIVOT_POWER_OFF);
                return false;
            }
            // overall, the action powers the lift until it surpasses
            // 3000 encoder ticks, then powers it off
        }
    }
    public Action liftPivotUpChamber() {
        return new LiftPivotUpChamber();
    }

    public class LiftPivotUpLittle implements Action {
        // checks if the lift motor has been powered on
        private boolean initialized = false;

        // actions are formatted via telemetry packets as below
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            // powers on motor, if it is not on
            if (!initialized) {
                liftPivot.setPower(LIFT_PIVOT_POWER_UP);
                initialized = true;
            }
            // checks lift's current position
            double pos = liftPivot.getCurrentPosition();
            packet.put("liftPivotPos", pos);
            if (pos < POS_LIFT_PIVOT_UP_LITTLE) {
                // true causes the action to rerun
                return true;
            } else {
                // false stops action rerun
                liftPivot.setPower(LIFT_PIVOT_POWER_OFF);
                return false;
            }
            // overall, the action powers the lift until it surpasses
            // 3000 encoder ticks, then powers it off
        }
    }
    public Action liftPivotUpLittle() {
        return new LiftPivotUpLittle();
    }

    public class LiftPivotUpInit implements Action {
        // checks if the lift motor has been powered on
        private boolean initialized = false;

        // actions are formatted via telemetry packets as below
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            // powers on motor, if it is not on
            if (!initialized) {
                liftPivot.setPower(LIFT_PIVOT_POWER_UP);
                initialized = true;
            }
            // checks lift's current position
            double pos = liftPivot.getCurrentPosition();
            packet.put("liftPivotPos", pos);
            if (pos < POS_LIFT_PIVOT_UP_INIT) {
                // true causes the action to rerun
                return true;
            } else {
                // false stops action rerun
                liftPivot.setPower(LIFT_PIVOT_POWER_OFF);
                liftPivot.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                return false;
            }
            // overall, the action powers the lift until it surpasses
            // 3000 encoder ticks, then powers it off
        }
    }
    public Action liftPivotUpInit() {
        return new LiftPivotUpInit();
    }

    public class LiftPivotDown implements Action {
        private boolean initialized = false;

        // actions are formatted via telemetry packets as below
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            // powers on motor, if it is not on
            if (!initialized) {
                liftPivot.setPower(LIFT_PIVOT_POWER_DOWN);
                initialized = true;
            }
            // checks lift's current position
            double pos = liftPivot.getCurrentPosition();
            packet.put("liftPivotPos", pos);
            if (pos > POS_LIFT_PIVOT_DOWN) {
                // true causes the action to rerun
                return true;
            } else {
                // false stops action rerun
                liftPivot.setPower(LIFT_PIVOT_POWER_OFF);
                return false;
            }
            // overall, the action powers the lift until it surpasses
            // 3000 encoder ticks, then powers it off
        }
    }

    public Action liftPivotDown() {
        return new LiftPivotDown();
    }

    public class LiftPivotUpLittleSpec implements Action {
        // checks if the lift motor has been powered on
        private boolean initialized = false;

        // actions are formatted via telemetry packets as below
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            // powers on motor, if it is not on
            if (!initialized) {
                liftPivot.setPower(LIFT_PIVOT_POWER_DOWN);
                initialized = true;
            }
            // checks lift's current position
            double pos = liftPivot.getCurrentPosition();
            packet.put("liftPivotPos", pos);
            if (pos > POS_LIFT_PIVOT_UP_LITTLE_SPEC) {
                // true causes the action to rerun
                return true;
            } else {
                // false stops action rerun
                liftPivot.setPower(LIFT_PIVOT_POWER_OFF);
                return false;
            }
            // overall, the action powers the lift until it surpasses
            // 3000 encoder ticks, then powers it off
        }
    }
    public Action liftPivotUpLittleChamber() {
        return new LiftPivotUpLittleSpec();
    }

}
