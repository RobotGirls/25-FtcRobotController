package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.BioBuzzRoutes;

/**
 * Drive-only copy of Main Shoot. It follows the same Pure Pursuit route but intentionally does
 * not initialize or command the shooter, intake, feeder, servos, or any other mechanism.
 */
@Autonomous(name = "Main Shoot - Drive", group = "Pure Pursuit")
public class MainShootDriveOnly extends PurePursuitAutoBase<MainShootDriveOnly.State> {

    public enum State {
        DRIVE_ACROSS_FIELD,
        WAIT_AT_HIVE,
        DRIVE_INTO_ZONE,
        END
    }

    @Override
    protected Route route() {
        return BioBuzzRoutes.BLUE_GARDEN_PARK_SHOOTER_COPY;
    }

    @Override
    protected State initialState() {
        return State.DRIVE_ACROSS_FIELD;
    }

    @Override
    protected void mainStateMachine() {
        switch (getState()) {
            case DRIVE_ACROSS_FIELD:
                runRouteStepThen(0, State.WAIT_AT_HIVE);
                break;
            case WAIT_AT_HIVE:
                runRouteStepThen(1, State.DRIVE_INTO_ZONE);
                break;
            case DRIVE_INTO_ZONE:
                runRouteStepThen(2, State.END);
                break;
            case END:
                drive.stop();
                break;
        }
    }
}
