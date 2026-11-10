package org.firstinspires.ftc.teamcode.purepursuit.paths;

import com.acmerobotics.roadrunner.Pose2d;
import org.firstinspires.ftc.teamcode.purepursuit.PurePursuitPath;
import org.firstinspires.ftc.teamcode.purepursuit.Route;

/** Example only: add to Routes.list() if you want it visible in the FieldViewer. */
public final class MechanismRouteExample {
    private MechanismRouteExample() {}

    public static final String INTAKE_ON = "intakeOn";
    public static final String INTAKE_OFF = "intakeOff";
    public static final String SHOOT = "shoot";

    public static final Route BLUE_DEMO = Route.builder(
                    "BLUE Mechanism Demo", Route.Alliance.BLUE, new Pose2d(-60, 36, 0))
            .description("Shows separate Java mechanism classes connected using named markers")
            .marker(INTAKE_ON, 0.15)
            .follow(PurePursuitPath.builder().name("collect").to(-35, 36).build())
            .marker(INTAKE_OFF, 0.15)
            .follow(PurePursuitPath.builder().name("shoot position").to(-35, 55).build())
            .marker(SHOOT, 2.5)
            .build();
}
