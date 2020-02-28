package com.jchevertonwynne.utils;

import com.jchevertonwynne.structures.Coord;

import java.awt.Color;

public class Common {
    public static final String BACKGROUND_NAME = "areas/mazeSmall.png";
    public static final Coord START_POSITION = new Coord(20, 20);

    public static final boolean DISPLAY = true;

    public static final int PATH_COLOUR = new Color(255, 255, 255).getRGB();
    public static final int ALL_KNOWN_PATH_COLOUR = new Color(0, 255, 0).getRGB();
    public static final int ALL_KNOWN_WALL_COLOUR = new Color(255, 0, 0).getRGB();
    public static final int SOME_KNOWN_PATH_COLOUR = new Color(102, 153, 0).getRGB();
    public static final int SOME_KNOWN_WALL_COLOUR = new Color(255, 153, 0).getRGB();
    public static final int KNOWN_PATH_COLOUR = new Color(102, 102, 51).getRGB();
    public static final int KNOWN_WALL_COLOUR = new Color(255, 255, 0).getRGB();

    public static final int AGENT_COUNT = 1;
    public static final int SIGHT_RADIUS = 30;
    public static final int BROADCAST_RADIUS = 60;

    public static final int DFS_SOFT_LIST_RETURN_LIMIT = 400;
    public static final int DFS_MAX_TURNS_WITHOUT_FIND = 1000;

    public static final boolean RANDOM_SELECTION = true;
    public static final int RANDOM_BEST_SELECT_LIMIT = 20;
}
