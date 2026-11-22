package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos.tests;

import com.acmerobotics.roadrunner.Pose2d;

import org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos.PurePursuitAutoBase;

/**
 * Runs an entire {@code TestRoutes} route with the same Pinpoint localization, fault watchdog,
 * mecanum drive, and telemetry used by the competition Pure Pursuit autonomous OpModes.
 */
public abstract class TestRouteAutoBase
        extends PurePursuitAutoBase<TestRouteAutoBase.State> {

    protected enum State {
        RUN_TEST,
        END
    }

    @Override
    protected final State initialState() {
        return State.RUN_TEST;
    }

    @Override
    protected final void mainStateMachine() {
        switch (getState()) {
            case RUN_TEST:
                runActionThen(() -> drive.run(getRoute()), State.END);
                break;
            case END:
                drive.stop();
                Pose2d stopped = drive.getPose();
                telemetry.addLine("TEST COMPLETE");
                telemetry.addData("FINAL X", "%.2f in", stopped.position.x);
                telemetry.addData("FINAL Y", "%.2f in", stopped.position.y);
                telemetry.addData("FINAL HEADING", "%.1f deg",
                        Math.toDegrees(stopped.heading.toDouble()));
                telemetry.addData("PARALLEL TICKS", drive.getParallelEncoderTicks());
                telemetry.addData("PERPENDICULAR TICKS", drive.getPerpendicularEncoderTicks());
                break;
        }
    }
}
