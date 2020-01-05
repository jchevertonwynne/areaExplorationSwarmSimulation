package com.jchevertonwynne.structures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class Move {
    private final @NonNull Coord tile;
    private final @NonNull int distance;
}
