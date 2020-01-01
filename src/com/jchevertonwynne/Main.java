package com.jchevertonwynne;

import javax.swing.JFrame;
import java.awt.Container;

public class Main extends JFrame {
	private Display display;

	public Main() {
		display = new Display();
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.add(display);
		pack();
	}

	/**
	 * Infinitely run simulation and display when a new scan has been done
	 */
	public void activate() {
		while (true) {
			display.processImage();
			repaint();
		}
	}

    public static void main(String[] args) {
		Main m = new Main();
		m.setVisible(true);
		m.activate();
    }
}
