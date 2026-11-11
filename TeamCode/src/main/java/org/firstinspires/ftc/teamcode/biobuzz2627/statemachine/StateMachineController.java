package org.firstinspires.ftc.teamcode.biobuzz2627.statemachine;

/**
 * Small, hardware-independent state tracker for iterative FTC OpModes.
 * States are changed explicitly and all timing uses a monotonic clock.
 */
public final class StateMachineController<S extends Enum<S>> {
    private S currentState;
    private S previousState;
    private String transitionReason = "not started";
    private long machineStartedNanos;
    private long stateStartedNanos;
    private long stateLoopCount;
    private boolean started;

    public void start(S initialState) {
        requireState(initialState);
        long now = System.nanoTime();
        currentState = initialState;
        previousState = null;
        transitionReason = "initial state";
        machineStartedNanos = now;
        stateStartedNanos = now;
        stateLoopCount = 0;
        started = true;
    }

    /** Returns the state that was active before this transition. */
    public S transitionTo(S nextState, String reason) {
        requireStarted();
        requireState(nextState);
        S oldState = currentState;
        previousState = oldState;
        currentState = nextState;
        transitionReason = reason == null || reason.isEmpty() ? "unspecified" : reason;
        stateStartedNanos = System.nanoTime();
        stateLoopCount = 0;
        return oldState;
    }

    public void markLoopComplete() {
        requireStarted();
        stateLoopCount++;
    }

    public S state() {
        requireStarted();
        return currentState;
    }

    public S previousState() {
        requireStarted();
        return previousState;
    }

    public String transitionReason() {
        requireStarted();
        return transitionReason;
    }

    public long stateLoopCount() {
        requireStarted();
        return stateLoopCount;
    }

    public long stateElapsedMillis() {
        requireStarted();
        return elapsedMillis(stateStartedNanos);
    }

    public long totalElapsedMillis() {
        requireStarted();
        return elapsedMillis(machineStartedNanos);
    }

    public boolean stateTimedOut(long timeoutMillis) {
        if (timeoutMillis < 0) throw new IllegalArgumentException("Timeout cannot be negative");
        return stateElapsedMillis() >= timeoutMillis;
    }

    public boolean started() {
        return started;
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private static void requireState(Object state) {
        if (state == null) throw new IllegalArgumentException("State cannot be null");
    }

    private void requireStarted() {
        if (!started) throw new IllegalStateException("State machine has not started");
    }
}
