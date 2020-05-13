package com.jchevertonwynne.structures;

import lombok.NonNull;
import lombok.Value;

@Value
public class TileStatus {
    @NonNull Coord coord;
    boolean pathable;
}
