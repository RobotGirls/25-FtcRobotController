package org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.biobuzz2627.limelight.LimelightShootingRange;

/**
 * Nonblocking turret controller that centers the selected Hive AprilTag in the Limelight image.
 * The drivetrain path does not depend on this controller or on tag visibility.
 */
public final class LimelightTurretAimer {
    private final DcMotor turret;
    private String status = "idle";
    private double errorDegrees;
    private double commandedPower;

    public LimelightTurretAimer(DcMotor turret) {
        this.turret = turret;
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        turret.setPower(0);
    }

    /** Call once per OpMode loop. This method never waits or blocks the drivetrain. */
    public void update(boolean aimingRequested, LimelightHiveTargetTracker tracker) {
        if (!aimingRequested) {
            stopWithStatus("idle");
            return;
        }
        if (!HiveAssistConfig.TURRET_AIMING_VERIFIED) {
            stopWithStatus("locked - verify direction and limits");
            return;
        }
        if (!tracker.selectedTagVisibleNow()) {
            stopWithStatus("searching - selected tag not visible");
            return;
        }

        LimelightShootingRange.Measurement measurement = tracker.latestRange();
        if (measurement == null || !Double.isFinite(measurement.txDegrees())) {
            stopWithStatus("waiting for valid tx");
            return;
        }

        errorDegrees = HiveAssistConfig.TURRET_TX_SIGN * measurement.txDegrees();
        if (Math.abs(errorDegrees) <= HiveAssistConfig.TURRET_ALIGNMENT_TOLERANCE_DEG) {
            stopWithStatus("aligned");
            return;
        }

        double magnitude = Math.abs(HiveAssistConfig.TURRET_KP * errorDegrees);
        magnitude = Range.clip(
                magnitude,
                HiveAssistConfig.TURRET_MIN_POWER,
                HiveAssistConfig.TURRET_MAX_POWER);
        commandedPower = Math.copySign(magnitude, errorDegrees);
        turret.setPower(commandedPower);
        status = "aiming";
    }

    public void stop() {
        stopWithStatus("stopped");
    }

    public String status() {
        return status;
    }

    public double errorDegrees() {
        return errorDegrees;
    }

    public double commandedPower() {
        return commandedPower;
    }

    private void stopWithStatus(String nextStatus) {
        turret.setPower(0);
        commandedPower = 0;
        status = nextStatus;
    }
}
