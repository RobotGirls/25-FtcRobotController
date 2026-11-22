package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos.tests;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.TestRoutes;

@Autonomous(name = "TEST PP 1 - Straight 24in", group = "Pure Pursuit Tests")
public class TestStraight24Auto extends TestRouteAutoBase {
    @Override
    protected Route route() {
        return TestRoutes.STRAIGHT_24;
    }
}
