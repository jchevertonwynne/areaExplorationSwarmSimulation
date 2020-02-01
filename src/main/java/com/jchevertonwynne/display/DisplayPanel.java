package com.jchevertonwynne.display;

import com.jchevertonwynne.simulation.Simulator;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;

public class DisplayPanel extends JPanel {
    public DisplayPanel(BufferedImage image, Simulator simulator) {
        add(new JLabel(new ImageIcon(image)));
        addMouseListener(new DisplayClickListener(image, simulator));
    }
}
