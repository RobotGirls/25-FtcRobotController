package org.firstinspires.ftc.teamcode.biobuzz2627.navigation;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Nonblocking Pure Pursuit follower using tuning from each generated waypoint. */
public final class AdaptivePurePursuitFollower {
    private static final double TRANSLATION_KP = 0.055;
    private static final double HEADING_KP = 0.9;

    private final MecanumDrive drive;
    private List<PurePursuitWaypoint> path = Collections.emptyList();
    private int segment;
    private double finalHeading;
    private boolean active;
    private boolean finished;
    private String status = "idle";
    private long startedAt;
    private long lastProgressAt;
    private Vector2d lastProgressPosition;

    public AdaptivePurePursuitFollower(MecanumDrive drive) { this.drive = drive; }

    public void start(List<PurePursuitWaypoint> waypoints, double finalHeading) {
        if (waypoints == null || waypoints.size() < 2) {
            throw new IllegalArgumentException("Path requires at least two waypoints");
        }
        path = Collections.unmodifiableList(new ArrayList<>(waypoints));
        segment = 0;
        this.finalHeading = finalHeading;
        active = true;
        finished = false;
        status = "following tuned path";
        startedAt = now();
        lastProgressAt = startedAt;
        lastProgressPosition = drive.localizer.getPose().position;
    }

    public void update() {
        if (!active) return;
        drive.updatePoseEstimate();
        Pose2d pose = drive.localizer.getPose();
        Vector2d position = pose.position;
        Vector2d finish = roadRunner(path.get(path.size() - 1));
        double finalDistance = distance(position, finish);
        double headingError = wrap(finalHeading - pose.heading.toDouble());

        long now = now();
        if (distance(position, lastProgressPosition) >= 0.5) {
            lastProgressPosition = position;
            lastProgressAt = now;
        }
        if (now - startedAt > AutomationConfig.PATH_MAX_RUN_MS) {
            finish("path timed out");
            return;
        }
        if (now - lastProgressAt > AutomationConfig.PATH_NO_PROGRESS_TIMEOUT_MS) {
            finish("path made no progress");
            return;
        }

        while (segment < path.size() - 2
                && distance(position, roadRunner(path.get(segment + 1)))
                <= path.get(segment + 1).toleranceInches) segment++;

        if (finalDistance <= AutomationConfig.PATH_FINISH_TOLERANCE_IN
                && Math.abs(Math.toDegrees(headingError))
                <= AutomationConfig.PATH_FINISH_HEADING_TOLERANCE_DEG) {
            finish("path complete");
            return;
        }

        PurePursuitWaypoint targetSettings = path.get(segment + 1);
        Vector2d start = roadRunner(path.get(segment));
        Vector2d end = roadRunner(targetSettings);
        Vector2d lookahead = circleSegmentLookahead(
                position, start, end, targetSettings.lookaheadInches);
        double dx = lookahead.x - position.x;
        double dy = lookahead.y - position.y;
        double d = Math.hypot(dx, dy);
        double heading = pose.heading.toDouble();
        double forward = Math.cos(heading) * dx + Math.sin(heading) * dy;
        double left = -Math.sin(heading) * dx + Math.cos(heading) * dy;
        double speed = Math.min(targetSettings.moveSpeed, TRANSLATION_KP * d);
        double scale = d < 1e-6 ? 0 : speed / d;
        drive.setDrivePowers(new PoseVelocity2d(
                new Vector2d(forward * scale, left * scale),
                clip(headingError * HEADING_KP,
                        -targetSettings.turnSpeed, targetSettings.turnSpeed)));
    }

    public void cancel(String reason) {
        active = false;
        if (!finished) status = reason;
        stopMotors();
    }

    public boolean active() { return active; }
    public boolean finished() { return finished; }
    public String status() { return status; }
    public List<PurePursuitWaypoint> path() { return path; }

    public double distanceFromPath(PointInRoadRunner point) {
        if (path.size() < 2) return Double.POSITIVE_INFINITY;
        double best = Double.POSITIVE_INFINITY;
        for (int i = 1; i < path.size(); i++) {
            best = Math.min(best, segmentDistance(
                    new Vector2d(point.x, point.y), roadRunner(path.get(i - 1)), roadRunner(path.get(i))));
        }
        return best;
    }

    public static final class PointInRoadRunner {
        public final double x, y;
        public PointInRoadRunner(double x, double y) { this.x = x; this.y = y; }
    }

    private void finish(String reason) {
        active = false;
        finished = true;
        status = reason;
        stopMotors();
    }

    private void stopMotors() {
        drive.setDrivePowers(new PoseVelocity2d(new Vector2d(0, 0), 0));
    }

    private static Vector2d roadRunner(PurePursuitWaypoint waypoint) {
        return new Vector2d(
                waypoint.point.x - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                waypoint.point.y - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN);
    }

    private static Vector2d circleSegmentLookahead(Vector2d center, Vector2d start,
                                                    Vector2d end, double radius) {
        double sx = start.x - center.x, sy = start.y - center.y;
        double dx = end.x - start.x, dy = end.y - start.y;
        double a = dx * dx + dy * dy;
        if (a < 1e-9) return end;
        double b = 2 * (sx * dx + sy * dy);
        double c = sx * sx + sy * sy - radius * radius;
        double discriminant = b * b - 4 * a * c;
        double best = -1;
        if (discriminant >= 0) {
            double root = Math.sqrt(discriminant);
            double t1 = (-b - root) / (2 * a);
            double t2 = (-b + root) / (2 * a);
            if (t1 >= 0 && t1 <= 1) best = t1;
            if (t2 >= 0 && t2 <= 1) best = Math.max(best, t2);
        }
        return best < 0 ? end : new Vector2d(start.x + dx * best, start.y + dy * best);
    }

    private static double segmentDistance(Vector2d p, Vector2d a, Vector2d b) {
        double dx = b.x - a.x, dy = b.y - a.y;
        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared < 1e-9) return distance(p, a);
        double t = clip(((p.x - a.x) * dx + (p.y - a.y) * dy) / lengthSquared, 0, 1);
        return distance(p, new Vector2d(a.x + t * dx, a.y + t * dy));
    }

    private static double distance(Vector2d a, Vector2d b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }
    private static double wrap(double a) { return Math.atan2(Math.sin(a), Math.cos(a)); }
    private static double clip(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    private static long now() { return System.nanoTime() / 1_000_000L; }
}
