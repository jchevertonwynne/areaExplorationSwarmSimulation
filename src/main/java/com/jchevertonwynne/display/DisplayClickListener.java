package com.jchevertonwynne.display;

import com.jchevertonwynne.simulation.Simulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.lang.String.format;

public class DisplayClickListener implements MouseListener {
    Logger logger = LoggerFactory.getLogger(DisplayClickListener.class);

    private BufferedImage image;
    private Simulator simulator;
    private int imagesTaken;

    public DisplayClickListener(BufferedImage image, Simulator simulator) {
        this.image = image;
        this.simulator = simulator;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        String filename = simulator.complete()
                ? "imageOutput/finished.png"
                : format("imageOutput/output%04d.png", imagesTaken);
        File output = new File(filename);

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

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }
}
