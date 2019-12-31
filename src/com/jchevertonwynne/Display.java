package com.jchevertonwynne;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Display extends JPanel {
    private BufferedImage image;
    Simulator simulator;


    public Display() {
        simulator = new Simulator(1);
        try {
            File imageFile = new File("background2.png");
            image = ImageIO.read(imageFile);
        }
        catch (IOException e) {
            System.out.println("lmao");
        }
        add(new JLabel(new ImageIcon(image)));
    }

    public void processImage() {
        boolean change = false;
        while (!change) {
            change = simulator.progress();
        }
        simulator.displayWorld(image.getGraphics());
        repaint();
    }
}
