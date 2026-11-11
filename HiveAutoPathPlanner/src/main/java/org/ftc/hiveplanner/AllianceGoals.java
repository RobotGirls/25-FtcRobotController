package org.ftc.hiveplanner;
import java.util.*;

/** Team-surveyed valid shooting destinations for each alliance, in field inches. */
public final class AllianceGoals {
    private final List<Point> redAudience, redFar, blueAudience, blueFar;

    /** Backwards-compatible constructor: use the same candidates for either side. */
    public AllianceGoals(List<Point> red, List<Point> blue) {
        this(red, red, blue, blue);
    }

    public AllianceGoals(List<Point> redAudience, List<Point> redFar,
                         List<Point> blueAudience, List<Point> blueFar) {
        if (redAudience == null || redFar == null || blueAudience == null || blueFar == null)
            throw new IllegalArgumentException("Null alliance/side goals");
        this.redAudience = copy(redAudience);
        this.redFar = copy(redFar);
        this.blueAudience = copy(blueAudience);
        this.blueFar = copy(blueFar);
    }

    public List<Point> forAlliance(Alliance alliance) {
        return forTarget(alliance, FieldSide.AUDIENCE);
    }

    public List<Point> forTarget(Alliance alliance, FieldSide side) {
        if (alliance == null) throw new IllegalArgumentException("Select an alliance");
        if (side == null) throw new IllegalArgumentException("Select AUDIENCE or FAR");
        if (alliance == Alliance.RED) return side == FieldSide.AUDIENCE ? redAudience : redFar;
        return side == FieldSide.AUDIENCE ? blueAudience : blueFar;
    }

    private static List<Point> copy(List<Point> points) {
        return Collections.unmodifiableList(new ArrayList<>(points));
    }
}
