package com.jchevertonwynne.pathing;

import com.jchevertonwynne.simulation.SwarmAgent;
import com.jchevertonwynne.structures.Coord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class PathMediator {
    private static Logger logger = LoggerFactory.getLogger(PathMediator.class);
    private Set<Integer> checked = new HashSet<>();

    public boolean mediate(SwarmAgent a, SwarmAgent b) {
        int combinedHash = a.hashCode() ^ b.hashCode();

        if (checked.contains(combinedHash)) {
            return false;
        }
        checked.add(combinedHash);

        double aDistanceToGoal = a.distanceToGoal();
        double bDistanceToGoal = b.distanceToGoal();

        SwarmAgent x;
        SwarmAgent y;

        if (aDistanceToGoal < bDistanceToGoal) {
            x = a;
            y = b;
        }
        else {
            x = b;
            y = a;
        }

        Coord toBan = x.getCurrentGoal();
        boolean blacklisted = y.blacklistCoord(toBan);
        if (blacklisted) logger.info("Agent {} continuing to {}, agent {} to make new choice of move", x, toBan, y);
        return blacklisted;
    }
}
