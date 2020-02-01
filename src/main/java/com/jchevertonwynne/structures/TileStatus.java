package com.jchevertonwynne.structures;

import lombok.NonNull;
import lombok.Value;

@Value
public class TileStatus {
    private @NonNull Coord coord;
    private boolean status;
}
