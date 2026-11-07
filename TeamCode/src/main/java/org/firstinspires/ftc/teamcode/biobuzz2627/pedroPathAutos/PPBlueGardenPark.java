package org.firstinspires.ftc.teamcode.biobuzz2627.pedroPathAutos;

import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.biobuzz2627.auto.mechCode.IntakePurePursuit;
import org.firstinspires.ftc.teamcode.purepursuit.MechanismBindings;
import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.BioBuzzRoutes;

import java.util.Map;


/** Pure pursuit version of biobuzzautos/blueGardenAuto. */
@Autonomous(name = "PP BLUE Garden Park", group = "Pure Pursuit")
public class PPBlueGardenPark extends PurePursuitAutoBase {

    IntakePurePursuit intake;

    @Override
    protected Route route() {
        return BioBuzzRoutes.BLUE_GARDEN_PARK;
    }

    // 3. Initialize your mechanism
    @Override
    protected void initMechanisms() {
        intake = new IntakePurePursuit(hardwareMap, telemetry);
    }

    // 4. Register your mechanism markers
    @Override
    protected Map<String, Action> markers() {
        return new MechanismBindings<Action>()
                .register("collect", intake:: intakeArtifact)
                .register("eject", intake:: outtakeArtifact)
                .createAll();
    }

    // 5. Safely stop your mechanisms
    @Override
    protected void stopMechanisms() {
        // Add mechanism shutdown logic here
    }

}