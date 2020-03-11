package com.jchevertonwynne.pathing;

import com.jchevertonwynne.structures.Coord;
import lombok.NonNull;
import lombok.Value;

import java.util.*;

import static com.jchevertonwynne.structures.Coord.CARDINAL_DIRECTIONS;
import static java.lang.String.format;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;

public class AStarPathing {
    @Value
    private static class AStarPathOption {
        private double distanceEstimate;
        private int actualDistance;
        private @NonNull Coord tile;
        private @NonNull LinkedList<Coord> history;
    }

    private static Comparator<AStarPathOption> aStarOptionComparator = comparingDouble(aStarPathOption -> aStarPathOption.getActualDistance() + aStarPathOption.getDistanceEstimate());

    /**
     * A* to path from current position to destination
     * @param destination Goal coordinate
     * @return Path to destination, or null
     */
    public static LinkedList<Coord> calculatePath(Coord start, Coord destination, Map<Coord, Boolean> world) {
        Set<Coord> seen = new HashSet<>();
        seen.add(start);

        PriorityQueue<AStarPathOption> toTry = new PriorityQueue<>(aStarOptionComparator);
        toTry.add(new AStarPathOption(
                start.distance(destination),
                0,
                start,
                new LinkedList<>()));

        while (!toTry.isEmpty()) {
            AStarPathOption nextOption = toTry.poll();
            List<AStarPathOption> nextOptions = evaluateChoices(nextOption, destination, world);
            Optional<AStarPathOption> result = nextOptions.stream()
                    .filter(option -> option.getTile().equals(destination))
                    .findFirst();
            if (result.isPresent()) {
                return result.get().getHistory();
            }
            nextOptions.stream()
                    .filter(aStarPathOption -> !seen.contains(aStarPathOption.getTile()))
                    .forEach(aStarPathOption -> {
                        toTry.add(aStarPathOption);
                        seen.add(aStarPathOption.getTile());
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
     * @param aStarPathOption Current A* state
     * @param goal Destination
     * @return Unchecked neighbour A* states
     */
    private static List<AStarPathOption> evaluateChoices(AStarPathOption aStarPathOption, Coord goal, Map<Coord, Boolean> world) {
        int currDist = aStarPathOption.getActualDistance();
        Coord currTile = aStarPathOption.getTile();
        List<Coord> history = aStarPathOption.getHistory();

        return CARDINAL_DIRECTIONS.stream()
                .map(currTile::combine)
                .filter(coord -> world.getOrDefault(coord, false))
                .map(coord -> {
                    LinkedList<Coord> newHistory = new LinkedList<>(history);
                    newHistory.add(coord);
                    return new AStarPathOption(
                            coord.distance(goal),
                            currDist + 1,
                            coord,
                            newHistory
                    );
                }).collect(toList());
    }
}
