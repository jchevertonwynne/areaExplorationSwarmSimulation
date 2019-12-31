package com.jchevertonwynne;

import javax.swing.JFrame;
import java.awt.Container;

public class Main extends JFrame {

	public Main() {
		Display d = new Display();
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.add(d);
		pack();
		d.processImage();
		d.processImage();
	}

    public static void main(String[] args) {
		Main m = new Main();
		m.setVisible(true);
    }
}
