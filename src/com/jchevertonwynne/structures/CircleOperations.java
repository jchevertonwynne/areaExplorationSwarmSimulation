package com.jchevertonwynne.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.pow;

public class CircleOperations {
    private static Map<Integer, Set<Coord>> circleEdges = new HashMap<>();
    private static Map<Integer, List<List<Coord>>> circleRays = new HashMap<>();

    public static Set<Coord> calculateArc(int size) {
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

    public static Set<Coord> baseCircle(int size) {
        if (!circleEdges.containsKey(size)) {
            Set<Coord> quarter = calculateArc(size);
            Set<Coord> edges = new HashSet<>();
            quarter.forEach(coord -> {
                int x = coord.getX();
                int y= coord.getY();
                edges.add(new Coord(x, y));
                edges.add(new Coord(-x, y));
                edges.add(new Coord(x, -y));
                edges.add(new Coord(-x, -y));
            });
            circleEdges.put(size, edges);
        }

        return circleEdges.get(size);
    }

    public static  Set<Coord> calcCircle(Coord centre, int size) {
        int cx = centre.getX();
        int cy = centre.getY();

        return baseCircle(size).stream()
                .map(edge -> new Coord(cx + edge.getX(), cy + edge.getY()))
                .collect(Collectors.toSet());
    }

    public static double angleBetween(Coord a, Coord b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        return atan2(dy, dx);
    }

    private static List<Coord> calculateRay(Coord end) {
        Coord start = new Coord(0, 0);
        List<Coord> result = new ArrayList<>();
        double targetAngle = angleBetween(start, end);
        int dx = Integer.compare(end.getX() - start.getX(), 0);
        int dy = Integer.compare(end.getY() - start.getY(), 0);

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

    public static List<List<Coord>> getCircleRays(Coord centre, int size) {
        if (!circleRays.containsKey(size)) {
            List<List<Coord>> rays = new ArrayList<>();
            baseCircle(size).forEach(edge -> rays.add(calculateRay(edge)));
            circleRays.put(size, rays);
        }

        List<List<Coord>> rays = circleRays.get(size);
        int cx = centre.getX();
        int cy = centre.getY();

        return rays.stream()
                .map(rayLine -> rayLine.stream()
                        .map(tile -> new Coord(cx + tile.getX(), cy + tile.getY()))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public static Coord mostSimilarAngle(Coord a, Coord b, Coord goal, double targetAngle) {
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
            return goal;
        }

        double aa = angleBetween(a, goal);
        double ba = angleBetween(b, goal);

        double ad = abs(aa - targetAngle);
        double bd = abs(ba - targetAngle);

        if (ad > PI) {
            ad = 2 * PI - ad;
        }

        if (bd > PI) {
            bd = 2 * PI - bd;
        }

        return ad < bd ? a : b;
    }
}

