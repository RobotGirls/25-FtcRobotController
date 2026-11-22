package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos.tests;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.TestRoutes;

@Autonomous(name = "TEST PP 6 - Out And Back", group = "Pure Pursuit Tests")
public class TestOutAndBackAuto extends TestRouteAutoBase {
    @Override
    protected Route route() {
        return TestRoutes.OUT_AND_BACK;
    }
}
