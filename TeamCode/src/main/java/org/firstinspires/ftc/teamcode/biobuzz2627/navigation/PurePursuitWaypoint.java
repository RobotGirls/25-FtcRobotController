package org.firstinspires.ftc.teamcode.biobuzz2627.navigation;

import org.ftc.hiveplanner.Point;

/** A generated path point with segment-specific Pure Pursuit tuning. */
public final class PurePursuitWaypoint {
    public final Point point;
    public final double moveSpeed;
    public final double turnSpeed;
    public final double lookaheadInches;
    public final double toleranceInches;

    public PurePursuitWaypoint(Point point, double moveSpeed, double turnSpeed,
                               double lookaheadInches, double toleranceInches) {
        if (point == null || moveSpeed <= 0 || turnSpeed <= 0
                || lookaheadInches <= 0 || toleranceInches <= 0) {
            throw new IllegalArgumentException("Invalid waypoint tuning");
        }
        this.point = point;
        this.moveSpeed = moveSpeed;
        this.turnSpeed = turnSpeed;
        this.lookaheadInches = lookaheadInches;
        this.toleranceInches = toleranceInches;
    }
}
