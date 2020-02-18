package com.jchevertonwynne.utils;

import com.jchevertonwynne.structures.CircleResult;
import com.jchevertonwynne.structures.Coord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Integer.compare;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.pow;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class CircleOperations {
    private static Hashtable<Integer, Set<Coord>> circleEdges = new Hashtable<>();
    private static Hashtable<Integer, CircleResult> circleRays = new Hashtable<>();

    private static Set<Coord> calculateArc(int size) {
        Set<Coord> result = new HashSet<>();
        int a = 0;
        int b = size;
        double limit = pow(size + 0.5, 2);

        while (b > 0) {
            while (pow(a, 2) + pow(b, 2) <= limit) {
                result.add(new Coord(a, b));
                result.add(new Coord(b, a));
                a++;
            }
            while (b != 0 && pow(a, 2) + pow(b, 2) > limit) {
                b--;
            }
        }
        return result;
    }

    private static Set<Coord> baseCircle(int size) {
        circleEdges.computeIfAbsent(
                size,
                radius -> calculateArc(size).stream()
                        .flatMap(edgeTile -> edgeTile.rotations().stream())
                        .collect(toSet())
        );
        return circleEdges.get(size);
    }

    public static Set<Coord> calcCircle(Coord centre, int size) {
        return baseCircle(size).stream()
                .map(centre::combine)
                .collect(toSet());
    }

    public static double angleBetween(Coord a, Coord b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        return atan2(dy, dx);
    }

    private static List<Coord> calculateRay(Coord end) {
        Coord start = new Coord(0, 0);
        List<Coord> result = new LinkedList<>();
        double targetAngle = angleBetween(start, end);
        int dx = compare(end.getX() - start.getX(), 0);
        int dy = compare(end.getY() - start.getY(), 0);

        Coord current = start;
        while (!current.equals(end)) {
            int cx = current.getX();
            int cy = current.getY();
            Coord a = dx != 0 ? new Coord(cx + dx, cy) : null;
            Coord b = dy != 0 ? new Coord(cx, cy + dy) : null;
            current = mostSimilarAngle(a, b, end, targetAngle);
            result.add(current);
        }
        return result;
    }

    public static CircleResult generateCircleRays(Coord centre, int size) {
        Coord start = new Coord(0, 0);
        circleRays.computeIfAbsent(
                size,
                radius -> {
                    Map<Coord, List<Coord>> circleResultMap = new HashMap<>();
                    Set<Coord> edges = baseCircle(radius);
                    edges.stream().map(CircleOperations::calculateRay).forEach(ray -> {
                        Coord first;
                        Coord second = start;
                        for (Coord coord : ray) {
                            first = second;
                            second = coord;
                            if (!circleResultMap.containsKey(first)) {
                                circleResultMap.put(first, new ArrayList<>());
                            }
                            circleResultMap.get(first).add(second);
                        }
                        circleResultMap.put(second, emptyList());
                    });
                    return new CircleResult(start, circleResultMap);
                }
        );
        CircleResult circleBase = circleRays.get(size);
        Map<Coord, List<Coord>> resultRays = new HashMap<>();
        circleBase.getRays().forEach((c, lc) -> {
            Coord modifiedCoord = c.combine(centre);
            List<Coord> modifiedList = lc.stream().map(r -> r.combine(centre)).collect(toList());
            resultRays.put(modifiedCoord, modifiedList);
        });
        return new CircleResult(
                circleBase.getStart().combine(centre),
                resultRays
        );
    }

    public static Coord mostSimilarAngle(Coord a, Coord b, Coord goal, double targetAngle) {
        if (a == null && b == null) {
            throw new IllegalArgumentException("Coords cannot both be null");
        }

        if (a == null) {
            return b;
        }
        else if (a.equals(goal)) {
            return a;
        }

        if (b == null) {
            return  a;
        }
        else if (b.equals(goal)) {
            return b;
        }

        double aToGoal = angleBetween(a, goal);
        double bToGoal = angleBetween(b, goal);

        double aAngleDifference = abs(aToGoal - targetAngle);
        double bAngleDifference = abs(bToGoal - targetAngle);

        if (aAngleDifference > PI) {
            aAngleDifference = 2 * PI - aAngleDifference;
        }

        if (bAngleDifference > PI) {
            bAngleDifference = 2 * PI - bAngleDifference;
        }

        return aAngleDifference < bAngleDifference ? a : b;
    }
}