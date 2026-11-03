package org.ftc.hiveplanner;
import java.util.*;

/** 8-connected A* in inches; verifies straight segments and rejects corner cuts. */
public final class AStarPlanner {
    private final FieldMap map;
    private final double step;
    private final int n;
    private static final int[][] DIRS={{1,0},{-1,0},{0,1},{0,-1},
        {1,1},{1,-1},{-1,1},{-1,-1}};
    public AStarPlanner(FieldMap map, double step) {
        this.map=map; this.step=step;
        this.n=(int)Math.floor(FieldDimensions.FIELD_SIDE_IN/step)+1;
    }
    private static final class Node {
        final int x,y; final double g,f;
        Node(int x,int y,double g,double f) {this.x=x;this.y=y;this.g=g;this.f=f;}
    }
    private Point at(int x,int y) {return new Point(x*step,y*step);}
    private boolean safe(int x,int y) {
        return x>=0&&y>=0&&x<n&&y<n&&map.isSafe(at(x,y));
    }
    private double h(int x,int y,int gx,int gy) {return Math.hypot(gx-x,gy-y);}
    public List<Point> plan(Point start, Point goal) {
        if (!map.isSafe(start)||!map.isSafe(goal)) return Collections.emptyList();
        // Direct visibility is common and eliminates grid-rounding problems.
        if (map.safeSegment(start,goal)) return Arrays.asList(start,goal);
        int sx=(int)Math.round(start.x/step),sy=(int)Math.round(start.y/step);
        int gx=(int)Math.round(goal.x/step),gy=(int)Math.round(goal.y/step);
        if (!safe(sx,sy)||!safe(gx,gy)) return Collections.emptyList();
        if (!map.safeSegment(start,at(sx,sy))||!map.safeSegment(goal,at(gx,gy)))
            return Collections.emptyList();
        double[][] costs=new double[n][n];
        for(double[] row:costs) Arrays.fill(row,Double.POSITIVE_INFINITY);
        int[][] px=new int[n][n],py=new int[n][n];
        for(int i=0;i<n;i++){Arrays.fill(px[i],-1);Arrays.fill(py[i],-1);}
        PriorityQueue<Node> open=new PriorityQueue<>(Comparator.comparingDouble(a->a.f));
        costs[sx][sy]=0;open.add(new Node(sx,sy,0,h(sx,sy,gx,gy)));
        while(!open.isEmpty()) {
            Node cur=open.poll();
            if (cur.g>costs[cur.x][cur.y]+1e-9)continue;
            if(cur.x==gx&&cur.y==gy) {
                LinkedList<Point> raw=new LinkedList<>();
                int x=gx,y=gy;
                while(x>=0&&y>=0){raw.addFirst(at(x,y));int xx=px[x][y],yy=py[x][y];x=xx;y=yy;}
                raw.addFirst(start);raw.addLast(goal);
                return simplify(raw);
            }
            for(int[] d:DIRS){
                int nx=cur.x+d[0],ny=cur.y+d[1];
                if(!safe(nx,ny)||!map.safeSegment(at(cur.x,cur.y),at(nx,ny)))continue;
                if(d[0]!=0&&d[1]!=0&&(!safe(cur.x+d[0],cur.y)||!safe(cur.x,cur.y+d[1])))continue;
                double g=cur.g+Math.hypot(d[0],d[1]);
                if(g+1e-9<costs[nx][ny]){
                    costs[nx][ny]=g;px[nx][ny]=cur.x;py[nx][ny]=cur.y;
                    open.add(new Node(nx,ny,g,g+h(nx,ny,gx,gy)));
                }
            }
        }
        return Collections.emptyList();
    }
    /** Greedy line-of-sight simplification, rechecking every shortcut. */
    private List<Point> simplify(List<Point> raw) {
        ArrayList<Point> out=new ArrayList<>();int i=0;out.add(raw.get(0));
        while(i<raw.size()-1) {
            int j=raw.size()-1;
            while(j>i+1&&!map.safeSegment(raw.get(i),raw.get(j)))j--;
            if(!map.safeSegment(raw.get(i),raw.get(j)))return Collections.emptyList();
            out.add(raw.get(j));i=j;
        }
        return out;
    }
}
