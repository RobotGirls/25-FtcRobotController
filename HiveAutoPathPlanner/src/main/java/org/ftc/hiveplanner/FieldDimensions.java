package org.ftc.hiveplanner;

/** Nominal measurements from official BIOBUZZ 2026-27 manual section 9 (TU06).
 * General nominal tolerance +/- 1 inch; do not treat as exact robot obstacle extents. */
public final class FieldDimensions {
    public static final double FIELD_SIDE_IN = 144.0;
    public static final double TILE_SIDE_IN = 24.0;
    public static final double FRAME_WIDTH_IN = 49.46;
    public static final double FRAME_DEPTH_IN = 38.95;
    public static final double HIVE_PIVOT_HEIGHT_IN = 43.95;
    public static final double CELL_OPENING_WIDTH_IN = 20.0;
    public static final double CELL_OPENING_HEIGHT_IN = 14.0;
    public static final double CELL_DEPTH_IN = 12.0;
    public static final double FLOWER_TOP_HEIGHT_IN = 21.5;
    public static final double FIELD_NOMINAL_TOLERANCE_IN = 1.0;
    private FieldDimensions() {}
}
