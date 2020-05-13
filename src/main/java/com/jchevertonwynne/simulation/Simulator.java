package com.jchevertonwynne.simulation;

import com.jchevertonwynne.display.Displayable;
import com.jchevertonwynne.pathing.PathMediator;
import com.jchevertonwynne.structures.Coord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.jchevertonwynne.utils.Common.DISTANCE_DISPLAY;
import static com.jchevertonwynne.utils.Common.START_POSITION;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class Simulator implements Displayable {
    Logger logger = LoggerFactory.getLogger(Simulator.class);

    private final int ALL_KNOWN_PATH_COLOUR = new Color(0, 255, 0).getRGB();
    private final int ALL_KNOWN_WALL_COLOUR = new Color(255, 0, 0).getRGB();
    private final int SOME_KNOWN_PATH_COLOUR = new Color(102, 153, 0).getRGB();
    private final int SOME_KNOWN_WALL_COLOUR = new Color(200, 200, 0).getRGB();
    private final int KNOWN_PATH_COLOUR = new Color(102, 102, 51).getRGB();
    private final int KNOWN_WALL_COLOUR = new Color(255, 12, 127).getRGB();

    private int imagesTaken;

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

    public void saveImage(BufferedImage image) {
        String filename = complete()
                ? "imageOutput/finished.png"
                : format("imageOutput/output%04d.png", imagesTaken);
        File output = new File(filename);

        try {
            ImageIO.write(image, "png", output);
            logger.info("Image {} saved", output.getName());
            if (!complete()) {
                imagesTaken++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final ScannerFactory scannerFactory;
    private final Set<SwarmAgent> agents = new HashSet<>();
    private final Map<SwarmAgent, Integer> scans = new HashMap<>();

    public Simulator(int agentCount, Boolean[][] world) {
        scannerFactory = new ScannerFactory(world, agents);

        for (int i = 0; i < agentCount; i++) {
            boolean added;
            do {
                added = agents.add(randomAgent());
            } while (!added);
        }

        agents.forEach(agent -> {
            agent.initialiseScanner(scannerFactory);
            scans.put(agent, 0);
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

    private Set<Coord> getSemiCommon() {
        Map<Coord, Integer> seenCount = new HashMap<>();
        Set<Coord> result = new HashSet<>();
        List<Map<Coord, Boolean>> allWorlds = agents.stream().map(SwarmAgent::getWorld).collect(toList());
        allWorlds.forEach(world -> world.forEach((k, v) -> {
            seenCount.putIfAbsent(k, 0);
            seenCount.computeIfPresent(k, (coord, count) -> count + 1);
        }));
        seenCount.forEach((coord, count) -> {
            if (count > 1) result.add(coord);
        });
        return result;
    }

    private Set<Coord> getCommon(Map<Coord, Boolean> combined) {
        List<Map<Coord, Boolean>> allWorlds = agents.stream()
                .map(SwarmAgent::getWorld)
                .collect(toList());
        Set<Coord> result = new HashSet<>();
        combined.forEach((coord, pathable) -> {
            if (allWorlds.stream().allMatch(world -> world.containsKey(coord))) {
                result.add(coord);
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
        boolean repathed;
        do {
            repathed = false;
            for (SwarmAgent agent : agents) {
                repathed |= agent.shareWithNeighbours(pathMediator);
            }
            ExecutorService threadManager = Executors.newCachedThreadPool();
            agents.forEach(a -> threadManager.execute(new AgentHandlerThread(a)));
            threadManager.shutdown();
            try {
                threadManager.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (repathed && !complete());

        boolean newlyScanned = false;
        for (SwarmAgent agent : agents) {
            if (scans.get(agent) != agent.getScansDone()) {
                scans.put(agent, agent.getScansDone());
                newlyScanned = true;
            }
        }

        agents.forEach(SwarmAgent::applyNextMove);
        return newlyScanned || complete();
    }

    public boolean complete() {
        return agents.stream().allMatch(SwarmAgent::isFinished);
    }

    @Override
    public void display(BufferedImage image) {
        Graphics graphics = image.getGraphics();
        Map<Coord, Integer> distances = new HashMap<>();
        if (DISTANCE_DISPLAY) {
            for (SwarmAgent agent : agents) {
                Map<Coord, Integer> agentDistances = agent.getDistances();
                agentDistances.forEach((c, d) -> {
                    int distance = distances.getOrDefault(c, Integer.MAX_VALUE);
                    distances.put(c, min(distance, d));
                });
            }
        }

        Map<Coord, Boolean> coordPathableMap = combinedDiscovery();
        Set<Coord> knownByMultiple = getSemiCommon();
        Set<Coord> knownByAll = getCommon(coordPathableMap);
        coordPathableMap.forEach((coord, pathable) -> {
            int knownAreaColour;
            if (DISTANCE_DISPLAY) {
                if (knownByAll.contains(coord)) {
                    knownAreaColour = pathable ? new Color(0, 255, distances.get(coord) % 256).getRGB() : ALL_KNOWN_WALL_COLOUR;
                }
                else if (knownByMultiple.contains(coord)) {
                    knownAreaColour = pathable ? new Color(distances.get(coord) % 256, 0, 255).getRGB() : SOME_KNOWN_WALL_COLOUR;
                }
                else {
                    knownAreaColour = pathable ? new Color(255, distances.get(coord) % 256, 0).getRGB() : KNOWN_WALL_COLOUR;
                }
            }
            else {
                if (knownByAll.contains(coord)) {
                    knownAreaColour = pathable ? ALL_KNOWN_PATH_COLOUR : ALL_KNOWN_WALL_COLOUR;
                }
                else if (knownByMultiple.contains(coord)) {
                    knownAreaColour = pathable ? SOME_KNOWN_PATH_COLOUR : SOME_KNOWN_WALL_COLOUR;
                }
                else {
                    knownAreaColour = pathable ? KNOWN_PATH_COLOUR : KNOWN_WALL_COLOUR;
                }
            }

            image.setRGB(coord.getX(), coord.getY(), knownAreaColour);
        });
        agents.forEach(swarmAgent -> swarmAgent.display(image));
        graphics.dispose();
    }
}
