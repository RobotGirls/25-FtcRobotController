package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.BioBuzzRoutes;

/** Blue-side mirror of Flower Park: clear the wall, then strafe robot-left into the zone. */
@Autonomous(name = "BLUE Flower Park SAFE v1", group = "Pure Pursuit")
public class BlueFlowerPark extends PurePursuitAutoBase<BlueFlowerPark.State> {

    public enum State {
        CLEAR_START,
        STRAFE_TO_PARK,
        END
    }

    @Override
    protected Route route() {
        return BioBuzzRoutes.BLUE_FLOWER_PARK;
    }

    @Override
    protected State initialState() {
        return State.CLEAR_START;
    }

    @Override
    protected void mainStateMachine() {
        switch (getState()) {
            case CLEAR_START:
                runRouteStepThen(0, State.STRAFE_TO_PARK);
                break;
            case STRAFE_TO_PARK:
                runRouteStepThen(1, State.END);
                break;
            case END:
                drive.stop();
                Pose2d stopped = drive.getPose();
                telemetry.addData("STRAFE STOP X", "%.2f in", stopped.position.x);
                telemetry.addData("STRAFE STOP Y", "%.2f in", stopped.position.y);
                telemetry.addData("STRAFE STOP HEADING", "%.1f deg",
                        Math.toDegrees(stopped.heading.toDouble()));
                break;
        }
    }
}
