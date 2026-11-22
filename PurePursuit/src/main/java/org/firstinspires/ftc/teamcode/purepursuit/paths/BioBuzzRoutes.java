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

    /** Red-side Flower Park targets used by both the simulator and the real robot. */
    public static final double FLOWER_START_X = 12;
    public static final double FLOWER_START_Y = -62;
    public static final double FLOWER_FORWARD_DISTANCE = 4.5;
    /** Facing +90 degrees, driving forward toward center makes field Y increase. */
    public static final double FLOWER_CLEAR_Y = FLOWER_START_Y + FLOWER_FORWARD_DISTANCE;
    /** Facing +90 degrees, robot-left is negative field X. */
    public static final double FLOWER_PARK_X = -50;

    /** Blue-side targets mirror the red route through the center of the field. */
    public static final double BLUE_FLOWER_START_X = -FLOWER_START_X;
    public static final double BLUE_FLOWER_START_Y = -FLOWER_START_Y;
    /** Facing -90 degrees, driving forward toward center makes field Y decrease. */
    public static final double BLUE_FLOWER_CLEAR_Y =
            BLUE_FLOWER_START_Y - FLOWER_FORWARD_DISTANCE;
    /** Facing -90 degrees, robot-left is positive field X. */
    public static final double BLUE_FLOWER_PARK_X = -FLOWER_PARK_X;

    public static final double X_OFFSET = 25;

    public static final double Y_OFFSET = 12;


    public static final Route MAIN_SHOOT = Route.builder(
                    "BLUE Garden Park + Shooter copy", Alliance.NONE, new Pose2d(-66, 12, Math.toRadians(0)))
            .description("Drive around the Hive, shoot, then enter the parking zone.")
            .follow(PurePursuitPath.builder().name("drive around hive")
                    .to(-36, 12)
                    // Back away before going around so the fixed-forward robot footprint and
                    // Pure Pursuit's rounded corner stay clear of the Hive.
                    .to(-42, 12)
                    .to(-42, 46)
                    .to(0, 46)
                    .to(34, 46)
                    // Keep the front of the mecanum robot facing field-forward (+X).
                    .fixedHeading(Math.toRadians(0))
                    .timeout(12)
                    .build())
            .follow(PurePursuitPath.builder().name("enter parking zone")
                    .moveSpeed(0.5)
                    .to(34, 60)
                    .fixedHeading(Math.toRadians(0))
                    .timeout(5)
                    .build())
            .build();

    public static final Route RED_FLOWER_PARK = Route.builder(
                    "RED Flower Park",
                    Alliance.RED,
                    new Pose2d(FLOWER_START_X, FLOWER_START_Y, Math.toRadians(90)))
            .description("Move forward 4.5 inches, then strafe left into the red parking zone.")
            .follow(PurePursuitPath.builder()
                    .name("clear flower start")
                    .followDistance(4)
                    .moveSpeed(0.3)
                    .to(FLOWER_START_X, FLOWER_CLEAR_Y)
                    .fixedHeading(Math.toRadians(90))
                    .build())
            .follow(PurePursuitPath.builder()
                    .name("strafe left into red parking zone")
                    .followDistance(6)
                    .moveSpeed(0.3)
                    .to(FLOWER_PARK_X, FLOWER_CLEAR_Y)
                    .fixedHeading(Math.toRadians(90))
                    .build())
            .build();

    public static final Route BLUE_FLOWER_PARK = Route.builder(
                    "BLUE Flower Park",
                    Alliance.BLUE,
                    new Pose2d(BLUE_FLOWER_START_X, BLUE_FLOWER_START_Y,
                            Math.toRadians(-90)))
            .description("Move forward 4.5 inches, then strafe left into the blue parking zone.")
            .follow(PurePursuitPath.builder()
                    .name("clear blue flower start")
                    .followDistance(4)
                    .moveSpeed(0.3)
                    .to(BLUE_FLOWER_START_X, BLUE_FLOWER_CLEAR_Y)
                    .fixedHeading(Math.toRadians(-90))
                    .build())
            .follow(PurePursuitPath.builder()
                    .name("strafe left into blue parking zone")
                    .followDistance(6)
                    .moveSpeed(0.3)
                    .to(BLUE_FLOWER_PARK_X, BLUE_FLOWER_CLEAR_Y)
                    .fixedHeading(Math.toRadians(-90))
                    .build())
            .build();

    public static final Route BLUE_GARDEN_PARK_SHOOTER = Route.builder(
                    "BLUE Garden Park + Shooter", Alliance.NONE, new Pose2d(-66, 12, Math.toRadians(0)))
            .follow(PurePursuitPath.builder().name("blue garden park + shooter copy copy")
                    .to(-36, 12)
                    .to(-42, 12)
                    .to(-42, 52)
                    .to(0, 52)
                    .to(53, 52)
                    .to(53+X_OFFSET, 70+Y_OFFSET)
                    .fixedHeading(Math.toRadians(0))
                    .build())
            .build();



}
