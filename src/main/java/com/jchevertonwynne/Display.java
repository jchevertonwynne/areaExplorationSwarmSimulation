package com.jchevertonwynne;

import com.jchevertonwynne.simulation.Simulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.jchevertonwynne.Common.BACKGROUND_NAME;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.String.format;

public class Display extends JPanel {
    Logger logger = LoggerFactory.getLogger(Display.class);

    private JFrame frame;
    private BufferedImage image;
    private Simulator simulator;
    private int imagesTaken;


    public Display(JFrame frame) {
        this.frame = frame;
        imagesTaken = 0;
        try {
            BufferedImage im = ImageIO.read(new File(BACKGROUND_NAME));
            image = new BufferedImage(im.getWidth(), im.getHeight(), TYPE_INT_RGB);
            image.getGraphics().drawImage(im, 0, 0, null);
        } catch (IOException e) {
            logger.error("Background image {} not found\n", BACKGROUND_NAME);
        }
        simulator = new Simulator(1, image);

        add(new JLabel(new ImageIcon(image)));
        addMouseListener(new DisplayClickListener(this));
    }

    /**
     * Run simulation until a scan has been done, then display the update
     */
    public void processImage() {
        logger.info("Commencing exploration of {}", BACKGROUND_NAME);
        while (!simulator.complete()) {
            boolean change = false;
            while (!change) {
                change = simulator.progress();
                if (simulator.complete()) {
                    break;
                }
            }
            simulator.displayWorld(image);
            frame.repaint();
        }
        simulator.displayWorld(image);
        frame.repaint();
        saveImage();
        logger.info("Simulation finished!");
    }

    public void saveImage() {
        File output = simulator.complete()
                ? new File("imageOutput/finished.png")
                : new File(format("imageOutput/output%04d.png", imagesTaken));
        try {
            ImageIO.write(image, "png", output);
            logger.info("Image {} saved", output.getName());
            if (!simulator.complete()) {
                imagesTaken++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}