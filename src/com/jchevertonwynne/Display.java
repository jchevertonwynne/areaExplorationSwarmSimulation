package com.jchevertonwynne;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.jchevertonwynne.structures.Common.BACKGROUND_NAME;

public class Display extends JPanel {
    private BufferedImage image;
    private Simulator simulator;
    private int imagesTaken;

    public Display() {
        simulator = new Simulator(1);
        imagesTaken = 0;
        try {
            BufferedImage im = ImageIO.read(new File(BACKGROUND_NAME));
            image = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
            image.getGraphics().drawImage(im, 0, 0, null);
        }
        catch (IOException e) {
            System.out.printf("Background image %s not found\n", BACKGROUND_NAME);
        }

        add(new JLabel(new ImageIcon(image)));

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                try {
                    ImageIO.write(
                            image,
                            "png",
                            new File(String.format("imageOutput/output%04d.png", imagesTaken))
                    );
                    System.out.printf("output%04d.png saved\n", imagesTaken);
                    imagesTaken++;
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
        });
    }

    /**
     * Run simulation until a scan has been done, then display the update
     */
    public void processImage() {
        boolean change = false;
        while (!change) {
            change = simulator.progress();
        }
        simulator.displayWorld(image);
    }
}
