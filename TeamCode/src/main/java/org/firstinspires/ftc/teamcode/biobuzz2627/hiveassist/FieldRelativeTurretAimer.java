package org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.biobuzz2627.limelight.LimelightShootingRange;
import org.ftc.hiveplanner.Alliance;
import org.ftc.hiveplanner.FieldSide;

/**
 * A continuous-rotation, encoder-tracked turret controller.
 *
 * <p>Without a tag, robot field pose plus the selected Hive coordinate supplies the PID target.
 * When the selected tag is visible, Limelight tx becomes the fine PID error. The resulting small
 * encoder trim is retained through tag loss so the turret does not jump back to the coarse model.</p>
 */
public final class FieldRelativeTurretAimer {
    public enum Mode { LOCKED, COARSE_PINPOINT, FINE_LIMELIGHT, ALIGNED, STOPPED }

    private final DcMotorEx turret;
    private final double ticksPerTurretRevolution;
    private Mode mode = Mode.STOPPED;
    private Alliance lastAlliance;
    private FieldSide lastSide;
    private long lastUpdateNanos;
    private double integral;
    private double previousError;
    private double visionTrimDegrees;
    private double currentTurretDegrees;
    private double desiredTurretDegrees;
    private double fieldBearingDegrees;
    private double errorDegrees;
    private double commandedPower;

    public FieldRelativeTurretAimer(DcMotorEx turret) {
        this.turret = turret;
        double motorTicks = turret.getMotorType().getTicksPerRev();
        ticksPerTurretRevolution = motorTicks
                * HiveAssistConfig.TURRET_MOTOR_REVS_PER_TURRET_REV;
        turret.setPower(0);
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        // This establishes "turret forward". The mechanism must physically face robot-forward
        // before INIT, unless an absolute encoder/home sensor is added later.
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void update(Pose2d robotPose,
                       boolean pinpointReady,
                       HiveTargetSelection selection,
                       LimelightHiveTargetTracker tracker) {
        if (!HiveAssistConfig.TURRET_AIMING_VERIFIED) {
            stopAs(Mode.LOCKED);
            return;
        }
        if (!pinpointReady || robotPose == null || !finitePose(robotPose)
                || !Double.isFinite(ticksPerTurretRevolution)
                || ticksPerTurretRevolution <= 0) {
            stopAs(Mode.STOPPED);
            return;
        }

        if (selection.alliance() != lastAlliance || selection.side() != lastSide) {
            lastAlliance = selection.alliance();
            lastSide = selection.side();
            visionTrimDegrees = 0;
            resetPid();
        }

        HiveAimTarget target = HiveAimTarget.forSelection(selection.alliance(), selection.side());
        double heading = robotPose.heading.toDouble();
        double cos = Math.cos(heading);
        double sin = Math.sin(heading);
        double turretFieldX = robotPose.position.x
                + cos * HiveAssistConfig.TURRET_OFFSET_FORWARD_IN
                - sin * HiveAssistConfig.TURRET_OFFSET_LEFT_IN;
        double turretFieldY = robotPose.position.y
                + sin * HiveAssistConfig.TURRET_OFFSET_FORWARD_IN
                + cos * HiveAssistConfig.TURRET_OFFSET_LEFT_IN;
        double targetX = target.fieldXIn - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN;
        double targetY = target.fieldYIn - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN;

        fieldBearingDegrees = Math.toDegrees(
                Math.atan2(targetY - turretFieldY, targetX - turretFieldX));
        double coarseRelativeDegrees = wrapDegrees(
                fieldBearingDegrees - Math.toDegrees(heading)
                        - HiveAssistConfig.TURRET_FORWARD_OFFSET_DEG);
        currentTurretDegrees = HiveAssistConfig.TURRET_ENCODER_SIGN
                * turret.getCurrentPosition() * 360.0 / ticksPerTurretRevolution;
        desiredTurretDegrees = nearestEquivalent(
                currentTurretDegrees, coarseRelativeDegrees + visionTrimDegrees);

        boolean visionValid = tracker.selectedTagVisibleNow();
        LimelightShootingRange.Measurement measurement = tracker.latestRange();
        if (visionValid && measurement != null && Double.isFinite(measurement.txDegrees())) {
            // Once visible, tx verifies and refines the geometric/encoder solution.
            errorDegrees = HiveAssistConfig.TURRET_TX_SIGN * measurement.txDegrees();
            visionTrimDegrees = Range.clip(
                    wrapDegrees(currentTurretDegrees - coarseRelativeDegrees),
                    -HiveAssistConfig.TURRET_MAX_VISION_TRIM_DEG,
                    HiveAssistConfig.TURRET_MAX_VISION_TRIM_DEG);
            applyPid(errorDegrees,
                    HiveAssistConfig.TURRET_VISION_KP,
                    HiveAssistConfig.TURRET_VISION_KI,
                    HiveAssistConfig.TURRET_VISION_KD,
                    HiveAssistConfig.TURRET_ALIGNMENT_TOLERANCE_DEG,
                    Mode.FINE_LIMELIGHT);
        } else {
            errorDegrees = desiredTurretDegrees - currentTurretDegrees;
            applyPid(errorDegrees,
                    HiveAssistConfig.TURRET_COARSE_KP,
                    HiveAssistConfig.TURRET_COARSE_KI,
                    HiveAssistConfig.TURRET_COARSE_KD,
                    HiveAssistConfig.TURRET_COARSE_TOLERANCE_DEG,
                    Mode.COARSE_PINPOINT);
        }
    }

    public void stop() {
        stopAs(Mode.STOPPED);
    }

    public Mode mode() { return mode; }
    public double currentTurretDegrees() { return currentTurretDegrees; }
    public double desiredTurretDegrees() { return desiredTurretDegrees; }
    public double fieldBearingDegrees() { return fieldBearingDegrees; }
    public double errorDegrees() { return errorDegrees; }
    public double commandedPower() { return commandedPower; }
    public double visionTrimDegrees() { return visionTrimDegrees; }

    private void applyPid(double error, double kp, double ki, double kd,
                          double tolerance, Mode activeMode) {
        long now = System.nanoTime();
        double dt = lastUpdateNanos == 0 ? 0 : (now - lastUpdateNanos) / 1e9;
        if (mode != activeMode || dt <= 0 || dt > 0.25) {
            integral = 0;
            previousError = error;
            dt = 0;
        }
        lastUpdateNanos = now;

        if (Math.abs(error) <= tolerance) {
            turret.setPower(0);
            commandedPower = 0;
            integral = 0;
            previousError = error;
            mode = Mode.ALIGNED;
            return;
        }

        double derivative = 0;
        if (dt > 0) {
            integral = Range.clip(
                    integral + error * dt,
                    -HiveAssistConfig.TURRET_INTEGRAL_LIMIT_DEG_SECONDS,
                    HiveAssistConfig.TURRET_INTEGRAL_LIMIT_DEG_SECONDS);
            derivative = (error - previousError) / dt;
        }
        previousError = error;

        double output = kp * error + ki * integral + kd * derivative;
        double magnitude = Range.clip(
                Math.abs(output),
                HiveAssistConfig.TURRET_MIN_POWER,
                HiveAssistConfig.TURRET_MAX_POWER);
        commandedPower = HiveAssistConfig.TURRET_POWER_SIGN
                * Math.copySign(magnitude, output);
        turret.setPower(commandedPower);
        mode = activeMode;
    }

    private void stopAs(Mode nextMode) {
        turret.setPower(0);
        commandedPower = 0;
        mode = nextMode;
        resetPid();
    }

    private void resetPid() {
        lastUpdateNanos = 0;
        integral = 0;
        previousError = 0;
    }

    private static double nearestEquivalent(double currentDegrees, double wrappedTargetDegrees) {
        return currentDegrees + wrapDegrees(wrappedTargetDegrees - currentDegrees);
    }

    private static double wrapDegrees(double degrees) {
        double wrapped = degrees % 360.0;
        if (wrapped <= -180) wrapped += 360;
        if (wrapped > 180) wrapped -= 360;
        return wrapped;
    }

    private static boolean finitePose(Pose2d pose) {
        return Double.isFinite(pose.position.x)
                && Double.isFinite(pose.position.y)
                && Double.isFinite(pose.heading.toDouble());
    }
}
