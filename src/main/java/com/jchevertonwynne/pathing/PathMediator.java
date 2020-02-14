package com.jchevertonwynne.pathing;

import com.jchevertonwynne.simulation.SwarmAgent;
import com.jchevertonwynne.structures.Coord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.hash;

public class PathMediator {
    private static Logger logger = LoggerFactory.getLogger(PathMediator.class);
    private Set<Integer> checked = new HashSet<>();

    public void mediate(SwarmAgent a, SwarmAgent b) {
        int combinedHash = hash(a.hashCode(), b.hashCode());

        if (checked.contains(combinedHash)) {
            return;
        }

        logger.info("Mediating pathing between agent {} and agent {}", a, b);
        double aDistanceToGoal = a.distanceToGoal();
        double bDistanceToGoal = b.distanceToGoal();

        if (aDistanceToGoal < bDistanceToGoal) {
            logger.info("Agent {} continuing, agent {} to make new choice of move", a, b);
            Coord toBan = a.getCurrentGoal();
            b.blacklistCoord(toBan);
            b.clearCurrentPath();
        }
        else {
            logger.info("Agent {} continuing, agent {} to make new choice of move", b, a);
            Coord toBan = b.getCurrentGoal();
            a.blacklistCoord(toBan);
            a.clearCurrentPath();
        }

        checked.add(combinedHash);
    }
}
