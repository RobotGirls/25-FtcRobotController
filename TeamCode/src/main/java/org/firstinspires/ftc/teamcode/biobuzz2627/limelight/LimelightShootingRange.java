package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.hardware.limelightvision.LLResultTypes;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;

/** Calculates camera-to-AprilTag range without owning the Limelight or any robot hardware. */
public final class LimelightShootingRange {
    public static final class Measurement {
        private final int tagId;
        private final double xMeters;
        private final double yMeters;
        private final double zMeters;
        private final double distanceMeters;
        private final double txDegrees;
        private final double tyDegrees;

        private Measurement(int tagId, double xMeters, double yMeters, double zMeters,
                            double distanceMeters, double txDegrees, double tyDegrees) {
            this.tagId = tagId;
            this.xMeters = xMeters;
            this.yMeters = yMeters;
            this.zMeters = zMeters;
            this.distanceMeters = distanceMeters;
            this.txDegrees = txDegrees;
            this.tyDegrees = tyDegrees;
        }

        public int tagId() { return tagId; }
        public double xMeters() { return xMeters; }
        public double yMeters() { return yMeters; }
        public double zMeters() { return zMeters; }
        public double distanceMeters() { return distanceMeters; }
        public double txDegrees() { return txDegrees; }
        public double tyDegrees() { return tyDegrees; }
    }

    public Measurement measure(LLResultTypes.FiducialResult tag) {
        Pose3D tagPose = tag.getTargetPoseCameraSpace();
        double x = tagPose.getPosition().x;
        double y = tagPose.getPosition().y;
        double z = tagPose.getPosition().z;
        return new Measurement(
                tag.getFiducialId(),
                x,
                y,
                z,
                Math.sqrt(x * x + y * y + z * z),
                tag.getTargetXDegrees(),
                tag.getTargetYDegrees());
    }

    /** True only after the team calibrates and explicitly verifies the configured window. */
    public boolean isInVerifiedShootingWindow(Measurement measurement) {
        return measurement != null
                && HiveAssistConfig.SHOOTING_RANGE_LIMITS_VERIFIED
                && measurement.xMeters() >= HiveAssistConfig.MIN_SHOOTING_X_METERS
                && measurement.xMeters() <= HiveAssistConfig.MAX_SHOOTING_X_METERS
                && measurement.distanceMeters() >= HiveAssistConfig.MIN_SHOOTING_DISTANCE_METERS
                && measurement.distanceMeters() <= HiveAssistConfig.MAX_SHOOTING_DISTANCE_METERS;
    }
}
