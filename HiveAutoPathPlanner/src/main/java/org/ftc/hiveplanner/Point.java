package org.ftc.hiveplanner;
public final class Point {
    public final double x, y;
    public Point(double x, double y) { this.x=x; this.y=y; }
    public double distance(Point b) { return Math.hypot(x-b.x,y-b.y); }
    @Override public String toString() { return String.format(java.util.Locale.US,"(%.2f, %.2f)",x,y); }
}
