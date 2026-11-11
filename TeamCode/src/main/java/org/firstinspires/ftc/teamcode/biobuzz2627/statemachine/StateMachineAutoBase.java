package org.firstinspires.ftc.teamcode.biobuzz2627.statemachine;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import java.util.Map;

/**
 * Iterative, nonblocking autonomous base with entry/exit hooks, timeouts, emergency reserve,
 * debug pauses, and loop profiling. Subclasses must never sleep inside {@link #runState}.
 */
public abstract class StateMachineAutoBase<S extends Enum<S>> extends OpMode {
    private static final long DEFAULT_AUTO_DURATION_MS = 30_000;

    private final StateMachineController<S> machine = new StateMachineController<>();
    private final LoopProfiler profiler = new LoopProfiler();
    private boolean pauseBetweenStates;
    private boolean waitingForDebugContinue;
    private boolean stateEntryPending;
    private boolean previousDebugToggle;
    private boolean previousDebugContinue;
    private boolean emergencyTriggered;
    private String failureReason = "";

    @Override
    public final void init() {
        onRobotInit();
    }

    @Override
    public final void init_loop() {
        boolean toggle = gamepad1.y;
        if (toggle && !previousDebugToggle) pauseBetweenStates = !pauseBetweenStates;
        previousDebugToggle = toggle;
        onRobotInitLoop();
        telemetry.addData("State debug", pauseBetweenStates
                ? "ON — press A between states" : "OFF — Y toggles");
        telemetry.update();
    }

    @Override
    public final void start() {
        machine.start(initialState());
        emergencyTriggered = false;
        waitingForDebugContinue = false;
        stateEntryPending = false;
        failureReason = "";
        onAutoStart();
        onStateEnter(machine.state());
    }

    @Override
    public final void loop() {
        profiler.beginLoop();
        enforceEmergencyReserve();

        if (waitingForDebugContinue) {
            stopRobotMotion();
            boolean continuePressed = gamepad1.a;
            if (continuePressed && !previousDebugContinue) {
                waitingForDebugContinue = false;
                if (stateEntryPending) {
                    stateEntryPending = false;
                    onStateEnter(machine.state());
                }
            }
            previousDebugContinue = continuePressed;
        } else {
            previousDebugContinue = gamepad1.a;
            profiler.beginSection("state");
            S loopState = machine.state();
            runState(loopState);
            profiler.endSection("state");
            if (machine.state() == loopState) machine.markLoopComplete();
        }

        addBaseTelemetry();
        addStateTelemetry();
        telemetry.update();
        profiler.endLoop();
    }

    @Override
    public final void stop() {
        stopRobotMotion();
        onAutoStop();
    }

    protected final void transitionTo(S nextState, String reason) {
        S oldState = machine.state();
        if (oldState == nextState) return;
        onStateExit(oldState);
        machine.transitionTo(nextState, reason);
        waitingForDebugContinue = pauseBetweenStates && !emergencyTriggered;
        stateEntryPending = waitingForDebugContinue;
        if (waitingForDebugContinue) {
            stopRobotMotion();
        } else {
            onStateEnter(nextState);
        }
    }

    protected final void fail(String reason) {
        failureReason = reason == null ? "unspecified failure" : reason;
        S failureState = failureState();
        if (failureState == null) {
            stopRobotMotion();
            throw new IllegalStateException(failureReason);
        }
        transitionTo(failureState, failureReason);
    }

    protected final S state() {
        return machine.state();
    }

    protected final long stateElapsedMillis() {
        return machine.stateElapsedMillis();
    }

    protected final long stateLoopCount() {
        return machine.stateLoopCount();
    }

    protected final String transitionReason() {
        return machine.transitionReason();
    }

    protected final boolean stateTimedOut(long timeoutMillis) {
        return !pauseBetweenStates && machine.stateTimedOut(timeoutMillis);
    }

    protected final long autoTimeRemainingMillis() {
        return Math.max(0, autonomousDurationMillis() - machine.totalElapsedMillis());
    }

    /** In debug mode, subclasses may multiply commanded power by this value. */
    protected final double debugPowerScale() {
        return pauseBetweenStates ? gamepad1.left_trigger : 1.0;
    }

    protected long autonomousDurationMillis() {
        return DEFAULT_AUTO_DURATION_MS;
    }

    /** Reserve this much time for {@link #emergencyState()}; zero disables deadline preemption. */
    protected long emergencyReserveMillis() {
        return 0;
    }

    protected S emergencyState() {
        return null;
    }

    protected S failureState() {
        return null;
    }

    protected abstract S initialState();
    protected abstract void onRobotInit();
    protected abstract void runState(S state);
    protected abstract void stopRobotMotion();

    protected void onRobotInitLoop() {}
    protected void onAutoStart() {}
    protected void onAutoStop() {}
    protected void onStateEnter(S state) {}
    protected void onStateExit(S state) {}
    protected void addStateTelemetry() {}

    private void enforceEmergencyReserve() {
        if (pauseBetweenStates) return;
        S emergency = emergencyState();
        long reserve = emergencyReserveMillis();
        if (!emergencyTriggered && emergency != null && reserve > 0
                && autoTimeRemainingMillis() <= reserve && machine.state() != emergency) {
            emergencyTriggered = true;
            waitingForDebugContinue = false;
            transitionTo(emergency, "emergency time reserve reached");
        }
    }

    private void addBaseTelemetry() {
        telemetry.addData("Auto state", machine.state());
        telemetry.addData("State reason", machine.transitionReason());
        telemetry.addData("State time", "%d ms", machine.stateElapsedMillis());
        telemetry.addData("Auto remaining", "%.1f s", autoTimeRemainingMillis() / 1000.0);
        telemetry.addData("Loop rate", "%.1f Hz", profiler.loopsPerSecond());
        telemetry.addData("Debug pause", waitingForDebugContinue
                ? "PAUSED — press A" : pauseBetweenStates ? "armed" : "off");
        if (!failureReason.isEmpty()) telemetry.addData("Failure", failureReason);
        for (Map.Entry<String, Double> entry : profiler.sectionAveragesMillis().entrySet()) {
            telemetry.addData("Profile " + entry.getKey(), "%.3f ms", entry.getValue());
        }
    }
}
