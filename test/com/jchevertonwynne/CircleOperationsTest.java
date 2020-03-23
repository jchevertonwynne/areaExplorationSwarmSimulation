package com.jchevertonwynne;

import com.jchevertonwynne.structures.Coord;
import com.jchevertonwynne.utils.CircleOperations;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.lang.Math.PI;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CircleOperationsTest {
    @Test
    public void shouldAssembleCircle() {
        Set<Coord> result = CircleOperations.calcCircle(new Coord(1, 4), 5);
        Set<Coord> expected = Set.of(
                new Coord(-4, 5),
                new Coord(-2, 0),
                new Coord(4, 8),
                new Coord(6, 6),
                new Coord(-1, 9),
                new Coord(-4, 2),
                new Coord(6, 2),
                new Coord(5, 1),
                new Coord(1, -1),
                new Coord(-4, 6),
                new Coord(4, 0),
                new Coord(-3, 7),
                new Coord(2, 9),
                new Coord(-4, 3),
                new Coord(6, 3),
                new Coord(-1, -1),
                new Coord(6, 4),
                new Coord(2, -1),
                new Coord(-3, 1),
                new Coord(3, 9),
                new Coord(1, 9),
                new Coord(6, 5),
                new Coord(-4, 4),
                new Coord(5, 7),
                new Coord(-2, 8),
                new Coord(3, -1),
                new Coord(0, 9),
                new Coord(0, -1)
        );
        assertEquals(result, expected);
    }

    @Test
    public void shouldCalculateAnglesCorrectly() {
        Coord start = new Coord(0, 0);
        List<AngleResult> expectedResults = Arrays.asList(
                new AngleResult(start, new Coord(1, 1), PI / 4),
                new AngleResult(start, new Coord(0, 1), PI / 2),
                new AngleResult(start, new Coord(-1, 1), PI * 3 / 4),
                new AngleResult(start, new Coord(-1, 0), PI),
                new AngleResult(start, new Coord(-1, -1), -(PI * 3 / 4)),
                new AngleResult(start, new Coord(0, -1), -(PI / 2)),
                new AngleResult(start, new Coord(1, -1), -(PI / 4)),
                new AngleResult(start, new Coord(1, 0), 0)
        );

        expectedResults.forEach(expected -> {
            Coord a = expected.getA();
            Coord b = expected.getB();
            double expectedAngle = expected.getExpectedAngle();

            double result = CircleOperations.angleBetween(a, b);
            assertEquals(result, expectedAngle);
        });
    }

    @Test
    public void shouldChooseMostSimilar() {
        Coord base = new Coord(0, 0);
        Coord end = new Coord(100, 100);
        double targetAngle = CircleOperations.angleBetween(base, end);

        Coord a = new Coord(44, 43);
        Coord b = new Coord(44, 44);
        Coord c = new Coord(44, 45);

        Coord result = CircleOperations.mostSimilarAngle(a, b, end, targetAngle);
        assertEquals(result, b);

        Coord result2 = CircleOperations.mostSimilarAngle(b, c, end, targetAngle);
        assertEquals(result2, b);

        Coord end2 = new Coord(-10, 1);
        double targetAngle2 = CircleOperations.angleBetween(base, end2);

        Coord d = new Coord(-1, 2);
        Coord e = new Coord(-1, -1);

        Coord result3 = CircleOperations.mostSimilarAngle(d, e, end2, targetAngle2);
        assertEquals(result3, e);

        Coord end3 = new Coord(-10, -1);
        double targetAngle3 = CircleOperations.angleBetween(base, end3);

        Coord f = new Coord(-1, 1);
        Coord g = new Coord(-1, -2);

        Coord result4 = CircleOperations.mostSimilarAngle(f, g, end3, targetAngle3);
        assertEquals(result4, f);
    }

    @Test
    public void shouldMakeRayFromPointToPoint() {
        Coord start = new Coord(3, 5);
        Coord end = new Coord(10, 10);

        List<Coord> expected = CircleOperations.calculateRay(new Coord(7, 5)).stream()
                .map(start::combine)
                .collect(toList());
        List<Coord> result = CircleOperations.calculateRay(start, end);

        System.out.println(result);

        assertEquals(expected, result);
    }

    private static class AngleResult {
        private Coord a;
        private Coord b;
        private double expectedAngle;

        public AngleResult(Coord a, Coord b, double expectedAngle) {
            this.a = a;
            this.b = b;
            this.expectedAngle = expectedAngle;
        }

        public Coord getA() {
            return a;
        }

        public Coord getB() {
            return b;
        }

        public double getExpectedAngle() {
            return expectedAngle;
        }
    }
}