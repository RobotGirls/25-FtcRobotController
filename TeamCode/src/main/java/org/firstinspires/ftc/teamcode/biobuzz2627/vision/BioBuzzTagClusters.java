package org.firstinspires.ftc.teamcode.biobuzz2627.vision;

import org.ftc.hiveplanner.Alliance;
import org.ftc.hiveplanner.FieldSide;

/** Official SDK 12 BIOBUZZ AprilTag cluster membership. */
public final class BioBuzzTagClusters {
    private BioBuzzTagClusters() {}

    public static boolean matches(Alliance alliance, FieldSide side, int id) {
        if (alliance == Alliance.RED) {
            return side == FieldSide.AUDIENCE ? between(id, 34, 37) : between(id, 30, 33);
        }
        return side == FieldSide.AUDIENCE ? between(id, 38, 41) : between(id, 42, 45);
    }

    private static boolean between(int value, int minimum, int maximum) {
        return value >= minimum && value <= maximum;
    }
}
