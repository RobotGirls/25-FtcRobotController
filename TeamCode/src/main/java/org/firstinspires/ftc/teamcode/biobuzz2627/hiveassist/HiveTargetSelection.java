package org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.ftc.hiveplanner.Alliance;
import org.ftc.hiveplanner.DriveAssist;
import org.ftc.hiveplanner.FieldSide;

/** Owns the driver's color/field-side selection and its button edge detection. */
public final class HiveTargetSelection {
    private Alliance alliance = Alliance.BLUE;
    private FieldSide side = FieldSide.AUDIENCE;
    private boolean previousX;
    private boolean previousB;
    private boolean previousDpadLeft;
    private boolean previousDpadRight;

    /** Returns true when the selected target cluster changed. */
    public boolean update(Gamepad gamepad, DriveAssist assist) {
        boolean changed = updateAlliance(gamepad, assist);
        if (!assist.active()) {
            if (gamepad.dpad_left && !previousDpadLeft && side != FieldSide.AUDIENCE) {
                side = FieldSide.AUDIENCE;
                assist.setSide(side);
                changed = true;
            }
            if (gamepad.dpad_right && !previousDpadRight && side != FieldSide.FAR) {
                side = FieldSide.FAR;
                assist.setSide(side);
                changed = true;
            }
        }

        previousDpadLeft = gamepad.dpad_left;
        previousDpadRight = gamepad.dpad_right;
        return changed;
    }

    /** X/B select BLUE/RED while idle. The Issue 40 TeleOp calls this only during INIT. */
    public boolean updateAlliance(Gamepad gamepad, DriveAssist assist) {
        boolean changed = false;
        if (!assist.active()) {
            if (gamepad.x && !previousX && alliance != Alliance.BLUE) {
                alliance = Alliance.BLUE;
                assist.setAlliance(alliance);
                changed = true;
            }
            if (gamepad.b && !previousB && alliance != Alliance.RED) {
                alliance = Alliance.RED;
                assist.setAlliance(alliance);
                changed = true;
            }
        }
        previousX = gamepad.x;
        previousB = gamepad.b;
        return changed;
    }

    /** Left/right bumper select audience/alliance side or far side; the choice stays selected. */
    public boolean updateSideFromBumpers(Gamepad gamepad) {
        boolean changed = false;
        if (gamepad.left_bumper && !gamepad.right_bumper && side != FieldSide.AUDIENCE) {
            side = FieldSide.AUDIENCE;
            changed = true;
        }
        if (gamepad.right_bumper && !gamepad.left_bumper && side != FieldSide.FAR) {
            side = FieldSide.FAR;
            changed = true;
        }
        return changed;
    }

    public Alliance alliance() {
        return alliance;
    }

    public FieldSide side() {
        return side;
    }

    public String clusterName() {
        return alliance + " " + (side == FieldSide.AUDIENCE ? "AUDIENCE" : "SCORING/FAR");
    }

    public boolean matchesTag(int id) {
        // Official SDK 12 BIOBUZZ clusters: RED SCORING 30-33, RED AUDIENCE 34-37,
        // BLUE AUDIENCE 38-41, BLUE SCORING 42-45.
        if (alliance == Alliance.RED) {
            return side == FieldSide.AUDIENCE ? between(id, 34, 37) : between(id, 30, 33);
        }
        return side == FieldSide.AUDIENCE ? between(id, 38, 41) : between(id, 42, 45);
    }

    private static boolean between(int value, int minimum, int maximum) {
        return value >= minimum && value <= maximum;
    }
}
