package com.jchevertonwynne.structures;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.util.Arrays.asList;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Coord {
    private final @NonNull int x;
    private final @NonNull int y;

    public static final List<Coord> CARDINAL_DIRECTIONS = asList(
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
        double xSquared = pow(abs(x - other.getX()), 2);
        double ySquared = pow(abs(y - other.getY()), 2);
        return pow(xSquared + ySquared, 0.5);
    }
}
