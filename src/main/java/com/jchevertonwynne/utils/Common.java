package com.jchevertonwynne.utils;

import com.jchevertonwynne.structures.Coord;

import java.awt.Color;

public class Common {
    private Common() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String BACKGROUND_NAME = "areas/floorPlan.png";
    public static final Coord START_POSITION = new Coord(20, 20);
    public static final int AGENT_COUNT = 5;
    public static final int SIGHT_RADIUS = 20;
    public static final int DFS_SOFT_LIST_RETURN_LIMIT = 200;
    public static final int DFS_MAX_TURNS_WITHOUT_FIND = 50;
    public static final boolean RANDOM_SELECTION = true;
    public static final int RANDOM_BEST_SELECT_LIMIT = 11;
    public static final int PATH_COLOR = new Color(255, 255, 255).getRGB();
}
