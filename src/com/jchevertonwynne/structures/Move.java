package com.jchevertonwynne.structures;

public class Move {
    private Coord tile;
    private int distance;

    public Move(Coord tile, int distance) {
        this.tile = tile;
        this.distance = distance;
    }

    public Coord getTile() {
        return tile;
    }

    public int getDistance() {
        return distance;
    }
}
