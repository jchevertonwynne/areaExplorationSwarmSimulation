package com.jchevertonwynne.structures;

import java.util.Comparator;
import java.util.List;

public class AStarOption {
    private double distanceEstimate;
    private int actualDistance;
    private Coord tile;
    private List<Coord> history;

    public static Comparator<AStarOption> AStarOptionComparator = Comparator.comparingDouble(AStarOption::getDistanceEstimate);

    public AStarOption(double distanceEstimate, int actualDistance, Coord tile, List<Coord> history) {
        this.distanceEstimate = distanceEstimate;
        this.actualDistance = actualDistance;
        this.tile = tile;
        this.history = history;
    }

    public double getDistanceEstimate() {
        return distanceEstimate;
    }

    public int getActualDistance() {
        return actualDistance;
    }

    public Coord getTile() {
        return tile;
    }

    public List<Coord> getHistory() {
        return history;
    }
}