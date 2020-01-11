package com.jchevertonwynne.pathing;

import com.jchevertonwynne.structures.Coord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

class AStarPathingTest {

    @Test
    public void shouldReturnShortestRoute() {
        Map<Coord, Boolean> world = testWorld();

        List<Coord> resultPath1 = AStarPathing.calculatePath(
                new Coord(2, 1),
                new Coord(8, 7),
                world
        );
        assertThat(resultPath1, hasSize(12));

        List<Coord> resultPath2 = AStarPathing.calculatePath(
                new Coord(3, 6),
                new Coord(7, 3),
                world
        );
        assertThat(resultPath2, hasSize(13));
    }

    private Map<Coord, Boolean> testWorld() {
        Map<Coord, Boolean> world = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                world.put(new Coord(i, j), !((i % 9 == 0) || (j % 9 == 0)));
            }
        }

        for (int i = 2; i < 8; i++) {
            world.put(new Coord(i, 5), false);
        }

        for (int i = 2; i < 8; i++) {
            world.put(new Coord(5, i), false);
        }

        return world;
    }
}