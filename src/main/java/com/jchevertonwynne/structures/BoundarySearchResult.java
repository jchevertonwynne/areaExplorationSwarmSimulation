package com.jchevertonwynne.structures;

import lombok.Value;

import java.util.List;

@Value
public class BoundarySearchResult {
    private List<Move> legalMoves;
    private List<Move> blacklistedMoves;
}