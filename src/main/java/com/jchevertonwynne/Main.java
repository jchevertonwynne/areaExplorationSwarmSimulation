package com.jchevertonwynne;

import javax.swing.JFrame;

public class Main extends JFrame {
	private Display display;

	public Main() {
		display = new Display(this);
		setResizable(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().add(display);
		pack();
		setVisible(true);
	}

	public void activate() {
		display.processImage();
	}

    public static void main(String[] args) {
		Main m = new Main();
		m.activate();
    }
}
