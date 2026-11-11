package org.firstinspires.ftc.teamcode.biobuzz2627.strategy;

import org.ftc.hiveplanner.Alliance;
import org.ftc.hiveplanner.FieldSide;
import org.ftc.hiveplanner.Point;

/** Immutable choices frozen when autonomous starts. */
public final class AutoStrategy {
    public final Alliance alliance;
    public final FieldSide shootingSide;
    public final Point parkGoal;

    public AutoStrategy(Alliance alliance, FieldSide shootingSide, Point parkGoal) {
        if (alliance == null || shootingSide == null || parkGoal == null) {
            throw new IllegalArgumentException("Complete strategy is required");
        }
        this.alliance = alliance;
        this.shootingSide = shootingSide;
        this.parkGoal = parkGoal;
    }
}
