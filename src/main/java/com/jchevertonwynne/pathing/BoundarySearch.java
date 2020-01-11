package com.jchevertonwynne.pathing;

import com.jchevertonwynne.structures.Coord;
import com.jchevertonwynne.structures.Move;
import lombok.NonNull;
import lombok.Value;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jchevertonwynne.Common.DFS_MAX_TURNS_WITHOUT_FIND;
import static com.jchevertonwynne.Common.DFS_SOFT_LIST_RETURN_LIMIT;
import static com.jchevertonwynne.structures.Coord.CARDINAL_DIRECTIONS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class BoundarySearch {
    @Value
    private static class MoveHistory {
        private Coord lastTile;
        private @NonNull Coord currentTile;
    }

    /**
     * Calculate all edge tiles on current agent's world knowledge
     * @return List of first <= ~400 boundary tiles
     */
    public static List<Move> findAvailable(Coord position, Map<Coord, Boolean> world) {
        List<MoveHistory> toCheck = new LinkedList<>();
        toCheck.add(new MoveHistory(null, position));

        Set<Coord> seen = new HashSet<>();
        seen.add(position);

        int distance = 0;
        List<Move> result = new LinkedList<>();
        Set<Coord> resultTiles = new HashSet<>();

        int turnsWithoutFind = 0;

        while (!toCheck.isEmpty() && result.size() < DFS_SOFT_LIST_RETURN_LIMIT) {
            if (!result.isEmpty() && turnsWithoutFind > DFS_MAX_TURNS_WITHOUT_FIND) {
                return result;
            }
            distance++;
            turnsWithoutFind++;

            List<MoveHistory> nextAvailable = toCheck.stream()
                    .map(MoveHistory::getCurrentTile)
                    .filter(coord -> world.getOrDefault(coord, false))
                    .flatMap(coord -> getUnvisited(coord, seen).stream()
                            .map(newPos -> new MoveHistory(coord, newPos))
                    ).collect(toList());

            List<MoveHistory> nextToCheck = new LinkedList<>();
            for (MoveHistory nextPair : nextAvailable) {
                Coord previousTile = nextPair.getLastTile();
                Coord possibleTile = nextPair.getCurrentTile();
                if (!world.containsKey(possibleTile)) {
                    if (!resultTiles.contains(previousTile)) {
                        result.add(new Move(previousTile, distance));
                        resultTiles.add(previousTile);
                        turnsWithoutFind = 0;
                    }
                } else {
                    nextToCheck.add(new MoveHistory(previousTile, possibleTile));
                }
            }

            seen.addAll(toCheck.stream()
                    .map(MoveHistory::getCurrentTile)
                    .collect(toSet()));
            toCheck = nextToCheck;
        }
        return result;
    }

    /**
     * @param position Position to check surrounding tiles
     * @param checked Coords that have already been considered
     * @return Unconsidered neighbour tiles
     */
    private static List<Coord> getUnvisited(Coord position, Set<Coord> checked) {
        return CARDINAL_DIRECTIONS.stream()
                .map(position::add)
                .filter(coord -> !checked.contains(coord))
                .peek(checked::add)
                .collect(toList());
    }
}
