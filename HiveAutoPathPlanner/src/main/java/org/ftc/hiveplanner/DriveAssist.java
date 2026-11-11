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
    private boolean active=false, prevButton=false;
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

    /** Call once per TeleOp loop; assist is hold-to-run and release always stops immediately. */
    public void update(boolean assistButton, boolean cancelButton) {
        boolean pressed=assistButton&&!prevButton;prevButton=assistButton;
        if(!assistButton||cancelButton||robot.manualOverrideRequested()||!robot.localizationValid()) {
            if(active){robot.cancelFollowerAndStop();active=false;}
            return;
        }
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
}
