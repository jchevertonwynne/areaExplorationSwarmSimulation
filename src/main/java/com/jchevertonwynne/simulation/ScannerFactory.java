package com.jchevertonwynne.simulation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ScannerFactory {
    private final Boolean[][] world;
    private final List<SwarmAgent> agents;

    public ScannerFactory(Boolean[][] world, List<SwarmAgent> agents) {
        this.world = world;
        this.agents = agents;
    }

    public Scanner instance(SwarmAgent agent) {
        List<SwarmAgent> otherAgents = agents.stream().filter(a -> !a.equals(agent)).collect(toList());
        return new Scanner(world, agent, otherAgents);
    }
}
