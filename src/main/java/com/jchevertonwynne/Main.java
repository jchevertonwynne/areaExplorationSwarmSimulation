package com.jchevertonwynne;

import com.jchevertonwynne.display.DisplayFrame;
import com.jchevertonwynne.display.DisplayPanel;
import com.jchevertonwynne.simulation.Simulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.jchevertonwynne.utils.Common.AGENT_COUNT;
import static com.jchevertonwynne.utils.Common.BACKGROUND_NAME;
import static com.jchevertonwynne.utils.Common.DISPLAY;
import static com.jchevertonwynne.utils.Common.PATH_COLOUR;

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
				result[x][y] = image.getRGB(x, y) == PATH_COLOUR;
			}
		}

		return result;
	}

    public static void main(String[] args) throws IOException {
		File backgroundFile = new File(BACKGROUND_NAME);
		BufferedImage image = ImageIO.read(backgroundFile);
		BufferedImage originalImage = ImageIO.read(backgroundFile);
		Graphics graphics = image.getGraphics();
		Boolean[][] world = createWorld(image);
		Simulator simulator = new Simulator(AGENT_COUNT, world);

		DisplayPanel displayPanel;
		DisplayFrame displayFrame;

		if (DISPLAY) {
			displayPanel= new DisplayPanel(image, simulator);
		 	displayFrame= new DisplayFrame(displayPanel);
		}

		logger.info("Commencing exploration of {}", BACKGROUND_NAME);

		do {
			long start = System.currentTimeMillis();
			boolean change;
			do {
				change = simulator.progress();
			} while (!change);
			System.out.printf("next scan calculated in %d ms\n", System.currentTimeMillis() - start);
			if (DISPLAY) {
				start = System.currentTimeMillis();
				graphics.drawImage(originalImage, 0, 0, null);
				simulator.display(image);
				displayFrame.repaint();
			}
		} while (!simulator.complete());
		logger.info("Simulation finished!");
	}
}
