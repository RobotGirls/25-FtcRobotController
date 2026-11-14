package org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist;

import org.ftc.hiveplanner.Alliance;
import org.ftc.hiveplanner.FieldSide;

/** Field coordinate of the AprilTag cluster selected by the Issue 40 controls. */
public final class HiveAimTarget {
    public final double fieldXIn;
    public final double fieldYIn;

    private HiveAimTarget(double fieldXIn, double fieldYIn) {
        this.fieldXIn = fieldXIn;
        this.fieldYIn = fieldYIn;
    }

    public static HiveAimTarget forSelection(Alliance alliance, FieldSide side) {
        if (alliance == Alliance.RED) {
            return side == FieldSide.AUDIENCE
                    ? new HiveAimTarget(HiveAssistConfig.RED_ALLIANCE_AIM_X_IN,
                            HiveAssistConfig.RED_ALLIANCE_AIM_Y_IN)
                    : new HiveAimTarget(HiveAssistConfig.RED_FAR_AIM_X_IN,
                            HiveAssistConfig.RED_FAR_AIM_Y_IN);
        }
        return side == FieldSide.AUDIENCE
                ? new HiveAimTarget(HiveAssistConfig.BLUE_ALLIANCE_AIM_X_IN,
                        HiveAssistConfig.BLUE_ALLIANCE_AIM_Y_IN)
                : new HiveAimTarget(HiveAssistConfig.BLUE_FAR_AIM_X_IN,
                        HiveAssistConfig.BLUE_FAR_AIM_Y_IN);
    }
}
