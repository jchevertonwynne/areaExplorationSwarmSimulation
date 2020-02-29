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

        if (aDistanceToGoal < bDistanceToGoal) {
            logger.info("Agent {} continuing, agent {} to make new choice of move", a, b);
            Coord toBan = a.getCurrentGoal();
            b.clearCurrentPath();
            return b.blacklistCoord(toBan);
        }
        else {
            logger.info("Agent {} continuing, agent {} to make new choice of move", b, a);
            Coord toBan = b.getCurrentGoal();
            a.clearCurrentPath();
            return a.blacklistCoord(toBan);
        }
    }
}
