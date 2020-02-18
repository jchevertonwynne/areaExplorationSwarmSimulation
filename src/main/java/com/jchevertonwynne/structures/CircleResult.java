package com.jchevertonwynne.structures;

import lombok.NonNull;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class CircleResult {
    private @NonNull Coord start;
    private @NonNull Map<Coord, List<Coord>> rays;
}
