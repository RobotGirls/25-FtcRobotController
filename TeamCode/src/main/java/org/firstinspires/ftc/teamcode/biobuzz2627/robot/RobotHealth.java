package org.firstinspires.ftc.teamcode.biobuzz2627.robot;

import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms.PieceSensor;

/** Read-only startup verification; it never moves a mechanism. */
public final class RobotHealth {
    private final boolean sensorsAvailable;

    public RobotHealth(PieceSensor intakeSensor, PieceSensor transferSensor) {
        sensorsAvailable = intakeSensor.available() && transferSensor.available();
    }

    public boolean mechanismsReady() {
        return AutomationConfig.MECHANISMS_VERIFIED
                && AutomationConfig.SHOOTER_TARGET_TICKS_PER_SECOND != 0
                && AutomationConfig.INTAKE_POWER != 0
                && AutomationConfig.TRANSFER_POWER != 0;
    }

    public boolean sensorsReady() {
        return !AutomationConfig.PIECE_SENSORS_VERIFIED || sensorsAvailable;
    }

    public boolean completeAutoReady() {
        return AutomationConfig.COMPLETE_AUTO_VERIFIED
                && AutomationConfig.PARK_GOALS_VERIFIED
                && mechanismsReady() && sensorsReady();
    }

    public String summary() {
        if (!mechanismsReady()) return "mechanism calibration locked";
        if (!sensorsReady()) return "configured piece sensors missing";
        if (!AutomationConfig.PARK_GOALS_VERIFIED) return "park goals unverified";
        if (!AutomationConfig.COMPLETE_AUTO_VERIFIED) return "complete auto not approved";
        return "ready";
    }
}
