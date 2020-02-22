package com.jchevertonwynne.simulation;

import com.jchevertonwynne.display.Displayable;
import com.jchevertonwynne.pathing.PathMediator;
import com.jchevertonwynne.structures.BoundarySearchResult;
import com.jchevertonwynne.structures.CircleResult;
import com.jchevertonwynne.structures.Coord;
import com.jchevertonwynne.structures.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import static com.jchevertonwynne.pathing.AStarPathing.calculatePath;
import static com.jchevertonwynne.pathing.BoundarySearch.calculateBoundaryTiles;
import static com.jchevertonwynne.utils.CircleOperations.generateCircleRays;
import static com.jchevertonwynne.utils.Common.RANDOM_BEST_SELECT_LIMIT;
import static com.jchevertonwynne.utils.Common.RANDOM_SELECTION;
import static com.jchevertonwynne.utils.Common.SIGHT_RADIUS;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparingDouble;
import static java.util.Objects.hash;
import static java.util.stream.Collectors.toList;

public class SwarmAgent implements Displayable {
    private final Logger logger = LoggerFactory.getLogger(SwarmAgent.class);

    private Coord position;
    private Coord currentGoal;
    private final Coord startPosition;
    private final Color color;

    private Scanner scanner;

    private int turn = 0;
    private int scansDone = 0;
    private boolean finished;
    private boolean mediated;

    private Map<Coord, Boolean> world = new HashMap<>();
    private List<Coord> currentPath = new ArrayList<>();

    private Set<Coord> blackList = new HashSet<>();
    private Set<Coord> whiteList = new HashSet<>();

    private final Random random;

    public SwarmAgent(Coord position, Color color) {
        this.startPosition = position;
        this.position = position;
        this.color = color;
        this.random = new Random();
        world.put(position, true);

        logger.debug("Initialising agent {} at {}", this, startPosition);
    }

    public void initialiseScanner(ScannerFactory scannerFactory) {
        scanner = scannerFactory.instance(this);
    }

    public Coord getPosition() {
        return position;
    }

    public Coord getCurrentGoal() {
        return currentGoal;
    }

    public Color getColor() {
        return color;
    }

    public void setWorld(Map<Coord, Boolean> newWorld) {
        world = newWorld;
    }

    public Map<Coord, Boolean> getWorld() {
        return new HashMap<>(world);
    }

    public int getScansDone() {
        return scansDone;
    }

    public boolean getFinished() {
        return finished;
    }

    public void receiveWorldInfo(Map<Coord, Boolean> newInformation) {
        world.putAll(newInformation);
    }

    public void shareWithNeighbours(PathMediator mediator) {
        // update all nearby agents with latest world info and get latest from them
        List<SwarmAgent> otherLocalAgents = scanner.getOtherLocalAgents();
        otherLocalAgents.forEach(agent -> {
            Map<Coord, Boolean> w = getWorld();
            receiveWorldInfo(agent.getWorld());
            agent.receiveWorldInfo(getWorld());
        });

        if (turn > 0) {
            List<SwarmAgent> headingSameWay = otherLocalAgents.stream()
                    .filter(agent -> agent.getCurrentGoal().distance(currentGoal) < SIGHT_RADIUS)
                    .collect(toList());
            headingSameWay.forEach(other -> mediator.mediate(this, other));
        }
    }

    public void processTurn() {
        // if agent is done or still on path we let it do its thing
        if (finished || currentPath.size() != 0) {
            return;
        }

        BoundarySearchResult boundarySearchResult = calculateBoundaryTiles(position, world, blackList);

        // agent has explored and now returned back to beginning
        if (position.equals(startPosition) && !boundarySearchResult.movesAvailable()) {
            finished = true;
            logger.info("Agent {} returned to start {} in {} turns", this,  position, turn);
        }
        else {
           chooseNextMove(boundarySearchResult);
        }
        turn++;
    }

    private void chooseNextMove(BoundarySearchResult boundarySearchResult) {
        logger.info("Agent {} scanning at {}", this,  position);
        if (mediated) {
            mediated = false;
        }
        else {
            scanArea();
        }

        List<Move> legalMoves = boundarySearchResult.getLegalMoves();

        if (legalMoves.size() != 0) {
            Coord tile = chooseNextMove(legalMoves);
            logger.info("Agent {} now moving to {}", this, tile.toString());
            currentPath = calculatePath(position, tile, world);
        }
        else {
            List<Move> blacklistedMoves = boundarySearchResult.getBlacklistedMoves();
            if (blacklistedMoves.size() != 0) {
                Coord tile = chooseNextMove(blacklistedMoves);
                blackList.remove(tile);
                whiteList.add(tile);
                logger.info("Agent {} now moving to {}", this, tile.toString());
                currentPath = calculatePath(position, tile, world);
            }
            else {
                logger.info("Agent {} going back to start {} from {}", this,  startPosition, position);
                currentPath = calculatePath(position, startPosition, world);
            }
        }
        currentGoal = position;
    }

    private Coord chooseNextMove(List<Move> choices) {
        if (choices.size() == 0) {
            throw new IllegalArgumentException("A zero size list is not allowed");
        }
        if (RANDOM_SELECTION) {
            int available = min(RANDOM_BEST_SELECT_LIMIT, choices.size());
            PriorityQueue<Move> pq = new PriorityQueue<>(comparingDouble(this::evaluateGoodness));
            pq.addAll(choices);
            List<Move> moveOptions = new ArrayList<>(pq).subList(0, available);
            return moveOptions.get(random.nextInt(available)).getTile();
        }
        else {
            return choices.stream().min(comparingDouble(this::evaluateGoodness)).get().getTile();
        }
    }

    /**
     * Measure of how 'good' a potential move is for ranking purposes
     * @param move Distance-Coord pair
     * @return double Arbitrary score number of goodness
     */
    private double evaluateGoodness(Move move) {
        int discoverable = calculatePotentialNewVisible(move.getTile());
        double distance = move.getDistance();
        return discoverable - pow(distance, 0.5);
    }

    /**
     * Calculate how many new tiles may be discovered from new position
     * @param coord Position to look for new
     * @return number of potentially visible tiles from coord
     */
    private int calculatePotentialNewVisible(Coord coord) {
        CircleResult circleResult = generateCircleRays(coord, SIGHT_RADIUS);
        Set<Coord> seen = new HashSet<>();
        int result = 0;

        List<Coord> coordsToProcess = singletonList(circleResult.getStart());
        Map<Coord, List<Coord>> rays = circleResult.getRays();

        while (coordsToProcess.size() != 0) {

            List<Coord> nextToProcess = new ArrayList<>();
            for (Coord rayCoord : coordsToProcess) {
                if (!world.getOrDefault(rayCoord, true)) {
                    continue;
                }
                if (seen.add(rayCoord) && !world.containsKey(rayCoord)) {
                    result++;
                }
            }
            coordsToProcess = nextToProcess;
        }

        return result;
    }

    /**
     * Scan area, increase scan counter and store previous positions
     */
    private void scanArea() {
        scanner.scan();
        scansDone++;
    }

    public void applyNextMove() {
        if (!finished) {
            try {
                position = currentPath.remove(0);
            }
            catch (IndexOutOfBoundsException e) {
                System.out.println("Please don't do this");
            }
        }
    }

    public void setWorldStatus(Coord coord, boolean pathable) {
        world.put(coord, pathable);
    }

    public void blacklistCoord(Coord coord) {
        if (!whiteList.contains(coord)) {
            blackList.add(coord);
            mediated = true;
        }
    }

    public void clearCurrentPath() {
        currentPath.clear();
        currentGoal = position;
    }

    public double distanceFrom(Coord coord) {
        return position.distance(coord);
    }

    public double distanceFrom(SwarmAgent agent) {
        return position.distance(agent.getPosition());
    }

    public double distanceToGoal() {
        return position.distance(currentGoal);
    }

    @Override
    public String toString() {
        return color.toString().substring(14);
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

    @Override
    public void display(BufferedImage image) {
        Graphics graphics = image.getGraphics();
        graphics.setColor(color);
        graphics.fillOval(position.getX() - 4, position.getY() - 4, 8, 8);
    }
}
