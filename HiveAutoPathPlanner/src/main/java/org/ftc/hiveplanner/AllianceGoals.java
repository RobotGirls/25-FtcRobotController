package org.ftc.hiveplanner;
import java.util.*;

/** Team-surveyed valid shooting destinations for each alliance, in field inches. */
public final class AllianceGoals {
    private final List<Point> red, blue;
    public AllianceGoals(List<Point> red, List<Point> blue) {
        if (red == null || blue == null) throw new IllegalArgumentException("Null alliance goals");
        this.red = Collections.unmodifiableList(new ArrayList<>(red));
        this.blue = Collections.unmodifiableList(new ArrayList<>(blue));
    }
    public List<Point> forAlliance(Alliance alliance) {
        if (alliance == null) throw new IllegalArgumentException("Select an alliance");
        return alliance == Alliance.RED ? red : blue;
    }
}
