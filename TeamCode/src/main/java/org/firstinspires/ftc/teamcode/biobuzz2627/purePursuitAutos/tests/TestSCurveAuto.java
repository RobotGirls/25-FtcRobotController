package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos.tests;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.TestRoutes;

@Autonomous(name = "TEST PP 7 - S Curve", group = "Pure Pursuit Tests")
public class TestSCurveAuto extends TestRouteAutoBase {
    @Override
    protected Route route() {
        return TestRoutes.S_CURVE;
    }
}
