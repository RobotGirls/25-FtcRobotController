package org.team25.fieldviewer;

import com.acmerobotics.roadrunner.Pose2d;

import org.firstinspires.ftc.teamcode.purepursuit.PurePursuitPath;
import org.firstinspires.ftc.teamcode.purepursuit.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A path built in the FieldViewer's path editor. The first point is the start, the rest are
 * waypoints. It can be simulated like any other route and copied out as Java to paste into
 * purepursuit/paths.
 */
final class Sketch {
    /** One waypoint; speed / lookahead override the path's defaults for the segment ending here. */
    static final class Point {
        final double x, y;
        final Double speed, lookahead;
        /** Overrides the path-wide direction for the segment ending here. */
        final Boolean backwards;
        /** Seconds to wait after arriving at this waypoint. */
        final double wait;
        /** Turn 180 degrees in place after arriving at this waypoint. */
        final boolean turnAround;

        Point(double x, double y, Double speed, Double lookahead, Boolean backwards, double wait,
              boolean turnAround) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.lookahead = lookahead;
            this.backwards = backwards;
            this.wait = wait;
            this.turnAround = turnAround;
        }
    }

    final List<Point> points = new ArrayList<>();
    String name = "";
    boolean reversed;
    double moveSpeed = 0.6;
    double followDistance = 16;
    /** Field headings in radians; null means "not set". */
    Double startHeading, endHeading, fixedHeading;

    void add(double x, double y, Double speed, Double lookahead, Boolean backwards, double wait,
             boolean turnAround) {
        points.add(new Point(x, y, speed, lookahead, backwards, wait, turnAround));
    }

    boolean isUsable() {
        return points.size() >= 2;
    }

    /** The chosen start heading, or facing the first waypoint (away from it when reversed). */
    double startHeading() {
        if (startHeading != null) {
            return startHeading;
        }
        Point a = points.get(0), b = points.get(1);
        double heading = Math.atan2(b.y - a.y, b.x - a.x) + (isBackwards(b) ? Math.PI : 0);
        return Math.atan2(Math.sin(heading), Math.cos(heading));
    }

    private String displayName() {
        return name == null || name.trim().isEmpty() ? "My Route" : name.trim();
    }

    Route toRoute() {
        Point start = points.get(0);
        Route.Builder route = Route.builder("Sketch", Route.Alliance.NONE,
                        new Pose2d(start.x, start.y, startHeading()))
                .description("Built in the FieldViewer");
        if (start.turnAround) route.turn(Math.PI);
        if (start.wait > 0) route.waitSeconds(start.wait);

        int leg = 1;
        for (int first = 1; first < points.size();) {
            boolean backwards = isBackwards(points.get(first));
            int last = first;
            while (last + 1 < points.size()
                    && points.get(last).wait <= 0 && !points.get(last).turnAround
                    && isBackwards(points.get(last + 1)) == backwards) {
                last++;
            }
            PurePursuitPath.Builder path = PurePursuitPath.builder()
                    .name(displayName() + (hasMultipleLegs() ? " leg " + leg : ""));
            for (int i = first; i <= last; i++) {
                Point p = points.get(i);
                path.moveSpeed(p.speed != null ? p.speed : moveSpeed)
                        .followDistance(p.lookahead != null ? p.lookahead : followDistance)
                        .to(p.x, p.y);
            }
            if (backwards) path.reversed();
            if (fixedHeading != null) path.fixedHeading(fixedHeading);
            if (last == points.size() - 1 && endHeading != null) path.endHeading(endHeading);
            route.follow(path.build());
            if (points.get(last).turnAround) route.turn(Math.PI);
            if (points.get(last).wait > 0) route.waitSeconds(points.get(last).wait);
            first = last + 1;
            leg++;
        }
        return route.build();
    }

    /** Java to paste into BioBuzzRoutes / TestRoutes. */
    String toJava() {
        Point start = points.get(0);
        String constant = displayName().toUpperCase(Locale.US).replaceAll("[^A-Z0-9]+", "_").replaceAll("^_|_$", "");
        if (constant.isEmpty() || Character.isDigit(constant.charAt(0))) {
            constant = "ROUTE_" + constant;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("public static final Route ").append(constant).append(" = Route.builder(\n")
                .append("                \"").append(displayName().replace("\"", "'")).append("\", Alliance.NONE, new Pose2d(")
                .append(num(start.x)).append(", ").append(num(start.y)).append(", Math.toRadians(")
                .append(num(Math.toDegrees(startHeading()))).append(")))\n");
        if (start.turnAround) sb.append("        .turn(Math.PI)\n");
        if (start.wait > 0) sb.append("        .waitSeconds(").append(num(start.wait)).append(")\n");

        int leg = 1;
        for (int first = 1; first < points.size();) {
            boolean backwards = isBackwards(points.get(first));
            int last = first;
            while (last + 1 < points.size() && points.get(last).wait <= 0 && !points.get(last).turnAround
                    && isBackwards(points.get(last + 1)) == backwards) last++;
            sb.append("        .follow(PurePursuitPath.builder().name(\"")
                    .append(displayName().toLowerCase(Locale.US).replace("\"", "'"));
            if (hasMultipleLegs()) sb.append(" leg ").append(leg);
            sb.append("\")\n");
            double speed = 0.6, lookahead = 16;
            for (int i = first; i <= last; i++) {
                Point p = points.get(i);
                double wantSpeed = p.speed != null ? p.speed : moveSpeed;
                double wantLookahead = p.lookahead != null ? p.lookahead : followDistance;
                if (Math.abs(wantSpeed - speed) > 1e-9) {
                    sb.append("                .moveSpeed(").append(num(wantSpeed)).append(")\n");
                    speed = wantSpeed;
                }
                if (Math.abs(wantLookahead - lookahead) > 1e-9) {
                    sb.append("                .followDistance(").append(num(wantLookahead)).append(")\n");
                    lookahead = wantLookahead;
                }
                sb.append("                .to(").append(num(p.x)).append(", ").append(num(p.y)).append(")\n");
            }
            if (backwards) sb.append("                .reversed()\n");
            if (fixedHeading != null) sb.append("                .fixedHeading(Math.toRadians(")
                    .append(num(Math.toDegrees(fixedHeading))).append("))\n");
            if (last == points.size() - 1 && endHeading != null) sb.append("                .endHeading(Math.toRadians(")
                    .append(num(Math.toDegrees(endHeading))).append("))\n");
            sb.append("                .build())\n");
            if (points.get(last).turnAround) sb.append("        .turn(Math.PI)\n");
            if (points.get(last).wait > 0) sb.append("        .waitSeconds(").append(num(points.get(last).wait)).append(")\n");
            first = last + 1;
            leg++;
        }
        sb.append("        .build();\n");
        return sb.toString();
    }

    private boolean isBackwards(Point point) {
        return point.backwards != null ? point.backwards : reversed;
    }

    private boolean hasMultipleLegs() {
        for (int i = 1; i < points.size() - 1; i++) {
            if (points.get(i).wait > 0 || points.get(i).turnAround
                    || isBackwards(points.get(i)) != isBackwards(points.get(i + 1))) return true;
        }
        return false;
    }

    /** Up to two decimals, no trailing zeros: 90, 0.65, 12.5 (89.99999 from degree math prints 90). */
    private static String num(double value) {
        double rounded = Math.round(value * 100) / 100.0;
        if (rounded == Math.rint(rounded)) {
            return String.valueOf((long) rounded);
        }
        return String.format(Locale.US, "%.2f", rounded).replaceAll("0+$", "");
    }
}
