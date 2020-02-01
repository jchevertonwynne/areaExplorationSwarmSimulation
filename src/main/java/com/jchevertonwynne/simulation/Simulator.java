package com.jchevertonwynne.simulation;

import com.jchevertonwynne.display.Displayable;
import com.jchevertonwynne.structures.Coord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.jchevertonwynne.utils.Common.START_POSITION;

public class Simulator implements Displayable {
    Logger logger = LoggerFactory.getLogger(Simulator.class);

    private static class AgentHandlerThread extends Thread {
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
    private Set<SwarmAgent> agents = new HashSet<>();
    private Map<Color, Integer> scans = new HashMap<>();

    public Simulator(int agentCount, Boolean[][] world) {
        scannerFactory = new ScannerFactory(world, agents);

        for (int i = 0; i < agentCount; i++) {
            boolean added;
            do {
                added = agents.add(randomAgent());
            } while (!added);
        }

        agents.forEach(agent -> {
            agent.initialiseScanner();
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
        return new SwarmAgent(START_POSITION, agentColor, scannerFactory);
    }

    public Map<Coord, Boolean> combinedDiscovery() {
        Map<Coord, Boolean> result = new HashMap<>();
        agents.forEach(agent -> result.putAll(agent.getWorld()));
        return result;
    }

    /**
     * Progress all agents and check for any new scans
     * @return New scan status
     */
    public boolean progress() {
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
    public void display(Graphics g) {
        agents.forEach(swarmAgent -> {
            Coord position = swarmAgent.getPosition();
            g.setColor(swarmAgent.getColor());
            g.fillOval(position.getX() - 4, position.getY() - 4, 8, 8);
        });
        g.dispose();
    }
}
