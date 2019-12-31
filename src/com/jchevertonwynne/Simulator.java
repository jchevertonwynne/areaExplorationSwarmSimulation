package com.jchevertonwynne;

import com.jchevertonwynne.structures.Coord;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class Simulator {
    private Boolean[][] world;
    private Scanner scanner;
    private Set<SwarmAgent> agents;
    private Random random;
    private Map<Color, Integer> scans;

    public Simulator(int agentCount) {
        random = new Random();
        world = loadWorld();
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

    private SwarmAgent randomAgent() {
        Color agentColor = new Color(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
        );
        return new SwarmAgent(new Coord(780, 780), agentColor, scanner);
    }

    private Boolean[][] loadWorld() {
        final int pathColor = new Color(255, 255, 255).getRGB();
        File imageFile = new File("background2.png");

        try {
            BufferedImage image = ImageIO.read(imageFile);
            Boolean[][] result = new Boolean[image.getWidth()][image.getHeight()];

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    result[x][y] = image.getRGB(x, y) == pathColor;
                }
            }

            return result;
        }
        catch (IOException e) {
            System.out.println("lmao");
            return null;
        }
    }

    public void displayWorld(Graphics graphics) {
        agents.forEach(swarmAgent -> {
            swarmAgent.getWorld().forEach((coord, pathable) -> {
                graphics.setColor(pathable ? new Color(0, 255, 0) : new Color(255, 0, 0));
                graphics.drawLine(coord.getX(), coord.getY(), coord.getX(), coord.getY());
                graphics.dispose();
            });
        });
        agents.forEach(swarmAgent -> {
            Coord position = swarmAgent.getPosition();
            graphics.setColor(swarmAgent.getColor());
            graphics.fillOval(position.getX() - 4, position.getY() - 4, 8, 8);
            graphics.dispose();
        });
    }

    public boolean progress() {
        agents = agents.stream().map(SwarmAgent::nextMove).collect(Collectors.toSet());

        for (SwarmAgent agent : agents) {
            if (scans.get(agent.getColor()) != agent.getScansDone()) {
                return true;
            }
        }

        return false;
    }
}
