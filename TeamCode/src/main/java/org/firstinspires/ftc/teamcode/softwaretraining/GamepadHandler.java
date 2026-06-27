package org.firstinspires.ftc.teamcode.softwaretraining;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class GamepadHandler {
    private Gamepad localGamepad;

    private boolean prevA = false;
    private boolean prevB = false;
    private boolean prevX = false;
    private boolean prevY = false;

    // --- NEW: Internal state variables for the menu ---
    private boolean isRedAlliance = true;
    private boolean isCloseSide = true;

    public GamepadHandler(Gamepad gamepad) {
        this.localGamepad = gamepad;
    }

    public void update() {
        prevA = localGamepad.a;
        prevB = localGamepad.b;
        prevX = localGamepad.x;
        prevY = localGamepad.y;
    }

    // --- NEW: Dedicated Menu Method ---
    /**
     * Handles menu selections and outputs telemetry to the driver station.
     * @param telemetry Pass the OpMode's telemetry object to display the menu.
     */
    public void runConfigurationMenu(Telemetry telemetry) {
        // Update states first
        this.update();

        // Toggle Alliance Color: B for Red, X for Blue
        if (this.isBPressed()) { isRedAlliance = true; }
        if (this.isXPressed()) { isRedAlliance = false; }

        // Toggle Side: A for Close, Y for Far
        if (this.isAPressed()) { isCloseSide = true; }
        if (this.isYPressed()) { isCloseSide = false; }

        // Render Menu Telemetry
        telemetry.addLine("=== SYSTEM CONFIGURATION ===");
        telemetry.addData("Alliance", isRedAlliance ? "RED (Press B)" : "BLUE (Press X)");
        telemetry.addData("Starting Side", isCloseSide ? "CLOSE (Press A)" : "FAR (Press Y)");
        telemetry.addLine("--------------------------------");
    }

    // --- GETTERS FOR THE SELECTIONS ---
    public boolean isRedAlliance() { return isRedAlliance; }
    public boolean isCloseSide()    { return isCloseSide; }

    // --- DRIVE STICK GETTERS ---
    public double getDriveY() { return -localGamepad.left_stick_y; }
    public double getDriveX() { return localGamepad.left_stick_x; }
    public double getTurnX()  { return localGamepad.right_stick_x; }

    // --- RISING-EDGE BUTTON DETECTORS ---
    public boolean isAPressed() { return localGamepad.a && !prevA; }
    public boolean isBPressed() { return localGamepad.b && !prevB; }
    public boolean isXPressed() { return localGamepad.x && !prevX; }
    public boolean isYPressed() { return localGamepad.y && !prevY; }
}