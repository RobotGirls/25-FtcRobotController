package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import com.acmerobotics.roadrunner.Pose2d;

import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.BioBuzzRoutes;


/** Red-side Flower Park: clear the wall, then strafe robot-left into the parking zone. */
@Autonomous(name = "RED Flower Park SAFE v4", group = "Pure Pursuit")
public class FlowerPark extends PurePursuitAutoBase<FlowerPark.State> {

    public enum State {
        CLEAR_START,
        STRAFE_TO_PARK,
        END
    }

    @Override
    protected Route route() {
        return BioBuzzRoutes.FLOWER_PARK;
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


