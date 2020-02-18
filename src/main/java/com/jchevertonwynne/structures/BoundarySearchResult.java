package com.jchevertonwynne.structures;

import com.jchevertonwynne.structures.Move;
import lombok.Value;

import java.util.List;

@Value
public class BoundarySearchResult {
    private List<Move> legalMoves;
    private List<Move> blacklistedMoves;

    public boolean movesAvailable() {
        return legalMoves.size() != 0 || blacklistedMoves.size() != 0;
    }
}
