package com.jchevertonwynne;

import com.jchevertonwynne.structures.AStarOption;
import com.jchevertonwynne.structures.Coord;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import static com.jchevertonwynne.structures.AStarOption.AStarOptionComparator;
import static com.jchevertonwynne.structures.Coord.CARDINAL_DIRECTIONS;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class AStarPathing {
    /**
     * A* to path from current position to destination
     * @param destination Goal coordinate
     * @return Path to destination, or null
     */
    public static List<Coord> calculatePath(Coord start, Coord destination, Map<Coord, Boolean> world) {
        Set<Coord> seen = new HashSet<>();
        seen.add(start);

        PriorityQueue<AStarOption> toTry = new PriorityQueue<>(AStarOptionComparator);
        toTry.add(new AStarOption(
                start.distance(destination),
                0,
                start,
                new LinkedList<>()));

        while (!toTry.isEmpty()) {
            AStarOption nextOption = toTry.poll();
            List<AStarOption> nextOptions = evaluateChoices(nextOption, destination, world);
            for (AStarOption aStarOption : nextOptions) {
                Coord tile = aStarOption.getTile();
                if (tile.equals(destination)) {
                    return aStarOption.getHistory();
                }
            }
            nextOptions.stream()
                    .filter(aStarOption -> !seen.contains(aStarOption.getTile()))
                    .forEach(aStarOption -> {
                        toTry.add(aStarOption);
                        seen.add(aStarOption.getTile());
                    });
        }
        throw new IllegalArgumentException(
                format(
                        "Path from %s to %s is not possible for this world",
                        start.toString(),
                        destination.toString()
                )
        );
    }

    /**
     * From a tile, find all not checked neighbour tiles
     * @param aStarOption Current A* state
     * @param goal Destination
     * @return Unchecked neighbour A* states
     */
    private static List<AStarOption> evaluateChoices(AStarOption aStarOption, Coord goal,  Map<Coord, Boolean> world) {
        int currDist = aStarOption.getActualDistance();
        Coord currTile = aStarOption.getTile();
        List<Coord> history = aStarOption.getHistory();

        return CARDINAL_DIRECTIONS.stream()
                .map(currTile::add)
                .filter(coord -> world.getOrDefault(coord, false))
                .map(coord -> {
                    List<Coord> newHistory = new LinkedList<>(history);
                    newHistory.add(coord);
                    return new AStarOption(
                            coord.distance(goal),
                            currDist + 1,
                            coord,
                            newHistory
                    );
                }).collect(toList());
    }
}
