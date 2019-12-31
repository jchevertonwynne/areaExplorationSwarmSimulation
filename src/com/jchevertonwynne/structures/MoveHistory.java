package com.jchevertonwynne.structures;

public class MoveHistory {
    private Coord lastTile;
    private Coord currentTile;

    public MoveHistory(Coord last, Coord current) {
        lastTile = last;
        currentTile = current;
    }

    public Coord getLastTile() {
        return lastTile;
    }

    public Coord getCurrentTile() {
        return currentTile;
    }
}