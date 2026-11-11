package org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;

public final class TransferStateMachine implements UpdatableMechanism {
    public enum State { IDLE, FEEDING, COMPLETE, FAULT }

    private final DcMotorEx motor;
    private final PieceSensor pieceSensor;
    private State state = State.IDLE;
    private long stateStarted;
    private boolean pieceWasPresent;
    private String fault = "";

    public TransferStateMachine(DcMotorEx motor, PieceSensor pieceSensor) {
        this.motor = motor;
        this.pieceSensor = pieceSensor;
    }

    public void feed() {
        AutomationConfig.requireMechanismsVerified();
        state = State.FEEDING;
        stateStarted = now();
        pieceWasPresent = pieceSensor.available() && pieceSensor.piecePresent();
        fault = "";
    }

    @Override
    public void update() {
        if (state != State.FEEDING) return;
        motor.setPower(AutomationConfig.TRANSFER_POWER);
        long elapsed = now() - stateStarted;

        if (Math.abs(motor.getCurrent(CurrentUnit.AMPS))
                > AutomationConfig.TRANSFER_MAX_CURRENT_AMPS) {
            fail("transfer over-current");
            return;
        }

        boolean sensorComplete = pieceSensor.available() && pieceWasPresent
                && !pieceSensor.piecePresent();
        boolean timedComplete = !pieceSensor.available()
                && !AutomationConfig.PIECE_SENSORS_VERIFIED
                && elapsed >= AutomationConfig.TRANSFER_MIN_FEED_MS;
        if (elapsed >= AutomationConfig.TRANSFER_MIN_FEED_MS
                && (sensorComplete || timedComplete)) {
            motor.setPower(0);
            state = State.COMPLETE;
        } else if (elapsed >= AutomationConfig.TRANSFER_TIMEOUT_MS) {
            fail("transfer timeout");
        }
    }

    public boolean complete() { return state == State.COMPLETE; }
    public State state() { return state; }

    @Override
    public void stop() {
        motor.setPower(0);
        if (state != State.FAULT && state != State.COMPLETE) state = State.IDLE;
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
