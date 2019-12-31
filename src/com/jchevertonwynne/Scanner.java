package com.jchevertonwynne;

import com.jchevertonwynne.structures.Coord;

import java.util.Objects;
import java.util.Set;

import static com.jchevertonwynne.structures.CircleOperations.angleBetween;
import static com.jchevertonwynne.structures.CircleOperations.calcCircle;
import static com.jchevertonwynne.structures.CircleOperations.mostSimilarAngle;
import static com.jchevertonwynne.structures.Common.SIGHT_RADIUS;

public class Scanner {
    private Boolean[][] world;

    public Scanner(Boolean[][] world) {
        this.world = world;
    }

    public void scan(SwarmAgent agent) {
        Set<Coord> edges = calcCircle(agent.getPosition(), SIGHT_RADIUS);
        edges.forEach(edge -> {
            boolean edgeSeen = false;

            Coord pos = agent.getPosition();
            int cx = pos.getX();
            int cy = pos.getY();

            int ex = edge.getX();
            int ey = edge.getY();

            double targetAngle = angleBetween(pos, edge);

            int dx = Integer.compare(ex - cx, 0);
            int dy = Integer.compare(ey - cy, 0);

            Coord nextCoord = pos;
            while (!Objects.equals(nextCoord, new Coord(ex, ey))) {
                cx = nextCoord.getX();
                cy = nextCoord.getY();
                boolean pathable = world[cx][cy];
                if (!pathable) {
                    edgeSeen = true;
                }
                else if (edgeSeen) {
                    break;
                }
                agent.getWorld().put(new Coord(cx, cy), pathable);
                Coord a = dx != 0 ? new Coord(cx + dx, cy) : null;
                Coord b = dy != 0 ? new Coord(cx, cy + dy) : null;
                nextCoord = mostSimilarAngle(a, b, edge, targetAngle);
            }
        });
    }
}
