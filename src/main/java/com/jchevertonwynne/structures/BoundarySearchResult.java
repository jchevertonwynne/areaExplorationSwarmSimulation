package com.jchevertonwynne.structures;

import lombok.Value;

import java.util.List;

@Value
public class BoundarySearchResult {
    List<Move> legalMoves;
    List<Move> blacklistedMoves;

    public boolean movesAvailable() {
        return legalMoves.size() != 0 || blacklistedMoves.size() != 0;
    }
}
