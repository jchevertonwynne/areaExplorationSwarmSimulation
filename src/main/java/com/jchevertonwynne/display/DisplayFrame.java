package com.jchevertonwynne.display;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class DisplayFrame extends JFrame {
    public DisplayFrame(JPanel display) {
        getContentPane().add(display);
        pack();
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
