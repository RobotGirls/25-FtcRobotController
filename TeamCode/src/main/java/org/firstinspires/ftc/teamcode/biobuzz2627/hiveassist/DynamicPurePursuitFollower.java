package org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.ftc.hiveplanner.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Nonblocking, hold-to-run pure-pursuit controller for paths generated during TeleOp. */
@Config
public final class DynamicPurePursuitFollower {
    public static double LOOKAHEAD_IN = 12;
    public static double TRANSLATION_KP = 0.055;
    public static double HEADING_KP = 0.9;
    public static double MAX_TRANSLATION = 0.55;
    public static double MAX_TURN = 0.35;
    public static double WAYPOINT_TOLERANCE_IN = 2.0;
    public static double FINISH_TOLERANCE_IN = 1.5;
    public static double FINISH_HEADING_TOLERANCE_DEG = 4;
    public static double NO_PROGRESS_DISTANCE_IN = 0.5;
    public static long NO_PROGRESS_TIMEOUT_MS = 1200;
    public static long MAX_RUN_TIME_MS = 7000;

    private final MecanumDrive drive;
    private List<Vector2d> path = Collections.emptyList();
    private int segment;
    private double finalHeading;
    private boolean active;
    private boolean finished;
    private String status = "idle";
    private long startedAt;
    private long lastProgressAt;
    private Vector2d lastProgressPosition;

    public DynamicPurePursuitFollower(MecanumDrive drive) {
        this.drive = drive;
    }

    public void start(List<Point> fieldWaypoints, double finalHeading) {
        if (fieldWaypoints == null || fieldWaypoints.size() < 2) {
            throw new IllegalArgumentException("A generated path needs at least two points");
        }
        List<Vector2d> converted = new ArrayList<>();
        for (Point point : fieldWaypoints) {
            converted.add(new Vector2d(
                    point.x - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                    point.y - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN));
        }
        path = Collections.unmodifiableList(converted);
        segment = 0;
        this.finalHeading = finalHeading;
        active = true;
        finished = false;
        status = "following generated path";
        startedAt = nowMillis();
        lastProgressAt = startedAt;
        lastProgressPosition = drive.localizer.getPose().position;
    }

    public void update() {
        if (!active) return;
        drive.updatePoseEstimate();
        Pose2d pose = drive.localizer.getPose();
        Vector2d position = pose.position;
        Vector2d finish = path.get(path.size() - 1);
        double finalDistance = distance(position, finish);
        double headingError = wrap(finalHeading - pose.heading.toDouble());

        long now = nowMillis();
        if (distance(position, lastProgressPosition) >= NO_PROGRESS_DISTANCE_IN) {
            lastProgressPosition = position;
            lastProgressAt = now;
        }
        if (now - startedAt > MAX_RUN_TIME_MS) {
            stopAs("assist timed out");
            return;
        }
        if (now - lastProgressAt > NO_PROGRESS_TIMEOUT_MS) {
            stopAs("no progress — stopped");
            return;
        }

        while (segment < path.size() - 2
                && distance(position, path.get(segment + 1)) <= WAYPOINT_TOLERANCE_IN) {
            segment++;
        }

        if (finalDistance <= FINISH_TOLERANCE_IN
                && Math.abs(Math.toDegrees(headingError)) <= FINISH_HEADING_TOLERANCE_DEG) {
            stopAs("shooting position reached");
            return;
        }

        Vector2d lookahead = circleSegmentLookahead(
                position, path.get(segment), path.get(segment + 1), LOOKAHEAD_IN);
        double dx = lookahead.x - position.x;
        double dy = lookahead.y - position.y;
        double distance = Math.hypot(dx, dy);
        double heading = pose.heading.toDouble();
        double forwardError = Math.cos(heading) * dx + Math.sin(heading) * dy;
        double leftError = -Math.sin(heading) * dx + Math.cos(heading) * dy;
        double speed = Math.min(MAX_TRANSLATION, TRANSLATION_KP * distance);
        if (finalDistance < LOOKAHEAD_IN) {
            speed = Math.min(speed, Math.max(0.12, TRANSLATION_KP * finalDistance));
        }
        double scale = distance < 1e-6 ? 0 : speed / distance;
        drive.setDrivePowers(new PoseVelocity2d(
                new Vector2d(forwardError * scale, leftError * scale),
                clip(headingError * HEADING_KP, -MAX_TURN, MAX_TURN)));
    }

    public void cancel() {
        active = false;
        if (!finished) {
            status = "driver canceled";
        }
        stopMotors();
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isActive() {
        return active;
    }

    public String status() {
        return status;
    }

    public List<Vector2d> path() {
        return path;
    }

    private void stopAs(String reason) {
        active = false;
        finished = true;
        status = reason;
        stopMotors();
    }

    private void stopMotors() {
        drive.setDrivePowers(new PoseVelocity2d(new Vector2d(0, 0), 0));
    }

    private static Vector2d circleSegmentLookahead(Vector2d center, Vector2d start,
                                                    Vector2d end, double radius) {
        double sx = start.x - center.x;
        double sy = start.y - center.y;
        double dx = end.x - start.x;
        double dy = end.y - start.y;
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
        if (best < 0) return end;
        return new Vector2d(start.x + dx * best, start.y + dy * best);
    }

    private static double distance(Vector2d a, Vector2d b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    private static double wrap(double angle) {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
    }

    private static double clip(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static long nowMillis() {
        return System.nanoTime() / 1_000_000L;
    }
}
