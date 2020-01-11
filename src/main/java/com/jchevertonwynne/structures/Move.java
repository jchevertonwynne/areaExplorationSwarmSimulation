package com.jchevertonwynne.structures;

import lombok.NonNull;
import lombok.Value;

@Value
public class Move {
    private @NonNull Coord tile;
    private int distance;
}
