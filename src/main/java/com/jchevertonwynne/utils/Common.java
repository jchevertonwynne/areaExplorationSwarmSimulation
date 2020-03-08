package com.jchevertonwynne.utils;

import com.jchevertonwynne.structures.Coord;

import java.awt.Color;

public class Common {
    public static final String BACKGROUND_NAME = "areas/mazeMedium.png";
    public static final Coord START_POSITION = new Coord(20, 20);

    public static final boolean DISPLAY = true;
    public static final boolean DISTANCE_DISPLAY = true;

    public static final int PATH_COLOUR = new Color(255, 255, 255).getRGB();
    public static final int ALL_KNOWN_PATH_COLOUR = new Color(0, 255, 0).getRGB();
    public static final int ALL_KNOWN_WALL_COLOUR = new Color(255, 0, 0).getRGB();
    public static final int SOME_KNOWN_PATH_COLOUR = new Color(102, 153, 0).getRGB();
    public static final int SOME_KNOWN_WALL_COLOUR = new Color(255, 153, 0).getRGB();
    public static final int KNOWN_PATH_COLOUR = new Color(102, 102, 51).getRGB();
    public static final int KNOWN_WALL_COLOUR = new Color(255, 255, 0).getRGB();

    public static final int DFS_MAX_TURNS_WITHOUT_FIND = 400;
    public static final int DFS_RETURN_SOFT_CAP = 400;

    public static final int AGENT_COUNT = 1;
    public static final int SIGHT_RADIUS = 30;
    public static final int BROADCAST_RADIUS = 60;
}
