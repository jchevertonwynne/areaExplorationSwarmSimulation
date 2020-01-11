package com.jchevertonwynne.structures;

import lombok.Value;

import java.util.List;

@Value
public class BoundarySearchResult {
    private boolean limitedLeft;
    private List<Move> moves;
}
