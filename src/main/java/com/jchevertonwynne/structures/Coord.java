package com.jchevertonwynne.structures;

import lombok.Value;

import java.util.List;

import static java.lang.Math.pow;
import static java.util.Arrays.asList;

@Value
public class Coord {
    private int x;
    private int y;

    public static List<Coord> CARDINAL_DIRECTIONS = asList(
            new Coord(0, 1),
            new Coord(1, 0),
            new Coord(0, -1),
            new Coord(-1, 0)
    );

    public Coord add(Coord other) {
        return new Coord(
                x + other.getX(),
                y + other.getY()
        );
    }

    public List<Coord> rotations() {
        return asList(
                this,
                new Coord(-x, -y),
                new Coord(x, -y),
                new Coord(-x, y)
        );
    }

    public double distance(Coord other) {
        double xSquared = pow(x - other.getX(), 2);
        double ySquared = pow(y - other.getY(), 2);
        return pow(xSquared + ySquared, 0.5);
    }
}
