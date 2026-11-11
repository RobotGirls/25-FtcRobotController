package org.firstinspires.ftc.teamcode.biobuzz2627.vision;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.limelight.LimelightShootingRange;
import org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms.ReadinessSignal;
import org.ftc.hiveplanner.Alliance;
import org.ftc.hiveplanner.FieldSide;

/** Selected-cluster detection, camera range, and shooting-readiness from one Limelight result. */
public final class LimelightTargeting implements ReadinessSignal {
    private final LimelightShootingRange calculator = new LimelightShootingRange();
    private Alliance alliance;
    private FieldSide side;
    private LimelightShootingRange.Measurement measurement;
    private long seenAt;

    public LimelightTargeting(Alliance alliance, FieldSide side) {
        setTarget(alliance, side);
    }

    public void setTarget(Alliance alliance, FieldSide side) {
        if (alliance == null || side == null) throw new IllegalArgumentException("Target is required");
        this.alliance = alliance;
        this.side = side;
        measurement = null;
        seenAt = 0;
    }

    public void update(LLResult result) {
        if (result == null || !result.isValid()) return;
        for (LLResultTypes.FiducialResult tag : result.getFiducialResults()) {
            if (BioBuzzTagClusters.matches(alliance, side, tag.getFiducialId())) {
                measurement = calculator.measure(tag);
                seenAt = now();
                return;
            }
        }
    }

    public boolean selectedTagFresh() {
        return measurement != null && now() - seenAt <= AutomationConfig.SELECTED_TAG_FRESH_MS;
    }

    public LimelightShootingRange.Measurement measurement() {
        return selectedTagFresh() ? measurement : null;
    }

    @Override
    public boolean ready() {
        return selectedTagFresh()
                && HiveAssistConfig.SHOOTING_RANGE_LIMITS_VERIFIED
                && calculator.isInVerifiedShootingWindow(measurement);
    }

    @Override
    public String description() {
        if (!selectedTagFresh()) return "selected tag unavailable";
        if (!HiveAssistConfig.SHOOTING_RANGE_LIMITS_VERIFIED) return "shooting range unverified";
        return ready() ? "range ready" : "move to shooting range";
    }

    private static long now() { return System.nanoTime() / 1_000_000L; }
}
