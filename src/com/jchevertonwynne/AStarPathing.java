package com.jchevertonwynne;

import com.jchevertonwynne.structures.AStarOption;
import com.jchevertonwynne.structures.Coord;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import static com.jchevertonwynne.structures.AStarOption.AStarOptionComparator;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
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
        toTry.add(new AStarOption(distanceEstimate(start, destination), 0, start, new LinkedList<>()));

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
        throw new IllegalArgumentException(format("Path from %s to %s is not possible for this world", start.toString(), destination.toString()));
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
        int cx = currTile.getX();
        int cy = currTile.getY();
        List<Coord> options = Arrays.asList(
                new Coord(1, 0),
                new Coord(-1, 0),
                new Coord(0, 1),
                new Coord(0, -1)
        );
        options.forEach(option -> {
            int dx = option.getX();
            int dy = option.getY();
            Coord nextCoord = new Coord(cx + dx, cy + dy);
            if (world.getOrDefault(nextCoord, false)) {
                List<Coord> newHistory = new LinkedList<>(aStarOption.getHistory());
                newHistory.add(nextCoord);
                result.add(new AStarOption(distanceEstimate(nextCoord, goal), currDist + 1, nextCoord, newHistory));
            }
        });
        return result;
    }

    public static double distanceEstimate(Coord a, Coord b) {
        return pow(pow(abs(a.getX() - b.getX()), 2) + pow(abs(a.getY() - b.getY()), 2), 0.5) - 1;
    }
}
