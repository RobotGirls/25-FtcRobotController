package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos.tests;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.TestRoutes;

@Autonomous(name = "TEST PP 4 - Mecanum Strafe Square", group = "Pure Pursuit Tests")
public class TestStrafeSquareAuto extends TestRouteAutoBase {
    @Override
    protected Route route() {
        return TestRoutes.STRAFE_SQUARE;
    }
}
