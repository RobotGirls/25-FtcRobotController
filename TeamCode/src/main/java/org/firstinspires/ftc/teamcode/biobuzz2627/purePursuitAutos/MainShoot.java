package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.biobuzz2627.mechCode.IntakePurePursuit;
import org.firstinspires.ftc.teamcode.biobuzz2627.mechCode.ShooterPurePursuit;
import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.BioBuzzRoutes;


/** Pure pursuit version of biobuzzautos/blueGardenAuto. */
@Autonomous(name = "Main Shoot", group = "Pure Pursuit")
public class MainShoot extends PurePursuitAutoBase<MainShoot.State> {
    private static final long SHOOT_TIME_MS = 4000;

    enum State {
        DRIVE_ACROSS_FIELD,
        SHOOT_POLLEN,
        DRIVE_BACK_INTO_ZONE,
        END
    }

    IntakePurePursuit intake;
    ShooterPurePursuit shooter;

    @Override
    protected Route route() {
        return BioBuzzRoutes.MAIN_SHOOT;
    }

    @Override
    protected State initialState() {
        return State.DRIVE_ACROSS_FIELD;
    }

    @Override
    protected void mainStateMachine() {
        switch (getState()) {
            case DRIVE_ACROSS_FIELD:
                runRouteStepThen(0, State.SHOOT_POLLEN);
                break;
            case SHOOT_POLLEN:
                if (stageFinished) {
                    initializeStateVariables();
                    shooter.startShooting();
                }
                shooter.addTelemetry();
                if (isTimedOut(SHOOT_TIME_MS)) {
                    shooter.stop();
                    nextState(State.DRIVE_BACK_INTO_ZONE);
                }
                break;
            case DRIVE_BACK_INTO_ZONE:
                runRouteStepThen(1, State.END);
                break;
            case END:
                drive.stop();
                break;
        }
    }

    // 3. Initialize your mechanism
    @Override
    protected void initMechanisms() {
        intake = new IntakePurePursuit(hardwareMap, telemetry);
        shooter = new ShooterPurePursuit(hardwareMap, telemetry);

    }

    @Override
    protected void stopMechanisms() {
        shooter.stop();
    }
}
