package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos.tests;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.TestRoutes;

@Autonomous(name = "TEST PP 9 - Field Loop", group = "Pure Pursuit Tests")
public class TestFieldLoopAuto extends TestRouteAutoBase {
    @Override
    protected Route route() {
        return TestRoutes.FIELD_LOOP;
    }
}
