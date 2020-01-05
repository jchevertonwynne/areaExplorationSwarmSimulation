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
                new LinkedList<>()
                ));

        while (!toTry.isEmpty()) {
            AStarOption nextOption = toTry.remove();
            List<AStarOption> nextOptions = evaluateChoices(nextOption, destination, world);
            for (AStarOption aStarOption : nextOptions) {
                Coord tile = aStarOption.getTile();
                if (tile.equals(destination)) {
                    return aStarOption.getHistory();
                }
            }
            nextOptions.forEach(option -> {
                Coord tile = option.getTile();
                if (!seen.contains(tile)) {
                    toTry.add(option);
                    seen.add(tile);
                }
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
        List<AStarOption> result = new LinkedList<>();
        int currDist = aStarOption.getActualDistance();
        Coord currTile = aStarOption.getTile();

        CARDINAL_DIRECTIONS.forEach(option -> {
            Coord nextCoord = currTile.add(option);
            if (world.getOrDefault(nextCoord, false)) {
                List<Coord> newHistory = new LinkedList<>(aStarOption.getHistory());
                newHistory.add(nextCoord);
                result.add(
                        new AStarOption(
                                currDist + nextCoord.distance(goal),
                                currDist + 1,
                                nextCoord,
                                newHistory
                        )
                );
            }
        });
        return result;
    }
}
