package com.jchevertonwynne.structures;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class CircleOperations {
    public static Set<Coord> calculateArc(int n) {
        Set<Coord> result = new HashSet<>();
        int a = 0;
        int b = n;
        double limit = pow(n + 0.5, 2);

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

    public static  Set<Coord> calcCircle(Coord centre, int n) {
        int cx = centre.getX();
        int cy = centre.getY();
        Set<Coord> quarter = calculateArc(n);
        Set<Coord> result = new HashSet<>();
        quarter.forEach(coord -> {
            int dx = coord.getX();
            int dy = coord.getY();
            result.add(new Coord(cx + dx, cy + dy));
            result.add(new Coord(cx + dx, cy - dy));
            result.add(new Coord(cx - dx, cy + dy));
            result.add(new Coord(cx - dx, cy - dy));
        });
        return result;
    }

    public static double angleBetween(Coord a, Coord b) {
        int ax = a.getX();
        int ay = a.getY();
        int bx = b.getX();
        int by = b.getY();

        if (bx - ax == 0) {
            return 0;
        }
        return (by - ay) - (bx - ax);
    }

    public static Coord mostSimilarAngle(Coord a, Coord b, Coord goal, double targetAngle) {
        if (a == null) {
            return b;
        }
        else if (b == null) {
            return  a;
        }
        double aDiff = abs(angleBetween(a, goal) - targetAngle);
        double bDiff = abs(angleBetween(b, goal) - targetAngle);
        return aDiff < bDiff ? a : b;
    }
}

