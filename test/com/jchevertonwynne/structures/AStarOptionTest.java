package com.jchevertonwynne.structures;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AStarOptionTest {

    @Test
    public void shouldCompareCorrectly() {
        AStarOption smallest = new AStarOption(2, 10, null, null);
        AStarOption medium = new AStarOption(3, 10, null, null);
        AStarOption biggest = new AStarOption(4, 10, null, null);

        List<AStarOption> options = Arrays.asList(biggest, smallest, medium);
        options.sort(AStarOption.aStarOptionComparator);

        assertEquals(options, Arrays.asList(smallest, medium, biggest));
    }
}