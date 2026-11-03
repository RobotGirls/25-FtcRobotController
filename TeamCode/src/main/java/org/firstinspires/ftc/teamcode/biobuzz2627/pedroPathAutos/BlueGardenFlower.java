package org.firstinspires.ftc.teamcode.biobuzz2627.pedroPathAutos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;



/** Pure pursuit version of biobuzzautos/blueGardenAuto. */
@Autonomous(name = "PP BLUE Garden Park", group = "Pure Pursuit")
public class PPBlueGardenPark extends PurePursuitAutoBase {
    @Override
    protected Route route() {
        return BioBuzzRoutes.BLUE_GARDEN_PARK;
    }
}