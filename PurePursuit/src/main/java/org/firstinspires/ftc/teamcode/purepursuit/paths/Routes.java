package org.firstinspires.ftc.teamcode.purepursuit.paths;

import org.firstinspires.ftc.teamcode.purepursuit.BioBuzzField;
import org.firstinspires.ftc.teamcode.purepursuit.Route;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Every route, in the order the FieldViewer and the path tester OpMode list them. */
public final class Routes {
    private Routes() {}

    public static final List<Route> ALL = Collections.unmodifiableList(Arrays.asList(
            BioBuzzRoutes.FLOWER_PARK,
            //BioBuzzRoutes.BLUE_CORNER_PARK,
            //BioBuzzRoutes.BLUE_GARDEN_SHOOTER,
            //BioBuzzRoutes.RED_FLOWER_PARK,
            //BioBuzzRoutes.RED_GARDEN_PARK,
            //BioBuzzRoutes.RED_CORNER_PARK,
            //BioBuzzRoutes.RNRR_BLUE_SHOOT,
            BioBuzzRoutes.MAIN_SHOOT
    ));

    /** Looks a route up by name, or returns null. */
    public static Route byName(String name) {
        for (Route route : ALL) {
            if (route.name.equals(name)) {
                return route;
            }
        }
        return null;
    }
}
