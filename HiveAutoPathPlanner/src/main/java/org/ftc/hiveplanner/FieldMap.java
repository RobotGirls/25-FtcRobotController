package org.ftc.hiveplanner;

/** Treats the robot as a bounding circle. Excludes center HIVE via surveyed envelope.
 * Four perimeter FLOWERS must be included in a verified survey, or surrounded by
 * a sufficient perimeter exclusion; static model cannot detect other robots. */
public final class FieldMap {
    private final FieldConfig config;
    public FieldMap(FieldConfig config) { this.config=config; }
    public boolean isSafe(Point p) {
        double wall=config.clearance()+config.perimeterMarginIn
            +FieldDimensions.FIELD_NOMINAL_TOLERANCE_IN;
        if (p.x < wall || p.y < wall || p.x > FieldDimensions.FIELD_SIDE_IN-wall
            || p.y > FieldDimensions.FIELD_SIDE_IN-wall) return false;
        double dx=p.x-config.hiveCenterXIn, dy=p.y-config.hiveCenterYIn;
        double exclusion=config.hiveExclusionRadiusIn+config.clearance()
            +FieldDimensions.FIELD_NOMINAL_TOLERANCE_IN;
        return dx*dx+dy*dy > exclusion*exclusion;
    }
    /** Samples the complete straight-line movement; includes endpoints. */
    public boolean safeSegment(Point a, Point b) {
        double distance=a.distance(b);
        int samples=Math.max(1,(int)Math.ceil(distance/0.5));
        for(int i=0;i<=samples;i++) {
            double t=i/(double)samples;
            if (!isSafe(new Point(a.x+(b.x-a.x)*t,a.y+(b.y-a.y)*t))) return false;
        }
        return true;
    }
}
