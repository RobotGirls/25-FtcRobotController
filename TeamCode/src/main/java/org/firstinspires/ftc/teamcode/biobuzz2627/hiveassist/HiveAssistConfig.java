package org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist;

import com.acmerobotics.dashboard.config.Config;

import org.ftc.hiveplanner.AllianceGoals;
import org.ftc.hiveplanner.Alliance;
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

    // Initial robot pose in the planner's 0..144 inch field coordinates. Pinpoint measures motion
    // from its reset location; it cannot discover this absolute field pose by itself.
    public static double START_FIELD_X_IN = 18;
    public static double START_FIELD_Y_IN = 18;
    public static double START_HEADING_DEG = 0;
    public static double BLUE_START_FIELD_X_IN = 18;
    public static double BLUE_START_FIELD_Y_IN = 18;
    public static double BLUE_START_HEADING_DEG = 0;
    public static double RED_START_FIELD_X_IN = 126;
    public static double RED_START_FIELD_Y_IN = 18;
    public static double RED_START_HEADING_DEG = 180;

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

    // Issue 41 field-relative turret. At INIT, physically point the shooter/Limelight straight
    // forward before the encoder is zeroed. Set TURRET_AIMING_VERIFIED only after checking the
    // signs at low power. MOTOR_REVS_PER_TURRET_REV includes any external gearing.
    public static String TURRET_MOTOR_NAME = "turret";
    public static double TURRET_MOTOR_REVS_PER_TURRET_REV = 1.0;
    public static double TURRET_ENCODER_SIGN = 1.0;
    public static double TURRET_POWER_SIGN = 1.0;
    public static double TURRET_FORWARD_OFFSET_DEG = 0.0;
    public static double TURRET_COARSE_KP = 0.012;
    public static double TURRET_COARSE_KI = 0.0;
    public static double TURRET_COARSE_KD = 0.0008;
    public static double TURRET_VISION_KP = 0.014;
    public static double TURRET_VISION_KI = 0.0;
    public static double TURRET_VISION_KD = 0.0008;
    public static double TURRET_INTEGRAL_LIMIT_DEG_SECONDS = 30.0;
    public static double TURRET_COARSE_TOLERANCE_DEG = 3.0;
    public static double TURRET_MAX_VISION_TRIM_DEG = 15.0;
    public static double TURRET_OFFSET_FORWARD_IN = 0.0;
    public static double TURRET_OFFSET_LEFT_IN = 0.0;

    // Separate coordinates let each selected AprilTag cluster have a surveyed aim point. The
    // defaults aim at the same alliance Hive center until the four cluster centers are measured.
    public static double RED_ALLIANCE_AIM_X_IN = 60.0;
    public static double RED_ALLIANCE_AIM_Y_IN = 72.0;
    public static double RED_FAR_AIM_X_IN = 60.0;
    public static double RED_FAR_AIM_Y_IN = 72.0;
    public static double BLUE_ALLIANCE_AIM_X_IN = 84.0;
    public static double BLUE_ALLIANCE_AIM_Y_IN = 72.0;
    public static double BLUE_FAR_AIM_X_IN = 84.0;
    public static double BLUE_FAR_AIM_Y_IN = 72.0;

    // Calibrate these from measured shots. The range indicator stays locked until verified.
    public static boolean SHOOTING_RANGE_LIMITS_VERIFIED = false;
    public static double MIN_SHOOTING_DISTANCE_METERS = 0;
    public static double MAX_SHOOTING_DISTANCE_METERS = 0;
    public static double MIN_SHOOTING_X_METERS = 0;
    public static double MAX_SHOOTING_X_METERS = 0;

    private HiveAssistConfig() {}

    public static double startFieldX(Alliance alliance) {
        return alliance == Alliance.RED ? RED_START_FIELD_X_IN : BLUE_START_FIELD_X_IN;
    }

    public static double startFieldY(Alliance alliance) {
        return alliance == Alliance.RED ? RED_START_FIELD_Y_IN : BLUE_START_FIELD_Y_IN;
    }

    public static double startHeadingDegrees(Alliance alliance) {
        return alliance == Alliance.RED ? RED_START_HEADING_DEG : BLUE_START_HEADING_DEG;
    }

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
