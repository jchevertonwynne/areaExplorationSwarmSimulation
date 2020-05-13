package com.jchevertonwynne.display;

import com.jchevertonwynne.simulation.Simulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class DisplayClickListener implements MouseListener {
    Logger logger = LoggerFactory.getLogger(DisplayClickListener.class);

    private final BufferedImage image;
    private final Simulator simulator;


    public DisplayClickListener(BufferedImage image, Simulator simulator) {
        this.image = image;
        this.simulator = simulator;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        simulator.saveImage(image);
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
