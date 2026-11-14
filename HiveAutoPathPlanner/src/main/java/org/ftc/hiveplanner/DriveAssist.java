package org.ftc.hiveplanner;
import java.util.*;
/** Hardware-independent, non-blocking TeleOp assist orchestrator. */
public final class DriveAssist {
    public interface Robot {
        Point positionInches();
        boolean localizationValid();
        boolean manualOverrideRequested();
        void followPath(List<Point> waypoints);
        void updateFollower();
        boolean followerFinished();
        void cancelFollowerAndStop();
    }
    private final Robot robot;
    private final GoalSelector selector;
    private final FieldConfig config;
    private final AllianceGoals goals;
    private Alliance alliance;
    private FieldSide side;
    private boolean active=false;
    private boolean previousAssistHeld;
    private FieldSide previousRequest;
    public DriveAssist(Robot robot, GoalSelector selector, FieldConfig config,
                       AllianceGoals goals, Alliance alliance) {
        if (robot==null || selector==null || config==null || goals==null || alliance==null)
            throw new IllegalArgumentException("Explicit alliance and dependencies are required");
        this.robot=robot;this.selector=selector;this.config=config;this.goals=goals;
        this.alliance=alliance;this.side=FieldSide.AUDIENCE;
    }
    public Alliance alliance(){return alliance;}
    public boolean active(){return active;}
    public FieldSide side(){return side;}
    /** Change only while idle (e.g. FTC init); switching while driving is forbidden. */
    public void setAlliance(Alliance alliance) {
        if (active) throw new IllegalStateException("Cancel assist before changing alliance");
        if (alliance==null) throw new IllegalArgumentException("Choose RED or BLUE");
        this.alliance=alliance;
    }

    /** Change only while idle. FAR means the scoring-table/rear side of the field. */
    public void setSide(FieldSide side) {
        if (active) throw new IllegalStateException("Cancel assist before changing side");
        if (side==null) throw new IllegalArgumentException("Choose AUDIENCE or FAR");
        this.side=side;
    }

    /**
     * Call once per TeleOp loop. The A button is hold-to-run; release cancels immediately. The
     * bumpers select the requested side before (or during) the assist. A side change cancels the
     * old route before a fresh route is generated from the current localized position.
     */
    public void update(boolean assistHeld, FieldSide requestedSide) {
        if (requestedSide == null) throw new IllegalArgumentException("Choose AUDIENCE or FAR");
        boolean pressed = assistHeld
                && (!previousAssistHeld || requestedSide != previousRequest);
        previousAssistHeld = assistHeld;
        previousRequest = requestedSide;

        if (!assistHeld || robot.manualOverrideRequested()
                || !robot.localizationValid()) {
            cancelActive();
            return;
        }

        if (active && side != requestedSide) {
            cancelActive();
        }
        side = requestedSide;

        if(pressed&&!active) {
            config.requireVerifiedForRobot();
            List<Point> selected=goals.forTarget(alliance,side);
            if(selected.isEmpty()) return; // no verified positions for that alliance
            List<Point> path=selector.bestPath(robot.positionInches(),selected);
            if(path.size()>=2){robot.followPath(path);active=true;}
        }
        if(active){
            robot.updateFollower();
            if(robot.followerFinished()){robot.cancelFollowerAndStop();active=false;}
        }
    }

    /** Backwards-compatible two-button API for older callers. */
    public void update(boolean assistButton, boolean cancelButton) {
        update(assistButton && !cancelButton, side);
    }

    private void cancelActive() {
        if (active) {
            robot.cancelFollowerAndStop();
            active = false;
        }
    }
}
