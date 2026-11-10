package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.BioBuzzRoutes;


/** Pure pursuit version of biobuzzautos/blueGardenAuto. */
@Autonomous(name = "Flower Park", group = "Pure Pursuit")
public class FlowerPark extends PurePursuitAutoBase<FlowerPark.State> {

    public enum State {
        PARK,
        END
    }

    @Override
    protected Route route() {
        return BioBuzzRoutes.FLOWER_PARK;
    }

    @Override
    protected State initialState() {
        return State.PARK;
    }

    @Override
    protected void mainStateMachine() {
        switch (getState()) {
            case PARK:
                runRouteStepThen(0, State.END);
                break;
            case END:
                drive.stop();
                break;
        }
    }
}


