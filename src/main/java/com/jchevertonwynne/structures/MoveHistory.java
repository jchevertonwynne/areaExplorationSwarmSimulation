package com.jchevertonwynne.structures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class MoveHistory {
    private final Coord lastTile;
    private final @NonNull Coord currentTile;
}