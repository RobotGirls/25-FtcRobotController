package org.ftc.hiveplanner;

/** Robot/field survey-specific values are deliberately NOT supplied by the game manual.
 * An explicit survey flag gates any real robot use. */
public final class FieldConfig {
    public final double robotBoundingRadiusIn;
    public final double additionalClearanceIn;
    public final double perimeterMarginIn;
    public final double hiveCenterXIn, hiveCenterYIn;
    public final double hiveExclusionRadiusIn;
    public final double gridIn;
    public final boolean surveyedAndVerified;

    public FieldConfig(double robotBoundingRadiusIn, double additionalClearanceIn,
        double perimeterMarginIn, double hiveCenterXIn, double hiveCenterYIn,
        double hiveExclusionRadiusIn, double gridIn, boolean surveyedAndVerified) {
        if (robotBoundingRadiusIn <= 0 || additionalClearanceIn < 0 || perimeterMarginIn < 0
            || hiveExclusionRadiusIn <= 0 || gridIn <= 0 || gridIn > 6)
            throw new IllegalArgumentException("Invalid geometry");
        this.robotBoundingRadiusIn=robotBoundingRadiusIn;
        this.additionalClearanceIn=additionalClearanceIn;
        this.perimeterMarginIn=perimeterMarginIn;
        this.hiveCenterXIn=hiveCenterXIn;
        this.hiveCenterYIn=hiveCenterYIn;
        this.hiveExclusionRadiusIn=hiveExclusionRadiusIn;
        this.gridIn=gridIn;
        this.surveyedAndVerified=surveyedAndVerified;
    }
    public double clearance() { return robotBoundingRadiusIn + additionalClearanceIn; }
    /** SIMULATION ONLY placeholder configuration, not a validated field survey. */
    public static FieldConfig simulationExample() {
        return new FieldConfig(10, 3, 2, 72, 72, 32, 3, false);
    }
    public void requireVerifiedForRobot() {
        if (!surveyedAndVerified) throw new IllegalStateException(
            "Field geometry/robot footprint not surveyed: robot drive assist disabled");
    }
}
