package org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;

public final class IntakeStateMachine implements UpdatableMechanism {
    public enum State { IDLE, INTAKING, REVERSING, LOADED, FAULT }

    private final DcMotorEx motor;
    private final PieceSensor loadedSensor;
    private State state = State.IDLE;
    private long stateStarted;
    private String fault = "";

    public IntakeStateMachine(DcMotorEx motor, PieceSensor loadedSensor) {
        this.motor = motor;
        this.loadedSensor = loadedSensor;
    }

    public void intake() {
        AutomationConfig.requireMechanismsVerified();
        state = State.INTAKING;
        stateStarted = now();
        fault = "";
    }

    public void reverse() {
        AutomationConfig.requireMechanismsVerified();
        state = State.REVERSING;
        stateStarted = now();
        fault = "";
    }

    @Override
    public void update() {
        if (state == State.INTAKING) {
            motor.setPower(AutomationConfig.INTAKE_POWER);
            if (loadedSensor.available() && loadedSensor.piecePresent()) {
                motor.setPower(0);
                state = State.LOADED;
            } else if (now() - stateStarted >= AutomationConfig.INTAKE_TIMEOUT_MS) {
                fail("intake timeout");
            }
        } else if (state == State.REVERSING) {
            motor.setPower(AutomationConfig.INTAKE_REVERSE_POWER);
        }

        if ((state == State.INTAKING || state == State.REVERSING)
                && Math.abs(motor.getCurrent(CurrentUnit.AMPS))
                > AutomationConfig.INTAKE_MAX_CURRENT_AMPS) {
            fail("intake over-current");
        }
    }

    public boolean loaded() { return state == State.LOADED; }
    public State state() { return state; }

    @Override
    public void stop() {
        motor.setPower(0);
        if (state != State.FAULT && state != State.LOADED) state = State.IDLE;
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
