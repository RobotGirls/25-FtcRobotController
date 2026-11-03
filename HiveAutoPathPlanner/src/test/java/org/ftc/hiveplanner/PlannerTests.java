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
            Arrays.asList(new Point(20,120)), Arrays.asList(new Point(124,72)));
        check(ag.forAlliance(Alliance.RED).get(0).x==20,"RED goal selection");
        check(ag.forAlliance(Alliance.BLUE).get(0).x==124,"BLUE goal selection");
        boolean nullRefused=false;
        try { ag.forAlliance(null); } catch(IllegalArgumentException ex) { nullRefused=true; }
        check(nullRefused,"Alliance is required");
        System.out.println("PASS: obstacle, wall, segment, route, alliance goal selection, verification gate");
        System.out.println("Example simulated route: "+path);
    }
}
