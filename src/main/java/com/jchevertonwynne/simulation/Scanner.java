package com.jchevertonwynne.simulation;

import com.jchevertonwynne.structures.Coord;
import com.jchevertonwynne.structures.TileStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jchevertonwynne.utils.CircleOperations.generateCircleRays;
import static com.jchevertonwynne.utils.Common.BROADCAST_RADIUS;
import static com.jchevertonwynne.utils.Common.SIGHT_RADIUS;
import static java.util.stream.Collectors.toSet;

public class Scanner {
    private final Boolean[][] world;
    private final SwarmAgent agent;
    private final Set<SwarmAgent> otherAgents;

    public Scanner(Boolean[][] world, SwarmAgent agent, Set<SwarmAgent> otherAgents) {
        this.world = world;
        this.agent = agent;
        this.otherAgents = otherAgents;
    }

    public Set<SwarmAgent> getOtherLocalAgents() {
        return otherAgents.stream()
                .filter(otherAgent -> otherAgent.distanceFrom(agent) < BROADCAST_RADIUS)
                .collect(toSet());
    }

    /**
     * Scan the area around an agent, cutting off at walls
     */
    public void scan() {
        List<List<Coord>> rays = generateCircleRays(agent.getPosition(), SIGHT_RADIUS);
        Map<Coord, Boolean> agentWorld = agent.getWorld();

        for (List<Coord> ray : rays) {
            boolean edgeSeen = false;
            for (Coord coord : ray) {
                boolean pathable;

                try {
                    pathable = world[coord.getX()][coord.getY()];
                    if (!pathable) {
                        edgeSeen = true;
                    }
                    else if (edgeSeen) {
                        break;
                    }

                    if (!agentWorld.containsKey(coord)) {
                        agent.setWorldStatus(new TileStatus(coord, pathable));
                    }
                }
                catch (IndexOutOfBoundsException ignored) {
                }
            }
        }
    }
}
