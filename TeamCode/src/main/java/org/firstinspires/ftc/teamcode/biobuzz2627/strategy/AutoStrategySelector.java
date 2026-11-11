package org.firstinspires.ftc.teamcode.biobuzz2627.strategy;

import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;
import org.ftc.hiveplanner.Alliance;
import org.ftc.hiveplanner.FieldSide;
import org.ftc.hiveplanner.Point;

/** Pre-start color/side selection. X/B choose blue/red; d-pad left/right choose audience/far. */
public final class AutoStrategySelector {
    private Alliance alliance = Alliance.BLUE;
    private FieldSide side = FieldSide.AUDIENCE;
    private boolean frozen;

    public boolean update(Gamepad gamepad) {
        if (frozen) return false;
        Alliance oldAlliance = alliance;
        FieldSide oldSide = side;
        if (gamepad.x) alliance = Alliance.BLUE;
        if (gamepad.b) alliance = Alliance.RED;
        if (gamepad.dpad_left) side = FieldSide.AUDIENCE;
        if (gamepad.dpad_right) side = FieldSide.FAR;
        return oldAlliance != alliance || oldSide != side;
    }

    public AutoStrategy freeze() {
        frozen = true;
        return new AutoStrategy(alliance, side, parkGoal(alliance, side));
    }

    public Alliance alliance() { return alliance; }
    public FieldSide side() { return side; }

    private static Point parkGoal(Alliance alliance, FieldSide side) {
        if (alliance == Alliance.BLUE) {
            return side == FieldSide.AUDIENCE
                    ? new Point(AutomationConfig.BLUE_AUDIENCE_PARK_X_IN,
                            AutomationConfig.BLUE_AUDIENCE_PARK_Y_IN)
                    : new Point(AutomationConfig.BLUE_FAR_PARK_X_IN,
                            AutomationConfig.BLUE_FAR_PARK_Y_IN);
        }
        return side == FieldSide.AUDIENCE
                ? new Point(AutomationConfig.RED_AUDIENCE_PARK_X_IN,
                        AutomationConfig.RED_AUDIENCE_PARK_Y_IN)
                : new Point(AutomationConfig.RED_FAR_PARK_X_IN,
                        AutomationConfig.RED_FAR_PARK_Y_IN);
    }
}
