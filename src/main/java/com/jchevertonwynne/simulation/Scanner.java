package com.jchevertonwynne.simulation;

import com.jchevertonwynne.structures.Coord;
import com.jchevertonwynne.structures.Drop;
import com.jchevertonwynne.structures.TileStatus;
import com.jchevertonwynne.utils.CircleOperations;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jchevertonwynne.utils.CircleOperations.generateCircleRays;
import static com.jchevertonwynne.utils.Common.BROADCAST_RADIUS;
import static com.jchevertonwynne.utils.Common.GLOBAL_KNOWLEDGE;
import static com.jchevertonwynne.utils.Common.SIGHT_RADIUS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class Scanner {
    private final Boolean[][] world;
    private final SwarmAgent agent;
    private final Set<SwarmAgent> otherAgents;
    private final List<Drop> drops;

    public Scanner(Boolean[][] world, SwarmAgent agent, Set<SwarmAgent> otherAgents, List<Drop> drops) {
        this.world = world;
        this.agent = agent;
        this.otherAgents = otherAgents;
        this.drops = drops;
    }

    public Set<SwarmAgent> getOtherLocalAgents() {
        if (GLOBAL_KNOWLEDGE) {
            return new HashSet<>(otherAgents);
        }
        else {
            return otherAgents.stream()
                    .filter(otherAgent -> otherAgent.distanceFrom(agent) < BROADCAST_RADIUS)
                    .filter(this::inSight)
                    .collect(toSet());
        }
    }

    public void putDrop() {
        Drop drop = new Drop(agent.getPosition(), agent);
        drops.add(drop);
    }

    public List<Drop> getLocalDrops() {
        return drops.stream()
                .filter(drop -> agent.distanceFrom(drop.getCoord()) <= SIGHT_RADIUS)
                .filter(drop -> !drop.getAgent().equals(agent))
                .filter(this::inSight)
                .collect(toList());
    }

    private boolean inSight(Drop drop) {
        return inSight(drop.getCoord());
    }

    private boolean inSight(SwarmAgent agent) {
        return inSight(agent.getPosition());
    }

    private boolean inSight(Coord coord) {
        Map<Coord, Boolean> world = agent.getWorld();
        List<Coord> pathToCoord = CircleOperations.calculateRay(agent.getPosition(), coord);
        return pathToCoord.stream().allMatch(c -> world.getOrDefault(c, false));
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
