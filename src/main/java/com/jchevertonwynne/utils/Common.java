package com.jchevertonwynne.utils;

import com.jchevertonwynne.structures.Coord;

public class Common {
    public static final String BACKGROUND_NAME = "areas/mazeSmallest.png";
    public static final Coord START_POSITION = new Coord(20, 20);

    public static final boolean DISPLAY = true;
    public static final boolean DISTANCE_DISPLAY = false;
    public static final boolean UNIFORM_AGENT_COLOUR = true;

    public static final int DFS_MAX_TURNS_WITHOUT_FIND = 400;
    public static final int DFS_RETURN_SOFT_CAP = 400;

    public static final boolean GLOBAL_KNOWLEDGE = false;
    public static final boolean EMPLOY_DROPS = false;
    public static final int AGENT_COUNT = 3;
    
    public static final int SIGHT_RADIUS = 30;
    public static final int BROADCAST_RADIUS = 60;
}
