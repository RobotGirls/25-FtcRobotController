package org.firstinspires.ftc.teamcode.biobuzz2627.logging;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.ftc.FlightRecorder;

/** Writes durable autonomous transitions into the existing Road Runner log. */
public final class AutoEventLogger {
    private String previous = "NONE";

    public void entered(Enum<?> state, String reason, Pose2d pose, String mechanisms) {
        String next = state.name();
        FlightRecorder.write("BIOBUZZ_AUTO_STATE", new StateTransitionMessage(
                previous, next, reason, pose, mechanisms));
        previous = next;
    }
}
