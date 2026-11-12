package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.biobuzz2627.PurePursuit.PurePursuitDrive;
import org.firstinspires.ftc.teamcode.purepursuit.Route;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * State-machine base for pure pursuit autonomous OpModes.
 *
 * <p>The child OpMode owns a typed enum and advances it with {@link #nextState(Enum)}. Each
 * state's Action is ticked once per OpMode loop instead of blocking the whole autonomous. Route
 * definitions still live in the PurePursuit module so the FieldViewer can simulate the same
 * paths.</p>
 */
public abstract class PurePursuitAutoBase<S extends Enum<S>> extends LinearOpMode {
    protected PurePursuitDrive drive;

    /** True until the current state's entry block has initialized its variables and Action. */
    protected boolean stageFinished = true;
    protected long stateStartTime;
    protected long programStartTime;

    private Route activeRoute;
    private Map<String, Action> markerActions = Collections.emptyMap();
    private Action stateAction;
    private S programState;

    protected abstract Route route();

    /** First state entered when the driver presses Play. */
    protected abstract S initialState();

    /** Called once per OpMode loop. Implement this as a switch over {@link #getState()}. */
    protected abstract void mainStateMachine();

    /** Mechanism Actions to run at the route's marker steps, by marker name. */
    protected Map<String, Action> markers() {
        return Collections.emptyMap();
    }

    /** Set up mechanism hardware (called during init). */
    protected void initMechanisms() {}

    /** Turn mechanisms off; always called when the OpMode ends, even if stopped early. */
    protected void stopMechanisms() {}

    protected final S getState() {
        return programState;
    }

    protected final Route getRoute() {
        return activeRoute;
    }

    protected final long stateElapsedMillis() {
        return nowMillis() - stateStartTime;
    }

    protected final long programElapsedMillis() {
        return nowMillis() - programStartTime;
    }

    /** Matches Gluten Free's state entry pattern for direct mechanism-control states. */
    protected final void initializeStateVariables() {
        if (!stageFinished) {
            throw new IllegalStateException("State " + programState + " is already initialized");
        }
        stageFinished = false;
        stateStartTime = nowMillis();
        stateAction = null;
    }

    /** State entry for a route step or other nonblocking Action. */
    protected final void initializeStateVariables(Action action) {
        initializeStateVariables();
        stateAction = action;
    }

    /** Runs the current state's Action once. Returns true on the loop where it finishes. */
    protected final boolean updateStateAction() {
        if (stateAction == null) {
            throw new IllegalStateException("State " + programState + " has no Action");
        }

        TelemetryPacket packet = new TelemetryPacket();
        packet.put("state", programState.name());
        packet.put("state time (ms)", stateElapsedMillis());
        packet.put("program time (ms)", programElapsedMillis());
        stateAction.preview(packet.fieldOverlay());
        boolean running = stateAction.run(packet);
        FtcDashboard.getInstance().sendTelemetryPacket(packet);
        if (!running) {
            stateAction = null;
        }
        return !running;
    }

    /** Initializes and runs one shared route step, then advances when that step is complete. */
    protected final void runRouteStepThen(int stepIndex, S next) {
        if (stageFinished) {
            if (stepIndex < 0 || stepIndex >= activeRoute.steps.size()) {
                throw new IllegalArgumentException("Route step index out of range: " + stepIndex);
            }
            initializeStateVariables(drive.runStep(activeRoute.steps.get(stepIndex), markerActions));
        }
        if (updateStateAction()) {
            finishActionOrStop(next);
        }
    }

    /** Initializes and runs any custom Action, then advances when that Action is complete. */
    protected final void runActionThen(Supplier<Action> actionFactory, S next) {
        if (stageFinished) {
            initializeStateVariables(actionFactory.get());
        }
        if (updateStateAction()) {
            finishActionOrStop(next);
        }
    }

    /** A localization or follower fault is fatal: stop instead of entering the next state. */
    private void finishActionOrStop(S next) {
        if (!drive.hasFault()) {
            nextState(next);
            return;
        }

        drive.stop();
        telemetry.addData("AUTONOMOUS STOPPED", drive.getFault());
        telemetry.update();
        requestOpModeStop();
    }

    /** Change states. The new state's entry block runs on the next OpMode loop. */
    protected final void nextState(S next) {
        drive.stop();
        stateAction = null;
        programState = next;
        stateStartTime = nowMillis();
        stageFinished = true;
    }

    /** State-relative timeout helper, disabled while the OpMode is in init. */
    protected final boolean isTimedOut(long milliseconds) {
        return isStarted() && stateElapsedMillis() > milliseconds;
    }

    @Override
    public final void runOpMode() {
        activeRoute = route();
        drive = PurePursuitDrive.create(hardwareMap, activeRoute.startPose);
        drive.setRouteName(activeRoute.name);
        initMechanisms();
        try {
            markerActions = markers();

            while (opModeInInit()) {
                drive.updatePoseEstimate();
                drive.reportIdle("waiting for start");
                telemetry.addData("Route", activeRoute.name);
                telemetry.addLine(activeRoute.description);
                if (activeRoute.requiresHolonomic && !drive.holonomic) {
                    telemetry.addLine("WARNING: this route strafes and needs a mecanum drive");
                }
                telemetry.addData("Start pose", activeRoute.startPose);
                telemetry.addData("Localizer pose", drive.getPose());
                telemetry.addData("Pinpoint", drive.getLocalizationStatus());
                if (!drive.isLocalizationReady()) {
                    telemetry.addLine("STOP: Pinpoint must say READY before this auto can drive");
                }
                telemetry.update();
            }
            if (isStopRequested()) {
                return;
            }

            programStartTime = nowMillis();
            stateStartTime = programStartTime;
            programState = initialState();
            stageFinished = true;

            while (opModeIsActive()) {
                mainStateMachine();
                telemetry.addData("State", programState);
                telemetry.addData("State time (ms)", stateElapsedMillis());
                telemetry.addData("Program time (ms)", programElapsedMillis());
                telemetry.addData("Pinpoint", drive.getLocalizationStatus());
                if (drive.hasFault()) {
                    telemetry.addData("AUTONOMOUS STOPPED", drive.getFault());
                }
                telemetry.update();

                if (stageFinished) {
                    drive.updatePoseEstimate();
                    drive.reportIdle("state: " + programState.name().toLowerCase());
                }
                idle();
            }
        } finally {
            drive.stop();
            stopMechanisms();
        }
    }

    private static long nowMillis() {
        return System.nanoTime() / 1_000_000L;
    }
}
