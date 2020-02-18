package com.jchevertonwynne.simulation;

import com.jchevertonwynne.display.Displayable;
import com.jchevertonwynne.pathing.PathMediator;
import com.jchevertonwynne.structures.Coord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.jchevertonwynne.utils.Common.ALL_KNOWN_PATH_COLOUR;
import static com.jchevertonwynne.utils.Common.ALL_KNOWN_WALL_COLOUR;
import static com.jchevertonwynne.utils.Common.KNOWN_PATH_COLOUR;
import static com.jchevertonwynne.utils.Common.KNOWN_WALL_COLOUR;
import static com.jchevertonwynne.utils.Common.SOME_KNOWN_PATH_COLOUR;
import static com.jchevertonwynne.utils.Common.SOME_KNOWN_WALL_COLOUR;
import static com.jchevertonwynne.utils.Common.START_POSITION;
import static java.util.stream.Collectors.toList;

public class Simulator implements Displayable {
    Logger logger = LoggerFactory.getLogger(Simulator.class);

    private static class AgentHandlerThread implements Runnable {
        private final SwarmAgent agent;

        public AgentHandlerThread(SwarmAgent agent) {
            this.agent = agent;
        }

        @Override
        public void run() {
            agent.processTurn();
        }
    }

    private ScannerFactory scannerFactory;
    private List<SwarmAgent> agents = new ArrayList<>();
    private Map<Color, Integer> scans = new HashMap<>();

    public Simulator(int agentCount, Boolean[][] world) {
        scannerFactory = new ScannerFactory(world, agents);
        Set<SwarmAgent> uniqueAgents = new HashSet<>();
        for (int i = 0; i < agentCount; i++) {
            boolean added;
            do {
                added = uniqueAgents.add(randomAgent());
            } while (!added);
        }
        agents.addAll(uniqueAgents);
        agents.forEach(agent -> {
            agent.initialiseScanner(scannerFactory);
            scans.put(agent.getColor(), 0);
        });
    }

    /**
     * Create randomly coloured SwarmAgent
     * @return SwarmAgent
     */
    private SwarmAgent randomAgent() {
        Random random = new Random();
        Color agentColor = new Color(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
        );
        return new SwarmAgent(START_POSITION, agentColor);
    }

    private Map<Coord, Boolean> combinedDiscovery() {
        Map<Coord, Boolean> result = new HashMap<>();
        agents.forEach(agent -> result.putAll(agent.getWorld()));
        return result;
    }

    private Map<Coord, Boolean> getSemiCommon() {
        List<Map<Coord, Boolean>> allWorlds = agents.stream().map(SwarmAgent::getWorld).collect(toList());
        Set<Coord> seen = new HashSet<>();
        Map<Coord, Boolean> result = new HashMap<>();
        for (Map<Coord, Boolean> world : allWorlds) {
            world.forEach((coord, pathable) -> {
                if (seen.contains(coord)) {
                    result.put(coord, pathable);
                }
                else {
                    seen.add(coord);
                }
            });
        }
        return result;
    }

    private Map<Coord, Boolean> getCommon(Map<Coord, Boolean> combined) {
        List<Map<Coord, Boolean>> allWorlds = agents.stream().map(SwarmAgent::getWorld).collect(toList());
        Map<Coord, Boolean> result = new HashMap<>();
        combined.forEach((coord, pathable) -> {
            if (allWorlds.stream().allMatch(world -> world.containsKey(coord))) {
                result.put(coord, pathable);
            }
        });
        return result;
    }

    /**
     * Progress all agents and check for any new scans
     * @return New scan status
     */
    public boolean progress() {
        PathMediator pathMediator = new PathMediator();
        agents.forEach(agent -> agent.shareWithNeighbours(pathMediator));

        ExecutorService threadManager = Executors.newCachedThreadPool();
        agents.forEach(a -> threadManager.execute(new AgentHandlerThread(a)));

        threadManager.shutdown();
        try {
            threadManager.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean newlyScanned = false;
        for (SwarmAgent agent : agents) {
            if (scans.get(agent.getColor()) != agent.getScansDone()) {
                scans.put(agent.getColor(), agent.getScansDone());
                newlyScanned = true;
            }
        }

        agents.forEach(SwarmAgent::applyNextMove);
        return newlyScanned;
    }

    public boolean complete() {
        return agents.stream().allMatch(SwarmAgent::getFinished);
    }

    @Override
    public void display(BufferedImage image) {
        Graphics graphics = image.getGraphics();
        Map<Coord, Boolean> coordPathableMap = combinedDiscovery();
        Map<Coord, Boolean> knownByMultiple = getSemiCommon();
        Map<Coord, Boolean> knownByAll = getCommon(coordPathableMap);
        coordPathableMap.forEach((coord, pathable) -> {
            int knownAreaColour;
            if (knownByAll.containsKey(coord)) {
                knownAreaColour = pathable ? ALL_KNOWN_PATH_COLOUR : ALL_KNOWN_WALL_COLOUR;
            }
            else if (knownByMultiple.containsKey(coord)) {
                knownAreaColour = pathable ? SOME_KNOWN_PATH_COLOUR : SOME_KNOWN_WALL_COLOUR;
            }
            else {
                knownAreaColour = pathable ? KNOWN_PATH_COLOUR : KNOWN_WALL_COLOUR;
            }
            image.setRGB(coord.getX(), coord.getY(), knownAreaColour);
        });
        agents.forEach(swarmAgent -> swarmAgent.display(image));
        graphics.dispose();
    }


}
