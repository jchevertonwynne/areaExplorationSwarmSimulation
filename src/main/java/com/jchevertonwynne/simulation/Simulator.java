package com.jchevertonwynne.simulation;

import com.jchevertonwynne.structures.Coord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Simulator {
    Logger logger = LoggerFactory.getLogger(Simulator.class);

    private Scanner scanner;
    private Set<SwarmAgent> agents;
    private Map<Color, Integer> scans;

    public Simulator(int agentCount, BufferedImage image) {
        Boolean[][] world = loadWorld(image);

        scanner = new Scanner(world);
        agents = new HashSet<>();
        scans = new HashMap<>();

        for (int i = 0; i < agentCount; i++) {
            boolean added;
            do {
                added = agents.add(randomAgent());
            } while (!added);
        }

        agents.forEach(agent -> scans.put(agent.getColor(), 0));
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
        return new SwarmAgent(new Coord(20, 20), agentColor, scanner);
    }

    /**
     * Load array of pathable terrain from png file
     * @return Array of pathable terrain
     */
    private Boolean[][] loadWorld(BufferedImage image) {
        int pathColor = new Color(255, 255, 255).getRGB();
        Boolean[][] result = new Boolean[image.getWidth()][image.getHeight()];

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                result[x][y] = image.getRGB(x, y) == pathColor;
            }
        }

        return result;
    }

    /**
     * Draw new discovered area, dots showing agent's paths and lines for history
     * @param image To draw the display
     */
    public void displayWorld(BufferedImage image) {
        int background = new Color(0, 255, 0).getRGB();
        int wall = new Color(255, 0, 0).getRGB();
        int path = new Color(0, 192, 255).getRGB();
        agents.forEach(swarmAgent -> {
            Map<Coord, Boolean> world = swarmAgent.getWorld();
            swarmAgent.getNewlyDone().forEach(coord -> {
                boolean pathable = world.get(coord);
                image.setRGB(coord.getX(), coord.getY(), pathable ? background : wall);
            });
        });
        Graphics graphics = image.getGraphics();
        agents.forEach(swarmAgent -> {
            Coord position = swarmAgent.getPosition();
            graphics.setColor(swarmAgent.getColor());
            graphics.fillOval(position.getX() - 4, position.getY() - 4, 8, 8);
            swarmAgent.getNewPathTaken().forEach(pathCoord -> image.setRGB(pathCoord.getX(), pathCoord.getY(), path));
        });
        graphics.dispose();
    }

    /**
     * Progress all agents and check for any new scans
     * @return New scan status
     */
    public boolean progress() {
        agents.forEach(SwarmAgent::calculateNextMove);
        boolean scansDone = false;
        for (SwarmAgent agent : agents) {
            if (scans.get(agent.getColor()) != agent.getScansDone()) {
                scansDone = true;
                scans.put(agent.getColor(), agent.getScansDone());
            }
        }
        agents.forEach(SwarmAgent::applyNextMove);
        return scansDone;
    }

    public boolean complete() {
        return agents.stream().allMatch(SwarmAgent::getFinished);
    }
}
