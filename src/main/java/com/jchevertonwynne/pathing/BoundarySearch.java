package com.jchevertonwynne.pathing;

import com.jchevertonwynne.structures.BoundarySearchResult;
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

import static com.jchevertonwynne.structures.Coord.CARDINAL_DIRECTIONS;
import static com.jchevertonwynne.utils.Common.DFS_MAX_TURNS_WITHOUT_FIND;
import static com.jchevertonwynne.utils.Common.DFS_RETURN_SOFT_CAP;
import static com.jchevertonwynne.utils.Common.SIGHT_RADIUS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class BoundarySearch {
    @Value
    private static class MoveHistory {
        @NonNull Coord lastTile;
        @NonNull Coord currentTile;
    }

    private final Coord position;
    private final Map<Coord, Boolean> world;
    private final Set<Coord> blacklist;
    private final Set<Coord> resultTiles = new HashSet<>();
    private final Set<Coord> seen = new HashSet<>();
    private int distance = 0;
    private int turnsWithoutFind = 0;
    private final List<Move> legalResults = new LinkedList<>();
    private final List<Move> blacklistedResults = new LinkedList<>();

    private BoundarySearch(Coord position, Map<Coord, Boolean> world, Set<Coord> blacklist) {
        this.position = position;
        this.world = world;
        this.blacklist = blacklist;
    }

    public static BoundarySearchResult calculateBoundaryTiles(Coord position, Map<Coord, Boolean> world, Set<Coord> blacklist) {
        return new BoundarySearch(position, world, blacklist).findAvailable();
    }

    /**
     * Calculate all edge tiles on current agent's world knowledge
     * @return List of first  ~ <200 boundary tiles
     */
    private BoundarySearchResult findAvailable() {
        BoundarySearchResult result = new BoundarySearchResult(legalResults, blacklistedResults);
        List<MoveHistory> toCheck = new LinkedList<>();
        toCheck.add(new MoveHistory(position, position));
        seen.add(position);

        while (!toCheck.isEmpty() && legalResults.size() < DFS_RETURN_SOFT_CAP) {
            if (!legalResults.isEmpty() && turnsWithoutFind > DFS_MAX_TURNS_WITHOUT_FIND) {
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
        List<MoveHistory> nextToCheck = new ArrayList<>();

        nextAvailable.forEach(nextPair -> {
            Coord previousTile = nextPair.getLastTile();
            Coord possibleTile = nextPair.getCurrentTile();
            if (world.containsKey(possibleTile)) {
                nextToCheck.add(new MoveHistory(previousTile, possibleTile));
            }
            else if (!resultTiles.contains(previousTile)) {
                Move move = new Move(previousTile, distance);
                boolean closeToBlacklist = blacklist.stream().anyMatch(tile -> tile.distance(previousTile) <= SIGHT_RADIUS);
                if (closeToBlacklist) {
                    blacklistedResults.add(move);
                }
                else {
                    legalResults.add(move);
                }
                resultTiles.add(previousTile);
                turnsWithoutFind = 0;
            }
        });

        return nextToCheck;
    }

    /**
     * @param position Position to check surrounding tiles
     * @return Unconsidered neighbour tiles
     */
    private List<Coord> getUnvisited(Coord position) {
        return CARDINAL_DIRECTIONS.stream()
                .map(position::combine)
                .filter(coord -> !seen.contains(coord))
                .peek(seen::add)
                .collect(toList());
    }
}
