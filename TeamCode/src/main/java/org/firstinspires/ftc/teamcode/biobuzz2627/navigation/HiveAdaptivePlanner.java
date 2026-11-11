package org.firstinspires.ftc.teamcode.biobuzz2627.navigation;

import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;
import org.ftc.hiveplanner.AStarPlanner;
import org.ftc.hiveplanner.GoalSelector;
import org.ftc.hiveplanner.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Converts safe A* output into a segment-tuned Pure Pursuit path. */
public final class HiveAdaptivePlanner {
    public static final class Plan {
        public final List<PurePursuitWaypoint> waypoints;
        public final Point goal;

        private Plan(List<PurePursuitWaypoint> waypoints, Point goal) {
            this.waypoints = Collections.unmodifiableList(waypoints);
            this.goal = goal;
        }

        public boolean valid() { return waypoints.size() >= 2; }
    }

    private final GoalSelector selector;

    public HiveAdaptivePlanner(AStarPlanner planner) {
        selector = new GoalSelector(planner);
    }

    public Plan planBest(Point start, List<Point> goals) {
        List<Point> path = selector.bestPath(start, goals);
        if (path.size() < 2) return new Plan(Collections.<PurePursuitWaypoint>emptyList(), null);
        return new Plan(tune(path), path.get(path.size() - 1));
    }

    public Plan planTo(Point start, Point goal) {
        return planBest(start, Collections.singletonList(goal));
    }

    private List<PurePursuitWaypoint> tune(List<Point> path) {
        List<PurePursuitWaypoint> tuned = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            boolean last = i == path.size() - 1;
            boolean corner = i > 0 && i < path.size() - 1 && isCorner(path, i);
            double speed = last ? AutomationConfig.PATH_FINAL_MOVE_SPEED
                    : corner ? AutomationConfig.PATH_CORNER_MOVE_SPEED
                    : AutomationConfig.PATH_DEFAULT_MOVE_SPEED;
            double lookahead = last ? AutomationConfig.PATH_FINAL_LOOKAHEAD_IN
                    : corner ? AutomationConfig.PATH_CORNER_LOOKAHEAD_IN
                    : AutomationConfig.PATH_DEFAULT_LOOKAHEAD_IN;
            tuned.add(new PurePursuitWaypoint(
                    path.get(i), speed, AutomationConfig.PATH_DEFAULT_TURN_SPEED,
                    lookahead, AutomationConfig.PATH_WAYPOINT_TOLERANCE_IN));
        }
        return tuned;
    }

    private static boolean isCorner(List<Point> path, int index) {
        Point before = path.get(index - 1);
        Point at = path.get(index);
        Point after = path.get(index + 1);
        double a = Math.atan2(at.y - before.y, at.x - before.x);
        double b = Math.atan2(after.y - at.y, after.x - at.x);
        return Math.abs(wrap(b - a)) > Math.toRadians(20);
    }

    private static double wrap(double angle) {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
    }
}
