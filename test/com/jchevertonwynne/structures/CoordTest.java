package com.jchevertonwynne.structures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordTest {
    @Test
    public void shouldCompareOnXAndY() {
        Coord a = new Coord(1, 2);
        Coord b = new Coord(1, 3);
        Coord c = new Coord(3, 2);
        Coord d = new Coord(3, 3);
        Coord e = new Coord(1, 2);

        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertEquals(a, e);
    }
}