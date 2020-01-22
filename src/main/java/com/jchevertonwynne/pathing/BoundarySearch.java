package com.jchevertonwynne.pathing;

import com.jchevertonwynne.structures.Coord;
import com.jchevertonwynne.structures.Move;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
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
        private @NonNull Coord lastTile;
        private @NonNull Coord currentTile;
    }

    private Coord position;
    private Map<Coord, Boolean> world;
    private Set<Coord> resultTiles = new HashSet<>();
    private Set<Coord> seen = new HashSet<>();
    private List<Move> result = new ArrayList<>();
    private int turnsWithoutFind = 0;
    private int distance = 0;


    private BoundarySearch(Coord position, Map<Coord, Boolean> world) {
        this.position = position;
        this.world = world;
    }

    public static List<Move> calculateBoundaryTiles(Coord position, Map<Coord, Boolean> world) {
        return new BoundarySearch(position, world).findAvailable();
    }

    /**
     * Calculate all edge tiles on current agent's world knowledge
     * @return List of first <= ~400 boundary tiles
     */
    private List<Move> findAvailable() {
        List<MoveHistory> toCheck = new LinkedList<>();
        toCheck.add(new MoveHistory(position, position));
        seen.add(position);

        while (!toCheck.isEmpty() && result.size() < DFS_SOFT_LIST_RETURN_LIMIT) {
            if (!result.isEmpty() && turnsWithoutFind > DFS_MAX_TURNS_WITHOUT_FIND) {
                return result;
            }
            distance++;
            turnsWithoutFind++;

            List<MoveHistory> nextAvailable = toCheck.stream()
                    .map(MoveHistory::getCurrentTile)
                    .filter(coord -> world.getOrDefault(coord, false))
                    .flatMap(coord -> getUnvisited(coord).stream()
                            .map(newPos -> new MoveHistory(coord, newPos))
                    ).collect(toList());

            List<MoveHistory> nextToCheck = nextToCheckFinder(nextAvailable);

            seen.addAll(toCheck.stream()
                    .map(MoveHistory::getCurrentTile)
                    .collect(toSet()));
            toCheck = nextToCheck;
        }
        return result;
    }

    private List<MoveHistory> nextToCheckFinder(List<MoveHistory> nextAvailable) {
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
        return nextToCheck;
    }

    /**
     * @param position Position to check surrounding tiles
     * @return Unconsidered neighbour tiles
     */
    private List<Coord> getUnvisited(Coord position) {
        List<Coord> adjacentTiles = CARDINAL_DIRECTIONS.stream()
                .map(position::add)
                .filter(coord -> !seen.contains(coord))
                .collect(toList());

        seen.addAll(adjacentTiles);
        return adjacentTiles;
    }
}
