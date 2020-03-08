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
            Coord toBan = a.getCurrentGoal();
            b.clearCurrentPath();
            boolean blacklisted = b.blacklistCoord(toBan);
            if (blacklisted) logger.info("Agent {} continuing, agent {} to make new choice of move", a, b);
            return blacklisted;
        }
        else {
            Coord toBan = b.getCurrentGoal();
            a.clearCurrentPath();
            boolean blacklisted = a.blacklistCoord(toBan);
            if (blacklisted) logger.info("Agent {} continuing, agent {} to make new choice of move", b, a);
            return  blacklisted;
        }
    }
}
