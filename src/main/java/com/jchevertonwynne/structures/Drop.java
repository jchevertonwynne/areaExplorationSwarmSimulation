package com.jchevertonwynne.structures;

import com.jchevertonwynne.simulation.SwarmAgent;
import lombok.Value;

@Value
public class Drop {
    private Coord coord;
    private SwarmAgent agent;
}
