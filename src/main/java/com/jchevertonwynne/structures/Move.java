package com.jchevertonwynne.structures;

import lombok.NonNull;
import lombok.Value;

@Value
public class Move {
    @NonNull Coord tile;
    int distance;
}
