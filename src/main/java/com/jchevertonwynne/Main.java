package com.jchevertonwynne;

import com.jchevertonwynne.display.DisplayFrame;
import com.jchevertonwynne.display.DisplayPanel;
import com.jchevertonwynne.simulation.Simulator;
import com.jchevertonwynne.structures.Coord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.jchevertonwynne.utils.Common.AGENT_COUNT;
import static com.jchevertonwynne.utils.Common.BACKGROUND_NAME;
import static com.jchevertonwynne.utils.Common.PATH_COLOR;

public class Main {
	private static Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * Load array of pathable terrain from png file
	 * @return Array of pathable terrain
	 */
	private static Boolean[][] createWorld(BufferedImage image) {
		Boolean[][] result = new Boolean[image.getWidth()][image.getHeight()];

		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				result[x][y] = image.getRGB(x, y) == PATH_COLOR;
			}
		}

		return result;
	}

    public static void main(String[] args) throws IOException {
		BufferedImage image = ImageIO.read(new File(BACKGROUND_NAME));
		Graphics graphics = image.getGraphics();
		Boolean[][] world = createWorld(image);
		Simulator simulator = new Simulator(AGENT_COUNT, world);

		DisplayPanel displayPanel = new DisplayPanel(image, simulator);
		DisplayFrame displayFrame = new DisplayFrame(displayPanel);

		logger.info("Commencing exploration of {}", BACKGROUND_NAME);

		int background = new Color(0, 255, 0).getRGB();
		int wall = new Color(255, 0, 0).getRGB();

		do {
			boolean change;
			do {
				change = simulator.progress();
			} while (!change && !simulator.complete());
			Map<Coord, Boolean> coordBooleanMap = simulator.combinedDiscovery();
			graphics.drawImage(image, 0, 0, null);
			coordBooleanMap.forEach((k, v) -> image.setRGB(k.getX(), k.getY(), v ? background : wall));
			simulator.display(graphics);
			displayFrame.repaint();
		} while (!simulator.complete());
		logger.info("Simulation finished!");
	}
}
