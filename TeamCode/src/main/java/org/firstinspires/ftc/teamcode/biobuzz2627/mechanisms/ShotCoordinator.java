package org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms;

/** Interlocks transfer motion until both shooter velocity and Limelight range are ready. */
public final class ShotCoordinator implements UpdatableMechanism {
    public enum State { IDLE, SPINNING_UP, WAITING_FOR_RANGE, FEEDING, COMPLETE, FAULT }

    private final ShooterStateMachine shooter;
    private final TransferStateMachine transfer;
    private final ReadinessSignal range;
    private State state = State.IDLE;
    private String fault = "";

    public ShotCoordinator(ShooterStateMachine shooter, TransferStateMachine transfer,
                           ReadinessSignal range) {
        this.shooter = shooter;
        this.transfer = transfer;
        this.range = range;
    }

    public void requestShot() {
        if (shooter.ready()) {
            state = State.WAITING_FOR_RANGE;
        } else {
            shooter.spinUp();
            state = State.SPINNING_UP;
        }
        fault = "";
    }

    @Override
    public void update() {
        shooter.update();
        transfer.update();
        if (shooter.faulted() || transfer.faulted()) {
            fail(shooter.faulted() ? shooter.status() : transfer.status());
            return;
        }
        if (state == State.SPINNING_UP && shooter.ready()) state = State.WAITING_FOR_RANGE;
        if (state == State.WAITING_FOR_RANGE && range.ready()) {
            transfer.feed();
            state = State.FEEDING;
        }
        if (state == State.FEEDING && transfer.complete()) state = State.COMPLETE;
    }

    public boolean complete() { return state == State.COMPLETE; }
    public State state() { return state; }

    @Override
    public void stop() {
        shooter.stop();
        transfer.stop();
        if (state != State.FAULT) state = State.IDLE;
    }

    private void fail(String reason) {
        fault = reason;
        state = State.FAULT;
        shooter.stop();
        transfer.stop();
    }

    @Override public boolean faulted() { return state == State.FAULT; }
    @Override public String status() {
        return faulted() ? fault : state + " / " + range.description();
    }
}
