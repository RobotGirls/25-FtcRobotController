package org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist;

import com.acmerobotics.dashboard.config.Config;

import org.ftc.hiveplanner.AllianceGoals;
import org.ftc.hiveplanner.FieldConfig;
import org.ftc.hiveplanner.Point;

import java.util.Arrays;

/** Dashboard-tunable field survey and shooting calibration for the Hive assist TeleOp. */
@Config
public final class HiveAssistConfig {
    /** Keep false until every coordinate and clearance below has been measured and field-tested. */
    public static boolean GEOMETRY_AND_GOALS_VERIFIED = false;

    /** Converts planner coordinates (0..144) to Road Runner coordinates (-72..72). */
    public static final double ROAD_RUNNER_FIELD_OFFSET_IN = 72.0;

    // Initial robot pose in the planner's 0..144 inch field coordinates.
    public static double START_FIELD_X_IN = 18;
    public static double START_FIELD_Y_IN = 18;
    public static double START_HEADING_DEG = 0;

    public static double ROBOT_BOUNDING_RADIUS_IN = 10;
    public static double EXTRA_CLEARANCE_IN = 4;
    public static double PERIMETER_MARGIN_IN = 2;
    public static double HIVE_CENTER_X_IN = 72;
    public static double HIVE_CENTER_Y_IN = 72;
    public static double HIVE_EXCLUSION_RADIUS_IN = 32;
    public static double PLANNER_GRID_IN = 3;

    // Candidate shooting locations. These are starting estimates and MUST be shooting-tested.
    public static double RED_HIVE_X_IN = 60;
    public static double BLUE_HIVE_X_IN = 84;
    public static double AUDIENCE_SHOOT_Y_IN = 20;
    public static double FAR_SHOOT_Y_IN = 124;
    public static double CANDIDATE_SPACING_IN = 4;

    public static int LIMELIGHT_APRILTAG_PIPELINE = 0;
    public static long TARGET_LOSS_GRACE_MS = 500;
    public static double MANUAL_DRIVE_SPEED = 0.8;
    public static double MANUAL_OVERRIDE_DEADBAND = 0.15;

    // FieldViewer talks to this relay over the robot Wi-Fi. The relay reaches the Limelight over
    // the Control Hub's USB-Ethernet link, so the laptop never needs direct Limelight access.
    public static boolean FIELD_VIEWER_LIMELIGHT_RELAY_ENABLED = true;
    public static String LIMELIGHT_STREAM_HOST = "172.29.0.1";
    public static int LIMELIGHT_STREAM_PORT = 5800;
    public static int LIMELIGHT_RELAY_PORT = 11117;
    public static int LIMELIGHT_MIN_PIPELINE = 0;
    public static int LIMELIGHT_MAX_PIPELINE = 9;

    // Limelight turret aiming. Verify motor direction and mechanical limits before enabling.
    public static boolean TURRET_AIMING_VERIFIED = false;
    public static double TURRET_TX_SIGN = -1.0;
    public static double TURRET_KP = 0.014;
    public static double TURRET_MIN_POWER = 0.08;
    public static double TURRET_MAX_POWER = 0.35;
    public static double TURRET_ALIGNMENT_TOLERANCE_DEG = 2.0;

    // Calibrate these from measured shots. The range indicator stays locked until verified.
    public static boolean SHOOTING_RANGE_LIMITS_VERIFIED = false;
    public static double MIN_SHOOTING_DISTANCE_METERS = 0;
    public static double MAX_SHOOTING_DISTANCE_METERS = 0;
    public static double MIN_SHOOTING_X_METERS = 0;
    public static double MAX_SHOOTING_X_METERS = 0;

    private HiveAssistConfig() {}

    public static FieldConfig fieldConfig() {
        return new FieldConfig(
                ROBOT_BOUNDING_RADIUS_IN,
                EXTRA_CLEARANCE_IN,
                PERIMETER_MARGIN_IN,
                HIVE_CENTER_X_IN,
                HIVE_CENTER_Y_IN,
                HIVE_EXCLUSION_RADIUS_IN,
                PLANNER_GRID_IN,
                GEOMETRY_AND_GOALS_VERIFIED);
    }

    public static AllianceGoals goals() {
        return new AllianceGoals(
                candidates(RED_HIVE_X_IN, AUDIENCE_SHOOT_Y_IN),
                candidates(RED_HIVE_X_IN, FAR_SHOOT_Y_IN),
                candidates(BLUE_HIVE_X_IN, AUDIENCE_SHOOT_Y_IN),
                candidates(BLUE_HIVE_X_IN, FAR_SHOOT_Y_IN));
    }

    private static java.util.List<Point> candidates(double centerX, double y) {
        return Arrays.asList(
                new Point(centerX - CANDIDATE_SPACING_IN, y),
                new Point(centerX, y),
                new Point(centerX + CANDIDATE_SPACING_IN, y));
    }
}
