package org.ftc.hiveplanner;
import java.util.*;
public class PlannerTests {
    static void check(boolean ok,String description){if(!ok)throw new AssertionError(description);}
    public static void main(String[] args){
        FieldConfig cfg=FieldConfig.simulationExample();FieldMap map=new FieldMap(cfg);
        AStarPlanner planner=new AStarPlanner(map,cfg.gridIn);
        check(!map.isSafe(new Point(72,72)),"HIVE blocked");
        check(!map.isSafe(new Point(5,5)),"Wall blocked");
        check(map.isSafe(new Point(20,20)),"Open corner safe");
        check(!map.safeSegment(new Point(20,72),new Point(124,72)),"Central crossing blocked");
        List<Point> path=planner.plan(new Point(20,72),new Point(124,72));
        check(path.size()>2,"Reroute around HIVE");
        for(int i=1;i<path.size();i++)check(map.safeSegment(path.get(i-1),path.get(i)),"Each edge safe");
        List<Point> best=new GoalSelector(planner).bestPath(new Point(20,72),
            Arrays.asList(new Point(124,72),new Point(20,120)));
        check(!best.isEmpty(),"Select a reachable shooting goal");
        boolean refused=false;try{cfg.requireVerifiedForRobot();}catch(IllegalStateException ex){refused=true;}
        check(refused,"Unverified robot config must fail closed");
        AllianceGoals ag = new AllianceGoals(
            Arrays.asList(new Point(20,120)), Arrays.asList(new Point(22,118)),
            Arrays.asList(new Point(124,72)), Arrays.asList(new Point(122,70)));
        check(ag.forAlliance(Alliance.RED).get(0).x==20,"RED goal selection");
        check(ag.forTarget(Alliance.RED,FieldSide.FAR).get(0).x==22,"FAR-side selection");
        check(ag.forAlliance(Alliance.BLUE).get(0).x==124,"BLUE goal selection");
        boolean nullRefused=false;
        try { ag.forAlliance(null); } catch(IllegalArgumentException ex) { nullRefused=true; }
        check(nullRefused,"Alliance is required");

        FieldConfig verified = new FieldConfig(10,3,2,72,72,32,3,true);
        FakeRobot robot = new FakeRobot();
        DriveAssist assist = new DriveAssist(robot,
            new GoalSelector(new AStarPlanner(new FieldMap(verified), verified.gridIn)),
            verified, ag, Alliance.RED);
        assist.setSide(FieldSide.FAR);
        assist.update(true,false);
        check(assist.active(),"Held assist starts a path");
        check(robot.followed.get(robot.followed.size()-1).x==22,"Assist uses selected FAR target");
        assist.update(false,false);
        check(!assist.active()&&robot.cancelCount==1,"Button release immediately cancels");

        System.out.println("PASS: obstacle, route, color/side selection, hold-to-run, verification gate");
        System.out.println("Example simulated route: "+path);
    }

    private static final class FakeRobot implements DriveAssist.Robot {
        List<Point> followed=Collections.emptyList(); int cancelCount;
        public Point positionInches(){return new Point(20,20);}
        public boolean localizationValid(){return true;}
        public boolean manualOverrideRequested(){return false;}
        public void followPath(List<Point> waypoints){followed=waypoints;}
        public void updateFollower(){}
        public boolean followerFinished(){return false;}
        public void cancelFollowerAndStop(){cancelCount++;}
    }
}
