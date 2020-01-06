package com.jchevertonwynne;

import com.jchevertonwynne.structures.Coord;
import com.jchevertonwynne.structures.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.geom.IllegalPathStateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import static com.jchevertonwynne.AStarPathing.calculatePath;
import static com.jchevertonwynne.BoundarySearch.findAvailable;
import static com.jchevertonwynne.structures.CircleOperations.getCircleRays;
import static com.jchevertonwynne.structures.Common.RANDOM_BEST_SELECT_LIMIT;
import static com.jchevertonwynne.structures.Common.SIGHT_RADIUS;
import static java.lang.Math.log;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.util.Comparator.comparingDouble;
import static java.util.Objects.hash;

public class SwarmAgent {
    Logger logger = LoggerFactory.getLogger(SwarmAgent.class);

    private Coord startPosition;
    private Coord position;
    private Color color;
    private Map<Coord, Boolean> world = new HashMap<>();
    private List<Coord> currentPath = new LinkedList<>();
    private Scanner scanner;
    private int scansDone = 0;
    private List<Coord> newlyDone = new LinkedList<>();
    private List<Coord> newPathTaken = new LinkedList<>();
    private DiscoveryMode discoveryMode = DiscoveryMode.LARGE;
    private boolean finished = false;
    private int turns = 0;

    private enum DiscoveryMode {
        LARGE,
        SMALL
    }

    public SwarmAgent(Coord position, Color color, Scanner scanner) {
        this.position = position;
        this.color = color;
        this.scanner = scanner;
        world.put(position, true);
        startPosition = position;

        logger.debug("Initialising agent {} at {}", color.getRGB(), position.toString());
    }

    public Coord getPosition() {
        return position;
    }

    public Color getColor() {
        return color;
    }

    public Map<Coord, Boolean> getWorld() {
        return world;
    }

    private List<Coord> getCurrentPath() {
        return currentPath;
    }

    private void setCurrentPath(List<Coord> currentPath) {
        this.currentPath = currentPath;
    }

    public int getScansDone() {
        return scansDone;
    }

    public boolean getFinished() {
        return finished;
    }

    /**
     * Keeps record of newly discovered tiles so that drawing to screen can be more efficient
     * @return Tiles discovered since last scan
     */
    public List<Coord> getNewlyDone() {
        List<Coord> result = newlyDone;
        newlyDone = new LinkedList<>();
        return result;
    }

    public List<Coord> getNewPathTaken() {
        List<Coord> result = newPathTaken;
        newPathTaken = new ArrayList<>();
        return result;
    }

    public void calculateNextMove() {
        if (!finished && getCurrentPath().size() == 0) {
            if (position.equals(startPosition) && turns != 0) {
                finished = true;
                logger.info("Agent {} returned to start {}", color.getRGB(),  position.toString());
            }
            else {
                logger.info("Agent {} scanning at {}", color.getRGB(),  position.toString());
                scanArea();
                logger.info("Agent {} scanned and discovered {} coords", color.getRGB(), newlyDone.size());
                PriorityQueue<Move> pq = new PriorityQueue<>(comparingDouble(this::evaluateGoodness));
                pq.addAll(findAvailable(position, world));

                if (pq.size() == 0) {
                    logger.info("Agent {} going back to start {} from {}", color.getRGB(),  startPosition.toString(), position.toString());
                    setCurrentPath(calculatePath(position, startPosition, world));
                }
                else {
                    int choices = min(RANDOM_BEST_SELECT_LIMIT, pq.size());
                    List<Move> moveOptions = new ArrayList<>(RANDOM_BEST_SELECT_LIMIT);
                    for (int i = 0; i < choices; i++) {
                        moveOptions.add(pq.poll());
                    }

                    Coord tile = moveOptions.get(new Random().nextInt(choices)).getTile();
                    logger.info("Agent {} now moving to {}", color.getRGB(), tile.toString());

                    setCurrentPath(calculatePath(position, tile, world));
                    int potentialDiscovered = getPotentialNewVisible(tile);
                    logger.info("Agent {} potentially discovering {} coords", color.getRGB(), potentialDiscovered);
                    double smallCutoff = 0.5 * pow(SIGHT_RADIUS, 2);
                    DiscoveryMode m = potentialDiscovered > smallCutoff ? DiscoveryMode.LARGE : DiscoveryMode.SMALL;
                    if (m != discoveryMode) {
                        logger.info("Agent {} switching to mode {}", color.getRGB(), m.toString());
                    }
                    discoveryMode = m;

                }
            }
            turns++;
        }
    }

    public void applyNextMove() {
        if (!finished) {
            newPathTaken.add(position);
            position = getCurrentPath().remove(0);
        }
    }

    /**
     * Measure of how 'good' a potential move is for ranking purposes
     * @param move Distance-Coord pair
     * @return double Arbitrary score number of goodness
     */
    private double evaluateGoodness(Move move) {
        switch(discoveryMode) {
            case LARGE: return largeDiscovery(move);
            case SMALL: return smallDiscovery(move);
        }
        throw new IllegalPathStateException();
    }

    private  double smallDiscovery(Move move) {
        return move.getDistance();
    }

    private double largeDiscovery(Move move) {
        int discoverable = getPotentialNewVisible(move.getTile());
        double distance = move.getDistance();
        return - (discoverable / log(distance));
    }

    /**
     * Calculate how many new tiles may be discovered from new position
     * @param coord Position to look for new
     * @return number of potentially visible tiles from coord
     */
    private int getPotentialNewVisible(Coord coord) {
        List<List<Coord>> rays = getCircleRays(coord, SIGHT_RADIUS);
        Set<Coord> seen = new HashSet<>();
        int result = 0;

        for (List<Coord> ray : rays) {
            for (Coord rayCoord : ray) {
                if (!world.getOrDefault(rayCoord, true)) {
                    break;
                }
                if (seen.add(rayCoord) && !world.containsKey(rayCoord)) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Scan area, increase scan counter and store previous positions
     */
    private void scanArea() {
        scansDone++;
        scanner.scan(this);
    }

    public void noteNewlyDone(Coord coord) {
        newlyDone.add(coord);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwarmAgent that = (SwarmAgent) o;
        return color.equals(that.color);
    }

    @Override
    public int hashCode() {
        return hash(color);
    }
}
