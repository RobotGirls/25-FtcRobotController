package org.firstinspires.ftc.teamcode.biobuzz2627.automation;

import com.acmerobotics.dashboard.config.Config;
import org.firstinspires.ftc.teamcode.MecanumDrive;

/** All unverified hardware, vision, navigation, and autonomous values fail closed here. */
@Config
public final class AutomationConfig {
    public static boolean MECHANISMS_VERIFIED = false;
    public static boolean PIECE_SENSORS_VERIFIED = false;
    public static boolean LIMELIGHT_POSE_TRANSFORM_VERIFIED = false;
    public static boolean PARK_GOALS_VERIFIED = false;
    public static boolean COMPLETE_AUTO_VERIFIED = false;
    public static boolean CHARACTERIZATION_ENABLED = false;
    public static boolean USE_PINPOINT_LOCALIZER = false;
    public static boolean PINPOINT_LOCALIZER_VERIFIED = false;

    public static String SHOOTER_NAME = "shooter";
    public static String INTAKE_NAME = "intake";
    public static String TRANSFER_NAME = "transfer";
    public static String INTAKE_SENSOR_NAME = "intakeLoaded";
    public static String TRANSFER_SENSOR_NAME = "transferLoaded";
    public static boolean PIECE_SENSOR_ACTIVE_LOW = true;

    public static double SHOOTER_TARGET_TICKS_PER_SECOND = 0;
    public static double SHOOTER_VELOCITY_TOLERANCE = 75;
    public static long SHOOTER_SETTLE_MS = 250;
    public static long SHOOTER_SPINUP_TIMEOUT_MS = 3000;
    public static double SHOOTER_MAX_CURRENT_AMPS = 8;

    public static double INTAKE_POWER = 0;
    public static double INTAKE_REVERSE_POWER = 0;
    public static long INTAKE_TIMEOUT_MS = 3500;
    public static double INTAKE_MAX_CURRENT_AMPS = 8;

    public static double TRANSFER_POWER = 0;
    public static long TRANSFER_MIN_FEED_MS = 150;
    public static long TRANSFER_TIMEOUT_MS = 1500;
    public static double TRANSFER_MAX_CURRENT_AMPS = 8;

    public static long SELECTED_TAG_FRESH_MS = 500;
    public static long LIMELIGHT_MAX_STALENESS_MS = 150;
    public static double LIMELIGHT_MAX_TOTAL_LATENCY_MS = 100;
    public static int LIMELIGHT_MIN_TAG_COUNT = 1;
    public static double LIMELIGHT_MAX_TAG_DISTANCE_M = 4;
    public static double LIMELIGHT_MAX_TRANSLATION_JUMP_IN = 6;
    public static double LIMELIGHT_MAX_HEADING_JUMP_DEG = 12;
    public static double LIMELIGHT_BLEND = 0.15;
    // Limelight botpose -> Road Runner transform. Measure before enabling correction.
    public static double LIMELIGHT_X_SIGN = 1;
    public static double LIMELIGHT_Y_SIGN = 1;
    public static double LIMELIGHT_HEADING_SIGN = 1;
    public static double LIMELIGHT_X_OFFSET_IN = 0;
    public static double LIMELIGHT_Y_OFFSET_IN = 0;
    public static double LIMELIGHT_HEADING_OFFSET_DEG = 0;

    public static double PATH_DEFAULT_MOVE_SPEED = 0.45;
    public static double PATH_DEFAULT_TURN_SPEED = 0.30;
    public static double PATH_DEFAULT_LOOKAHEAD_IN = 12;
    public static double PATH_CORNER_MOVE_SPEED = 0.30;
    public static double PATH_CORNER_LOOKAHEAD_IN = 8;
    public static double PATH_FINAL_MOVE_SPEED = 0.20;
    public static double PATH_FINAL_LOOKAHEAD_IN = 6;
    public static double PATH_WAYPOINT_TOLERANCE_IN = 2;
    public static double PATH_FINISH_TOLERANCE_IN = 1.5;
    public static double PATH_FINISH_HEADING_TOLERANCE_DEG = 4;
    public static double PATH_REPLAN_DEVIATION_IN = 8;
    public static long PATH_REPLAN_MIN_INTERVAL_MS = 750;
    public static long PATH_NO_PROGRESS_TIMEOUT_MS = 1500;
    public static long PATH_MAX_RUN_MS = 9000;

    // Planner field coordinates (0..144). These are placeholders until surveyed.
    public static double BLUE_AUDIENCE_PARK_X_IN = 18;
    public static double BLUE_AUDIENCE_PARK_Y_IN = 18;
    public static double BLUE_FAR_PARK_X_IN = 18;
    public static double BLUE_FAR_PARK_Y_IN = 126;
    public static double RED_AUDIENCE_PARK_X_IN = 126;
    public static double RED_AUDIENCE_PARK_Y_IN = 18;
    public static double RED_FAR_PARK_X_IN = 126;
    public static double RED_FAR_PARK_Y_IN = 126;
    public static double PARK_HEADING_DEG = 0;
    public static long EMERGENCY_PARK_RESERVE_MS = 8000;

    private AutomationConfig() {}

    public static MecanumDrive.LocalizerMode mecanumLocalizerMode() {
        if (USE_PINPOINT_LOCALIZER && !PINPOINT_LOCALIZER_VERIFIED) {
            throw new IllegalStateException("Pinpoint localizer has not been calibrated and verified");
        }
        return USE_PINPOINT_LOCALIZER
                ? MecanumDrive.LocalizerMode.PINPOINT
                : MecanumDrive.LocalizerMode.DRIVE_ENCODERS;
    }

    public static void requireMechanismsVerified() {
        if (!MECHANISMS_VERIFIED
                || SHOOTER_TARGET_TICKS_PER_SECOND == 0
                || INTAKE_POWER == 0
                || TRANSFER_POWER == 0) {
            throw new IllegalStateException("Mechanism directions/speeds are not verified");
        }
    }
}
