package com.jchevertonwynne.structures;

import com.jchevertonwynne.simulation.SwarmAgent;
import lombok.Value;

@Value
public class Drop {
    Coord coord;
    SwarmAgent agent;
}
