package com.jchevertonwynne.structures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparingDouble;

@Getter
@AllArgsConstructor
public class AStarOption {
    private final @NonNull double distanceEstimate;
    private final @NonNull int actualDistance;
    private final @NonNull Coord tile;
    private final @NonNull List<Coord> history;

    public static Comparator<AStarOption> AStarOptionComparator = comparingDouble(aStarOption -> aStarOption.actualDistance + aStarOption.distanceEstimate);
}