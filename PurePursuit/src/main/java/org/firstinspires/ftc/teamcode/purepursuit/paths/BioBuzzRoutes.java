package org.firstinspires.ftc.teamcode.purepursuit.paths;

import com.acmerobotics.roadrunner.Pose2d;

import org.firstinspires.ftc.teamcode.purepursuit.PurePursuitPath;
import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.Route.Alliance;
import org.firstinspires.ftc.teamcode.purepursuit.RouteSimulation;

/**
 * BioBuzz autonomous routes. These are the same start poses and moves as the Road Runner autos
 * in teamcode/biobuzzautos (tested on the starter bot), rewritten as pure pursuit paths.
 *
 * Those autos drive a tank drive with heading 0, so a lineToX towards -x was really driving
 * backwards; those legs use .reversed() here so the robot doesn't spin around first.
 */
public final class BioBuzzRoutes {
    private BioBuzzRoutes() {
    }

    /**
     * Marker name the shooter auto maps to its launch Action.
     */
    public static final String SHOOT = "shoot";

    public static final Route FLOWER_PARK = Route.builder(
                    "BLUE Garden Park", Alliance.NONE, new Pose2d(-12, 69, Math.toRadians(-90)))
            .follow(PurePursuitPath.builder().name("blue garden park copy")
                    .followDistance(6)
                    .to(-12, 60)
                    .moveSpeed(0.5)
                    .to(34, 60)
                    .fixedHeading(Math.toRadians(-90))
                    .build())
            .build();

//    public static final Route BLUE_CORNER_PARK = Route.builder(
//                    "BLUE Corner Park", Alliance.BLUE, new Pose2d(66, 60, 0))
//            .description("Back out of the corner into the blue zone.")
//            .follow(PurePursuitPath.builder().name("to park")
//                    .to(40, 60)
//                    .reversed()
//                    .build())
//            .build();

//    public static final Route RED_FLOWER_PARK = Route.builder(
//                    "RED Flower Park", Alliance.RED, new Pose2d(10, -60, 0))
//            .description("Back up along the red wall into the red zone.")
//            .follow(PurePursuitPath.builder().name("to park")
//                    .to(-30, -60)
//                    .reversed()
//                    .build())
//            .build();

//    public static final Route RED_GARDEN_PARK = Route.builder(
//                    "RED Garden Park", Alliance.RED, new Pose2d(66, -34, 0))
//            .description("Back up below the center structure, then drive into the red zone.")
//            .follow(PurePursuitPath.builder().name("across the field")
//                    .to(-34, -34)
//                    .reversed()
//                    .build())
//            .marker("collect", 4.0)
//            .follow(PurePursuitPath.builder().name("into zone")
//                    .moveSpeed(0.5)
//                    .to(-34, -60)
//                    .build())
//            .build();

//    public static final Route RED_CORNER_PARK = Route.builder(
//                    "RED Corner Park", Alliance.RED, new Pose2d(-60, -66, 0))
//            .description("Drive out of the corner into the red zone.")
//            .follow(PurePursuitPath.builder().name("to park")
//                    .to(-40, -66)
//                    .build())
//            .build();

//    public static final Route BLUE_GARDEN_SHOOTER = Route.builder(
//                    "BLUE Garden Park + Shooter", Alliance.BLUE, new Pose2d(-66, 12, 0))
//            .description("BLUE Garden Park, then spin up the launcher and feed artifacts.")
//            .follow(PurePursuitPath.builder().name("to hive")
//                    .to(-36, 12)
//                    .build())
//
//            .marker(SHOOT, 3.5)
//
//            .follow(PurePursuitPath.builder().name("to hive")
//                    .to(-36, 40)
//                    .to(0, 40)
//                    .to(34, 40)
//                    .moveSpeed(0.5)
//                    .to(34, 60)
//                    .build())
//            .build();

//    public static final Route RNRR_BLUE_SHOOT = Route.builder(
//                    "RNRR_BLUE_SHOOT", Alliance.BLUE, new Pose2d(-63, 12, Math.toRadians(0)))
//            .description("BLUE Garden Park, then spin up the launcher and feed artifacts.")
//            .follow(PurePursuitPath.builder().name("Near blue garden and shooter")
//                    .to(-42, 12)
//                    .to(-42, 53)
//                    .to(36, 57)
//                    .build())
//            .build();
    public static final Route MAIN_SHOOT = Route.builder(
                    "BLUE Garden Park + Shooter copy", Alliance.NONE, new Pose2d(-66, 12, Math.toRadians(0)))
            .description("Drive around the Hive, shoot, then enter the parking zone.")
            .follow(PurePursuitPath.builder().name("drive around hive")
                    .to(-36, 12)
                    .to(-36, 40)
                    .to(0, 40)
                    .to(34, 40)
                    .timeout(12)
                    .build())
            .follow(PurePursuitPath.builder().name("enter parking zone")
                    .moveSpeed(0.5)
                    .to(34, 60)
                    .timeout(5)
                    .build())
            .build();


}
