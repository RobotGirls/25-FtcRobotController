package org.firstinspires.ftc.teamcode.biobuzz2627.navigation;

import com.acmerobotics.roadrunner.Pose2d;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;
import org.ftc.hiveplanner.Point;

import java.util.List;

/** Follows a tuned path and safely replans after large deviation or localization correction. */
public final class ReplanningPathController {
    private final MecanumDrive drive;
    private final HiveAdaptivePlanner planner;
    private final AdaptivePurePursuitFollower follower;
    private Point goal;
    private double finalHeading;
    private long lastPlanAt;
    private boolean replanRequested;
    private String status = "idle";

    public ReplanningPathController(MecanumDrive drive, HiveAdaptivePlanner planner) {
        this.drive = drive;
        this.planner = planner;
        follower = new AdaptivePurePursuitFollower(drive);
    }

    public boolean startBest(List<Point> goals, double finalHeading) {
        HiveAdaptivePlanner.Plan plan = planner.planBest(currentFieldPosition(), goals);
        if (!plan.valid()) {
            status = "no safe path";
            return false;
        }
        this.goal = plan.goal;
        this.finalHeading = finalHeading;
        follower.start(plan.waypoints, finalHeading);
        lastPlanAt = now();
        replanRequested = false;
        status = "path generated";
        return true;
    }

    public boolean startBestFacing(List<Point> goals, double faceX, double faceY) {
        HiveAdaptivePlanner.Plan plan = planner.planBest(currentFieldPosition(), goals);
        if (!plan.valid()) {
            status = "no safe path";
            return false;
        }
        double heading = Math.atan2(faceY - plan.goal.y, faceX - plan.goal.x);
        this.goal = plan.goal;
        this.finalHeading = heading;
        follower.start(plan.waypoints, heading);
        lastPlanAt = now();
        replanRequested = false;
        status = "path generated";
        return true;
    }

    public boolean startTo(Point goal, double finalHeading) {
        HiveAdaptivePlanner.Plan plan = planner.planTo(currentFieldPosition(), goal);
        if (!plan.valid()) {
            status = "no safe path";
            return false;
        }
        this.goal = goal;
        this.finalHeading = finalHeading;
        follower.start(plan.waypoints, finalHeading);
        lastPlanAt = now();
        replanRequested = false;
        status = "path generated";
        return true;
    }

    public void update() {
        if (!follower.active()) return;
        Pose2d pose = drive.localizer.getPose();
        double deviation = follower.distanceFromPath(
                new AdaptivePurePursuitFollower.PointInRoadRunner(
                        pose.position.x, pose.position.y));
        boolean deviated = deviation > AutomationConfig.PATH_REPLAN_DEVIATION_IN;
        if ((replanRequested || deviated)
                && now() - lastPlanAt >= AutomationConfig.PATH_REPLAN_MIN_INTERVAL_MS) {
            HiveAdaptivePlanner.Plan plan = planner.planTo(currentFieldPosition(), goal);
            if (plan.valid()) {
                follower.cancel("replanning");
                follower.start(plan.waypoints, finalHeading);
                lastPlanAt = now();
                replanRequested = false;
                status = deviated ? "replanned after deviation" : "replanned after pose correction";
            } else {
                follower.cancel("replan failed");
                status = "replan failed";
                return;
            }
        }
        follower.update();
        if (follower.finished()) status = follower.status();
    }

    public void requestReplan() { replanRequested = true; }
    public void cancel(String reason) { follower.cancel(reason); status = reason; }
    public boolean active() { return follower.active(); }
    public boolean finished() { return follower.finished(); }
    public boolean succeeded() { return follower.finished() && "path complete".equals(follower.status()); }
    public String status() { return status + " / " + follower.status(); }

    private Point currentFieldPosition() {
        Pose2d pose = drive.localizer.getPose();
        return new Point(
                pose.position.x + HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                pose.position.y + HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN);
    }

    private static long now() { return System.nanoTime() / 1_000_000L; }
}
