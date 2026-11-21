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
@Autonomous(name = "Garden Shoot Blue", group = "Pure Pursuit")
public class GardenShoot extends PurePursuitAutoBase<GardenShoot.State> {

    public enum State {
        DRIVE_TO_HIVE_1,
        SHOOT_1,
        DRIVE_TO_GARDEN,
        INTAKE_GARDEN,
        DRIVE_TO_HIVE_2,
        SHOOT_2,
        PARK,
        END
    }

    IntakePurePursuit intake;
    ShooterPurePursuit shooter;

    @Override
    protected Route route() {
        return BioBuzzRoutes.GARDEN_SHOOT;
    }

    @Override
    protected State initialState() {
        return State.DRIVE_TO_HIVE_1;
    }

    @Override
    protected Map<String, Action> markers() {
        Map<String, Action> map = new HashMap<>();
        map.put(BioBuzzRoutes.SHOOT, shooter.shootArtifact());
        map.put(BioBuzzRoutes.INTAKE, intake.intakeArtifact());
        return map;
    }

    @Override
    protected void mainStateMachine() {
        switch (getState()) {
            case DRIVE_TO_HIVE_1:
                runRouteStepThen(0, State.SHOOT_1);
                break;
            case SHOOT_1:
                runRouteStepThen(1, State.DRIVE_TO_GARDEN);
                break;
            case DRIVE_TO_GARDEN:
                runRouteStepThen(2, State.INTAKE_GARDEN);
                break;
            case INTAKE_GARDEN:
                runRouteStepThen(3, State.DRIVE_TO_HIVE_2);
                break;
            case DRIVE_TO_HIVE_2:
                runRouteStepThen(4, State.SHOOT_2);
                break;
            case SHOOT_2:
                runRouteStepThen(5, State.PARK);
                break;
            case PARK:
                runRouteStepThen(6, State.END);
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
