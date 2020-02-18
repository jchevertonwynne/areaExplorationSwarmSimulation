package com.jchevertonwynne.simulation;

import com.jchevertonwynne.structures.CircleResult;
import com.jchevertonwynne.structures.Coord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jchevertonwynne.utils.CircleOperations.generateCircleRays;
import static com.jchevertonwynne.utils.Common.BROADCAST_RADIUS;
import static com.jchevertonwynne.utils.Common.SIGHT_RADIUS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class Scanner {
    private final Boolean[][] world;
    private final SwarmAgent agent;
    private final List<SwarmAgent> otherAgents;

    public Scanner(Boolean[][] world, SwarmAgent agent, List<SwarmAgent> otherAgents) {
        this.world = world;
        this.agent = agent;
        this.otherAgents = otherAgents;
    }

    public List<SwarmAgent> getOtherLocalAgents() {
        return otherAgents.stream()
                .filter(otherAgent -> otherAgent.distanceFrom(agent) < BROADCAST_RADIUS)
                .collect(toList());
    }

    /**
     * Scan the area around an agent, cutting off at walls
     */
    public void scan() {
        CircleResult circleResult = generateCircleRays(agent.getPosition(), SIGHT_RADIUS);
        Map<Coord, Boolean> agentWorld = agent.getWorld();

        Coord start = circleResult.getStart();
        Map<Coord, List<Coord>> rays = circleResult.getRays();

        List<Coord> coordsToProcess = singletonList(start);

        while (coordsToProcess.size() != 0) {
            List<Coord> nextToProcess = new ArrayList<>();
            coordsToProcess.forEach(coord -> {
                boolean pathable;
                try {
                    pathable = world[coord.getX()][coord.getY()];

                    if (!agentWorld.containsKey(coord)) {
                        agent.setWorldStatus(coord, pathable);
                    }

                    if (pathable) {
                        nextToProcess.addAll(rays.getOrDefault(coord, emptyList()));
                    }
                }
                catch (IndexOutOfBoundsException ignored) {
                }
            });
            coordsToProcess = nextToProcess;
        }
    }
}
