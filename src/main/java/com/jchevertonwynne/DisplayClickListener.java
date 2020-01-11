package com.jchevertonwynne;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class DisplayClickListener implements MouseListener {
    private Display display;
    public DisplayClickListener(Display display) {
        this.display = display;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        display.saveImage();
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
