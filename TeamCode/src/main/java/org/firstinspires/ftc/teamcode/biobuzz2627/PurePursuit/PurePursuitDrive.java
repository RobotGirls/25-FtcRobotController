package org.firstinspires.ftc.teamcode.biobuzz2627.PurePursuit;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.SleepAction;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.CompMechanumDrive;
import org.firstinspires.ftc.teamcode.Drawing;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.TankDrive;
import org.firstinspires.ftc.teamcode.purepursuit.CurvePoint;
import org.firstinspires.ftc.teamcode.purepursuit.DrivePowers;
import org.firstinspires.ftc.teamcode.purepursuit.FieldViewerLink;
import org.firstinspires.ftc.teamcode.purepursuit.PurePursuitFollower;
import org.firstinspires.ftc.teamcode.purepursuit.PurePursuitPath;
import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.ViewerFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Road Runner for localization, pure pursuit for following.
 * Wraps one of the existing drive classes (TankDrive, MecanumDrive, CompMechanumDrive) and
 * uses only its Road Runner localizer and setDrivePowers(); the tuned drive classes themselves
 * are untouched. Paths run as Road Runner Actions so they still mix with SequentialAction,
 * ParallelAction and mechanism Actions like before.
 */
public final class PurePursuitDrive {
    /** The parts of a Road Runner drive class pure pursuit needs. */
    public interface Hardware {
        /** Updates the localizer and returns the robot relative velocity. */
        PoseVelocity2d updatePoseEstimate();

        Pose2d getPose();

        void setPose(Pose2d pose);

        void setDrivePowers(PoseVelocity2d powers);

        List<DcMotor> driveMotors();

        default boolean localizationReady() {
            return true;
        }

        default String localizationStatus() {
            return "ready";
        }
    }

    public final Hardware hardware;
    /** True for mecanum (can strafe), false for tank. */
    public final boolean holonomic;

    private final LinkedList<Pose2d> poseHistory = new LinkedList<>();
    private String routeName = "";
    private String fault = "";

    public PurePursuitDrive(Hardware hardware, boolean holonomic) {
        this.hardware = hardware;
        this.holonomic = holonomic;
    }

    /** Builds the BioBuzz four-motor mecanum drive used by every Pure Pursuit OpMode. */
    public static PurePursuitDrive create(HardwareMap hardwareMap, Pose2d startPose) {
        return compMecanum(new CompMechanumDrive(hardwareMap, startPose));
    }

    public static PurePursuitDrive tank(final TankDrive drive) {
        return new PurePursuitDrive(new Hardware() {
            @Override
            public PoseVelocity2d updatePoseEstimate() {
                return drive.updatePoseEstimate();
            }

            @Override
            public Pose2d getPose() {
                return drive.localizer.getPose();
            }

            @Override
            public void setPose(Pose2d pose) {
                drive.localizer.setPose(pose);
            }

            @Override
            public void setDrivePowers(PoseVelocity2d powers) {
                drive.setDrivePowers(powers);
            }

            @Override
            public List<DcMotor> driveMotors() {
                List<DcMotor> motors = new ArrayList<>(drive.leftMotors);
                motors.addAll(drive.rightMotors);
                return motors;
            }
        }, false);
    }

    public static PurePursuitDrive mecanum(final MecanumDrive drive) {
        return new PurePursuitDrive(new Hardware() {
            @Override
            public PoseVelocity2d updatePoseEstimate() {
                return drive.updatePoseEstimate();
            }

            @Override
            public Pose2d getPose() {
                return drive.localizer.getPose();
            }

            @Override
            public void setPose(Pose2d pose) {
                drive.localizer.setPose(pose);
            }

            @Override
            public void setDrivePowers(PoseVelocity2d powers) {
                drive.setDrivePowers(powers);
            }

            @Override
            public List<DcMotor> driveMotors() {
                return Arrays.<DcMotor>asList(
                        drive.frontLeft, drive.backLeft, drive.backRight, drive.frontRight);
            }
        }, true);
    }

    public static PurePursuitDrive compMecanum(final CompMechanumDrive drive) {
        return new PurePursuitDrive(new Hardware() {
            @Override
            public PoseVelocity2d updatePoseEstimate() {
                return drive.updatePoseEstimate();
            }

            @Override
            public Pose2d getPose() {
                return drive.localizer.getPose();
            }

            @Override
            public void setPose(Pose2d pose) {
                drive.localizer.setPose(pose);
            }

            @Override
            public void setDrivePowers(PoseVelocity2d powers) {
                drive.setDrivePowers(powers);
            }

            @Override
            public List<DcMotor> driveMotors() {
                return Arrays.asList(drive.frontLeft, drive.backLeft, drive.backRight, drive.frontRight);
            }

            @Override
            public boolean localizationReady() {
                return drive.localizer instanceof org.firstinspires.ftc.teamcode.PinpointLocalizer
                        && ((org.firstinspires.ftc.teamcode.PinpointLocalizer) drive.localizer).isReady();
            }

            @Override
            public String localizationStatus() {
                return drive.localizer instanceof org.firstinspires.ftc.teamcode.PinpointLocalizer
                        ? ((org.firstinspires.ftc.teamcode.PinpointLocalizer) drive.localizer).getStatus()
                        : drive.localizer.getClass().getSimpleName();
            }
        }, true);
    }

    public PoseVelocity2d updatePoseEstimate() {
        PoseVelocity2d velocity = hardware.updatePoseEstimate();
        poseHistory.add(hardware.getPose());
        while (poseHistory.size() > 100) {
            poseHistory.removeFirst();
        }
        return velocity;
    }

    public Pose2d getPose() {
        return hardware.getPose();
    }

    public void setPose(Pose2d pose) {
        hardware.setPose(pose);
        poseHistory.clear();
    }

    /** Robot relative powers (x forward, y left, counter-clockwise turn), scaled so no wheel exceeds 1. */
    public void setPowers(PoseVelocity2d powers) {
        hardware.setDrivePowers(DrivePowers.normalize(powers, holonomic));
    }

    public void stop() {
        hardware.setDrivePowers(new PoseVelocity2d(new Vector2d(0, 0), 0));
    }

    public boolean hasFault() {
        return !fault.isEmpty();
    }

    public String getFault() {
        return fault;
    }

    public boolean isLocalizationReady() {
        return hardware.localizationReady();
    }

    public String getLocalizationStatus() {
        return hardware.localizationStatus();
    }

    private void fault(String message) {
        fault = message;
        stop();
    }

    /** Coasting lets the wheels roll freely so the robot can be pushed by hand; otherwise brake. */
    public void setCoasting(boolean coasting) {
        DcMotor.ZeroPowerBehavior behavior = coasting
                ? DcMotor.ZeroPowerBehavior.FLOAT : DcMotor.ZeroPowerBehavior.BRAKE;
        for (DcMotor motor : hardware.driveMotors()) {
            motor.setZeroPowerBehavior(behavior);
        }
    }

    /** Drive a pure pursuit path. Ends when the robot reaches the end (or the path's timeout). */
    public Action follow(PurePursuitPath path) {
        return new FollowAction(new PurePursuitFollower(path, PurePursuitConfig.FOLLOWER, holonomic),
                path.timeoutSeconds, path.name);
    }

    /** Turn in place to a field heading (radians). */
    public Action turnTo(double heading) {
        return new FollowAction(PurePursuitFollower.turnTo(heading, PurePursuitConfig.FOLLOWER, holonomic),
                0, String.format("turn to %.0f deg", Math.toDegrees(heading)));
    }

    public Action run(Route route) {
        return run(route, Collections.<String, Action>emptyMap());
    }

    /**
     * Builds the Action for one route step. This is useful for autonomous state machines that
     * want each path, turn, or mechanism marker to be its own named state.
     */
    public Action runStep(Route.Step step, Map<String, Action> markers) {
        if (step instanceof Route.Follow) {
            return follow(((Route.Follow) step).path);
        }
        if (step instanceof Route.Turn) {
            return turnTo(((Route.Turn) step).heading);
        }
        if (step instanceof Route.Marker) {
            Route.Marker marker = (Route.Marker) step;
            Action action = markers.get(marker.name);
            return new MarkerAction(marker.name,
                    action != null ? action : new SleepAction(marker.seconds));
        }
        throw new IllegalArgumentException("Unknown route step: " + step.getClass().getName());
    }

    /**
     * Every step of a route as one Action. Marker steps run the Action with the same name from
     * markers, or just wait for the marker's estimated time if there isn't one.
     */
    public Action run(final Route route, Map<String, Action> markers) {
        List<Action> actions = new ArrayList<>();
        actions.add(packet -> {
            routeName = route.name;
            return false;
        });
        for (Route.Step step : route.steps) {
            actions.add(runStep(step, markers));
        }
        return new SequentialAction(actions);
    }

    /** Runs a pure pursuit follower every loop until it's done. */
    public final class FollowAction implements Action {
        private final PurePursuitFollower follower;
        private final double timeoutSeconds;
        private final String name;
        private long startNanos = -1;
        private long lastProgressNanos = -1;
        private double lastProgress;

        FollowAction(PurePursuitFollower follower, double timeoutSeconds, String name) {
            this.follower = follower;
            this.timeoutSeconds = timeoutSeconds;
            this.name = name;
        }

        @Override
        public boolean run(@NonNull TelemetryPacket p) {
            if (startNanos < 0) {
                startNanos = System.nanoTime();
                lastProgressNanos = startNanos;
            }

            if (!hardware.localizationReady()) {
                fault(name + " stopped: Pinpoint is " + hardware.localizationStatus());
                p.put("FAULT", fault);
                report(p, follower, null, name + ": LOCALIZATION FAULT - STOPPED");
                return false;
            }

            PoseVelocity2d velocity = updatePoseEstimate();
            PurePursuitFollower.Output out = follower.update(getPose(), velocity);

            long now = System.nanoTime();
            double elapsed = (now - startNanos) / 1e9;
            boolean timedOut = timeoutSeconds > 0 && elapsed > timeoutSeconds;
            if (timedOut) {
                fault(String.format("%s timed out after %.1f seconds", name, timeoutSeconds));
                p.put("FAULT", fault);
                report(p, follower, out, name + ": TIMED OUT - STOPPED");
                return false;
            }
            if (out.isDone()) {
                stop();
                report(p, follower, out, name + ": done");
                return false;
            }

            if (out.progress >= lastProgress + PurePursuitConfig.MIN_PROGRESS_INCHES) {
                lastProgress = out.progress;
                lastProgressNanos = now;
            }

            double translationPower = out.powers.linearVel.norm();
            double noProgressSeconds = (now - lastProgressNanos) / 1e9;
            if (translationPower >= PurePursuitConfig.WATCHDOG_MIN_TRANSLATION_POWER
                    && noProgressSeconds > PurePursuitConfig.NO_PROGRESS_TIMEOUT_SECONDS) {
                fault(String.format("%s stopped: no Pinpoint path progress for %.2f seconds",
                        name, noProgressSeconds));
                p.put("FAULT", fault);
                report(p, follower, out, name + ": NO PROGRESS - STOPPED");
                return false;
            }

            setPowers(out.powers);
            p.put("no progress (s)", noProgressSeconds);
            report(p, follower, out, name + " (" + out.phase.name().toLowerCase() + ")");
            return true;
        }

        @Override
        public void preview(@NonNull Canvas c) {
            PurePursuitPath path = follower.getPath();
            if (path == null || path.points.size() < 2) {
                return;
            }
            double[] xs = new double[path.points.size()];
            double[] ys = new double[path.points.size()];
            for (int i = 0; i < xs.length; i++) {
                CurvePoint point = path.points.get(i);
                xs[i] = point.x;
                ys[i] = point.y;
            }
            c.setStroke("#4CAF507A");
            c.setStrokeWidth(1);
            c.strokePolyline(xs, ys);
        }
    }

    /** Runs a mechanism Action at a marker while still tracking (and showing) the robot. */
    private final class MarkerAction implements Action {
        private final String name;
        private final Action action;

        MarkerAction(String name, Action action) {
            this.name = name;
            this.action = action;
        }

        @Override
        public boolean run(@NonNull TelemetryPacket p) {
            updatePoseEstimate();
            boolean running = action.run(p);
            report(p, null, null, name + (running ? "" : ": done"));
            return running;
        }
    }

    /**
     * Shows the robot on FTC Dashboard and the FieldViewer when no path is running, e.g. during
     * init or TeleOp.
     */
    public void reportIdle(String status) {
        TelemetryPacket packet = new TelemetryPacket();
        report(packet, null, null, status);
        FtcDashboard.getInstance().sendTelemetryPacket(packet);
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    private void report(TelemetryPacket p, PurePursuitFollower follower,
                        PurePursuitFollower.Output out, String status) {
        Pose2d pose = getPose();
        p.put("route", routeName);
        p.put("status", status);
        p.put("x", pose.position.x);
        p.put("y", pose.position.y);
        p.put("heading (deg)", Math.toDegrees(pose.heading.toDouble()));
        p.put("localization", hardware.localizationStatus());
        if (hasFault()) {
            p.put("FAULT", fault);
        }
        if (out != null) {
            p.put("distance to end", out.distanceToEnd);
            p.put("progress", out.progress + " / " + out.length);
            p.put("heading error (deg)", Math.toDegrees(out.headingError));
        }

        Canvas c = p.fieldOverlay();
        if (follower != null && follower.getPathPoints().size() >= 2) {
            List<Vector2d> points = follower.getPathPoints();
            double[] xs = new double[points.size()];
            double[] ys = new double[points.size()];
            for (int i = 0; i < xs.length; i++) {
                xs[i] = points.get(i).x;
                ys[i] = points.get(i).y;
            }
            c.setStroke("#4CAF50");
            c.setStrokeWidth(1);
            c.strokePolyline(xs, ys);
        }
        if (out != null && out.followRadius > 0) {
            c.setStroke("#FF980080");
            c.strokeCircle(pose.position.x, pose.position.y, out.followRadius);
            c.setFill("#FF9800");
            c.fillCircle(out.followPoint.x, out.followPoint.y, 1.5);
            c.setFill("#9C27B0");
            c.fillCircle(out.pointToPoint.x, out.pointToPoint.y, 1.5);
        }
        drawPoseHistory(c);
        c.setStroke("#3F51B5");
        Drawing.drawRobot(c, pose);

        if (PurePursuitConfig.STREAM_TO_FIELD_VIEWER) {
            ViewerFrame frame = new ViewerFrame();
            frame.time = System.nanoTime() / 1e9;
            frame.routeName = routeName;
            frame.status = status;
            frame.pose = pose;
            frame.withFollower(follower, out);
            FieldViewerLink.get().publish(frame);
        }
    }

    private void drawPoseHistory(Canvas c) {
        double[] xs = new double[poseHistory.size()];
        double[] ys = new double[poseHistory.size()];
        int i = 0;
        for (Pose2d pose : poseHistory) {
            xs[i] = pose.position.x;
            ys[i] = pose.position.y;
            i++;
        }
        c.setStrokeWidth(1);
        c.setStroke("#3F51B5");
        c.strokePolyline(xs, ys);
    }
}
