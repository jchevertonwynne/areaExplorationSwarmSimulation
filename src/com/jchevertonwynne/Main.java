package com.jchevertonwynne;

import javax.swing.JFrame;
import java.awt.Container;

public class Main extends JFrame {
	private Display display;

	public Main() {
		display = new Display(this);
		setResizable(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.add(display);
		pack();
	}

	public void activate() {
		display.processImage();
	}

    public static void main(String[] args) {
		Main m = new Main();
		m.setVisible(true);
		m.activate();
    }
}
