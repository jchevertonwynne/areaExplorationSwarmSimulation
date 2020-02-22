package com.jchevertonwynne.pathing;

import com.jchevertonwynne.structures.Coord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

import java.util.*;

import static java.util.Comparator.comparingDouble;

public class ApproxDist {
    @Value
    private static class ApproxAStarOption {
        private double distanceEstimate;
        private int actualDistance;
        private @NonNull Coord tile;
    }

    private static Comparator<ApproxAStarOption> approxAStarOptionComparator = comparingDouble(approxAStarOption -> approxAStarOption.getActualDistance() + approxAStarOption.getDistanceEstimate());

    @Getter
    @AllArgsConstructor
    private static class CoordPair {
        private Coord a;
        private Coord b;

        @Override
        public int hashCode() {
            return a.hashCode() ^ b.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CoordPair coordPair = (CoordPair) o;
            return Objects.equals(a, coordPair.a) && Objects.equals(b, coordPair.b) ||
                    Objects.equals(a, coordPair.b) && Objects.equals(b, coordPair.a);
        }
    }

    private static boolean onBase(Coord coord) {
        return coord.getX() % 10 == 0 && coord.getY() % 10 == 0;
    }

    private static List<Coord> neighbourBases(Coord coord) {
        int x = coord.getX();
        int y = coord.getY();
        return List.of(
                new Coord(x - 10, y),
                new Coord(x + 10, y),
                new Coord(x, y - 10),
                new Coord(x, y + 10)
        );
    }

    private static List<Coord> nearBase(Coord coord) {
        int x = coord.getX();
        int y = coord.getY();

        List<Coord> result = new ArrayList<>();

        if (x % 10 != 0 && y % 10 != 0) {
            int xDiff = x % 10;
            int yDiff = y % 10;
            result.add(new Coord(x - xDiff, y - yDiff));
            result.add(new Coord(x + (10 - xDiff), y - yDiff));
            result.add(new Coord(x - xDiff, y + (10 - yDiff)));
            result.add(new Coord(x + (10 - xDiff), y + (10 - yDiff)));
        }
        else if (x % 10 != 0) {
            int xDiff = x % 10;
            result.add(new Coord(x - xDiff, y));
            result.add(new Coord(x + (10 - xDiff), y));
        }
        else if (y % 10 != 0) {
            int yDiff = y % 10;
            result.add(new Coord(x, y - yDiff));
            result.add(new Coord(x, y + (10 - yDiff)));
        }

        return result;
    }

    public static int distanceEstimate(Coord start, Coord end, Map<Coord, Boolean> world) {
        Set<Coord> onConnection = new HashSet<>();
        Set<Coord> offConnection = new HashSet<>();

        if (onBase(start)) {
            onConnection.add(start);
        }
        else {
            onConnection.addAll(nearBase(start));
        }

        if (onBase(end)) {
            offConnection.add(end);
        }
        else {
            offConnection.addAll(nearBase(end));
        }

        PriorityQueue<ApproxAStarOption> queue = new PriorityQueue<>(approxAStarOptionComparator);

        onConnection.forEach(on -> {
            int dist = AStarPathing.calculatePath(start, on, world).size();
            ApproxAStarOption startOption = new ApproxAStarOption(on.distance(end), dist, on);
            queue.add(startOption);
        });

        while (!queue.isEmpty()) {
            ApproxAStarOption top = queue.poll();
            Coord tile = top.getTile();
            int currentDist = top.getActualDistance();
            if (offConnection.contains(tile)) {
                int dist = AStarPathing.calculatePath(tile, end, world).size();
                return currentDist + dist;
            }
            List<Coord> nearby = neighbourBases(tile);
            nearby.forEach(near -> {
                try {
                    int estDist = AStarPathing.calculatePath(tile, near, world).size();
                    ApproxAStarOption next = new ApproxAStarOption(
                            near.distance(end),
                            currentDist + estDist,
                            near
                    );
                    queue.add(next);
                }
                catch (IllegalArgumentException ignored) { }
            });
        }

        return 0;
    }
}
