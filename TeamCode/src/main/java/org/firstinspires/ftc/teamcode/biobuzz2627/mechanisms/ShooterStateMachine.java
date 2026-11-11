package org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;

public final class ShooterStateMachine implements UpdatableMechanism {
    public enum State { IDLE, SPINNING_UP, READY, FAULT }

    private final DcMotorEx motor;
    private State state = State.IDLE;
    private long stateStarted;
    private long withinToleranceSince;
    private String fault = "";

    public ShooterStateMachine(DcMotorEx motor) {
        this.motor = motor;
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    public void spinUp() {
        AutomationConfig.requireMechanismsVerified();
        state = State.SPINNING_UP;
        stateStarted = now();
        withinToleranceSince = 0;
        fault = "";
    }

    @Override
    public void update() {
        if (state != State.SPINNING_UP && state != State.READY) return;
        motor.setVelocity(AutomationConfig.SHOOTER_TARGET_TICKS_PER_SECOND);
        if (Math.abs(motor.getCurrent(CurrentUnit.AMPS)) > AutomationConfig.SHOOTER_MAX_CURRENT_AMPS) {
            fail("shooter over-current");
            return;
        }

        double error = Math.abs(motor.getVelocity()
                - AutomationConfig.SHOOTER_TARGET_TICKS_PER_SECOND);
        if (error <= AutomationConfig.SHOOTER_VELOCITY_TOLERANCE) {
            if (withinToleranceSince == 0) withinToleranceSince = now();
            if (now() - withinToleranceSince >= AutomationConfig.SHOOTER_SETTLE_MS) {
                state = State.READY;
            }
        } else {
            withinToleranceSince = 0;
            state = State.SPINNING_UP;
        }

        if (now() - stateStarted >= AutomationConfig.SHOOTER_SPINUP_TIMEOUT_MS
                && state != State.READY) fail("shooter spin-up timeout");
    }

    public boolean ready() { return state == State.READY; }
    public State state() { return state; }

    @Override
    public void stop() {
        motor.setPower(0);
        if (state != State.FAULT) state = State.IDLE;
    }

    private void fail(String reason) {
        fault = reason;
        state = State.FAULT;
        motor.setPower(0);
    }

    @Override public boolean faulted() { return state == State.FAULT; }
    @Override public String status() { return faulted() ? fault : state.name(); }
    private static long now() { return System.nanoTime() / 1_000_000L; }
}
