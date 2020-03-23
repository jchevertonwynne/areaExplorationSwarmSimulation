package com.jchevertonwynne.simulation;

import com.jchevertonwynne.structures.Drop;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class ScannerFactory {
    private final Boolean[][] world;
    private final Set<SwarmAgent> agents;
    private final List<Drop> drops = new LinkedList<>();

    public ScannerFactory(Boolean[][] world, Set<SwarmAgent> agents) {
        this.world = world;
        this.agents = agents;
    }

    public Scanner instance(SwarmAgent agent) {
        Set<SwarmAgent> otherAgents = agents.stream().filter(a -> !a.equals(agent)).collect(toSet());
        return new Scanner(world, agent, otherAgents, drops);
    }
}
