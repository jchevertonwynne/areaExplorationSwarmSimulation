package com.jchevertonwynne;

import com.jchevertonwynne.structures.Coord;

import java.util.List;
import java.util.Map;

import static com.jchevertonwynne.structures.CircleOperations.getCircleRays;
import static com.jchevertonwynne.structures.Common.SIGHT_RADIUS;

public class Scanner {
    private Boolean[][] world;

    public Scanner(Boolean[][] world) {
        this.world = world;
    }

    /**
     * Scan the area around an agent, cutting off at walls
     * @param agent Agent to scan area around
     */
    public void scan(SwarmAgent agent) {
        List<List<Coord>> rays = getCircleRays(agent.getPosition(), SIGHT_RADIUS);
        Map<Coord, Boolean> agentWorld = agent.getWorld();

        for (List<Coord> ray : rays) {
            boolean edgeSeen = false;
            for (Coord coord : ray) {
                int cx = coord.getX();
                int cy = coord.getY();
                boolean pathable = this.world[cx][cy];

                if (!pathable) {
                    edgeSeen = true;
                }
                else if (edgeSeen) {
                    break;
                }
                if (!agentWorld.containsKey(coord)) {
                    agent.noteNewlyDone(coord);
                    agentWorld.put(coord, pathable);
                }
            }
        }
    }
}
