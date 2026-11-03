package org.ftc.hiveplanner;
import java.util.*;
/** Candidate goals must be established through alliance-specific shooting trials. */
public final class GoalSelector {
    private final AStarPlanner planner;
    public GoalSelector(AStarPlanner planner) {this.planner=planner;}
    public List<Point> bestPath(Point start, List<Point> verifiedGoals) {
        double best=Double.POSITIVE_INFINITY;List<Point> result=Collections.emptyList();
        for(Point goal:verifiedGoals){
            List<Point> candidate=planner.plan(start,goal);
            if(candidate.size()<2)continue;
            double length=0;
            for(int i=1;i<candidate.size();i++)length+=candidate.get(i-1).distance(candidate.get(i));
            if(length<best){best=length;result=candidate;}
        }
        return result;
    }
}
