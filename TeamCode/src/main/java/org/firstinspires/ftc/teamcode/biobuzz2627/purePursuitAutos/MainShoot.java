package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos;

import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.biobuzz2627.mechCode.IntakePurePursuit;
import org.firstinspires.ftc.teamcode.biobuzz2627.mechCode.ShooterPurePursuit;
import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.BioBuzzRoutes;

import java.util.HashMap;
import java.util.Map;

/** Pure pursuit version of biobuzzautos/blueGardenAuto. */
@Autonomous(name = "Main Shoot", group = "Pure Pursuit")
public class MainShoot extends PurePursuitAutoBase<MainShoot.State> {

    public enum State {
        DRIVE_TO_HIVE,
        SHOOT,
        PARK,
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
        return State.DRIVE_TO_HIVE;
    }

    @Override
    protected Map<String, Action> markers() {
        Map<String, Action> map = new HashMap<>();
        map.put(BioBuzzRoutes.SHOOT, shooter.shootArtifact());
        return map;
    }

    @Override
    protected void mainStateMachine() {
        switch (getState()) {
            case DRIVE_TO_HIVE:
                runRouteStepThen(0, State.SHOOT);
                break;
            case SHOOT:
                runRouteStepThen(1, State.PARK);
                break;
            case PARK:
                runRouteStepThen(2, State.END);
                break;
            case END:
                drive.stop();
                break;
        }
    }

    @Override
    protected void initMechanisms() {
        intake = new IntakePurePursuit(hardwareMap, telemetry);
        shooter = new ShooterPurePursuit(hardwareMap, telemetry);
    }

    @Override
    protected void stopMechanisms() {
        shooter.stop();
        intake.stop();
    }
}
