package org.firstinspires.ftc.teamcode.biobuzz2627.PurePursuit;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.teamcode.purepursuit.PurePursuitFollower;

/** Pure pursuit settings, live editable from FTC Dashboard (192.168.43.1:8080/dash). */
@Config
public final class PurePursuitConfig {
    private PurePursuitConfig() {}

    /** Follower tuning shared by every path. */
    public static PurePursuitFollower.Params FOLLOWER = new PurePursuitFollower.Params();

    /** Stop if a path requests translation but makes no meaningful progress for this long. */
    public static double NO_PROGRESS_TIMEOUT_SECONDS = 1.25;
    /** Progress needed to reset the no-progress timer. */
    public static double MIN_PROGRESS_INCHES = 0.35;
    /** A real turn also proves that Pinpoint and the robot are responding at a path corner. */
    public static double MIN_HEADING_PROGRESS_DEGREES = 3.0;
    /** Ignore the watchdog while translation power is below this value. */
    public static double WATCHDOG_MIN_TRANSLATION_POWER = 0.12;
    /** Send frames to the FieldViewer laptop app when it's connected. */
    public static boolean STREAM_TO_FIELD_VIEWER = true;

    /** Relay the Limelight MJPEG feed through the robot for FieldViewer Live mode. */
    public static boolean STREAM_LIMELIGHT_TO_FIELD_VIEWER = true;

    /** Limelight 3A address on the Control Hub's USB-Ethernet network. */
    public static String LIMELIGHT_HOST = "172.29.0.1";
    public static int LIMELIGHT_STREAM_PORT = 5800;

    /** Port exposed by the Control Hub to the laptop on the robot Wi-Fi network. */
    public static int LIMELIGHT_RELAY_PORT = 11117;
}
