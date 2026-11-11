package org.firstinspires.ftc.teamcode.biobuzz2627.PurePursuit;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.teamcode.purepursuit.PurePursuitFollower;

/** Pure pursuit settings, live editable from FTC Dashboard (192.168.43.1:8080/dash). */
@Config
public final class PurePursuitConfig {
    private PurePursuitConfig() {}

    public enum DriveType {
        /** BioBuzz starter bot: TankDrive with the Pinpoint localizer. */
        TANK,
        /** MecanumDrive using the four drive-motor encoders plus the Control Hub IMU. */
        MECANUM,
        /** Competition mecanum drive using drive-motor encoders plus the Control Hub IMU. */
        COMP_MECANUM
    }

    /** Which drive class (and therefore which Road Runner localizer) the pure pursuit OpModes use. */
    public static DriveType DRIVE_TYPE = DriveType.MECANUM;

    /**
     * Safety gate. Keep false until LocalizationTest reports correct forward, strafe, and heading
     * distances. It can be enabled temporarily from FTC Dashboard during testing.
     */
    public static boolean LOCALIZATION_VERIFIED = false;

    /** Follower tuning shared by every path. */
    public static PurePursuitFollower.Params FOLLOWER = new PurePursuitFollower.Params();

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
