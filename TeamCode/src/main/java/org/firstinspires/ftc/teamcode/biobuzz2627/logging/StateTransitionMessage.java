package org.firstinspires.ftc.teamcode.biobuzz2627.logging;

import com.acmerobotics.roadrunner.Pose2d;

public final class StateTransitionMessage {
    public final long timestamp;
    public final String previousState;
    public final String newState;
    public final String reason;
    public final double x;
    public final double y;
    public final double heading;
    public final String mechanisms;

    public StateTransitionMessage(String previousState, String newState, String reason,
                                  Pose2d pose, String mechanisms) {
        timestamp = System.nanoTime();
        this.previousState = previousState;
        this.newState = newState;
        this.reason = reason;
        x = pose.position.x;
        y = pose.position.y;
        heading = pose.heading.toDouble();
        this.mechanisms = mechanisms;
    }
}
