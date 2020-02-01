package com.jchevertonwynne.simulation;

import com.jchevertonwynne.structures.BoundarySearchResult;
import com.jchevertonwynne.structures.Coord;
import com.jchevertonwynne.structures.Move;
import com.jchevertonwynne.structures.TileStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static com.jchevertonwynne.pathing.AStarPathing.calculatePath;
import static com.jchevertonwynne.pathing.BoundarySearch.calculateBoundaryTiles;
import static com.jchevertonwynne.utils.CircleOperations.generateCircleRays;
import static com.jchevertonwynne.utils.Common.RANDOM_BEST_SELECT_LIMIT;
import static com.jchevertonwynne.utils.Common.RANDOM_SELECTION;
import static com.jchevertonwynne.utils.Common.SIGHT_RADIUS;
import static java.lang.Math.log;
import static java.lang.Math.min;
import static java.util.Comparator.comparingDouble;
import static java.util.Objects.hash;

public class SwarmAgent {
    private final Logger logger = LoggerFactory.getLogger(SwarmAgent.class);

    private Coord position;
    private Coord currentGoal;
    private final Coord startPosition;
    private final Color color;

    private final ScannerFactory scannerFactory;
    private Scanner scanner;

    private int turn = 0;
    private int scansDone = 0;
    private boolean finished = false;

    private Map<Coord, Boolean> world = new HashMap<>();
    private List<Coord> currentPath = new LinkedList<>();

    private Map<SwarmAgent, List<TileStatus>> otherAgentsMemo = new HashMap<>();
    private Set<Coord> blackList = new HashSet<>();

    private final Random random;

    public SwarmAgent(Coord position, Color color, ScannerFactory scannerFactory) {
        this.startPosition = position;
        this.position = position;
        this.color = color;
        this.scannerFactory = scannerFactory;
        this.random = new Random();
        world.put(position, true);

        logger.debug("Initialising agent {} at {}", this, startPosition);
    }

    public void initialiseScanner() {
        scanner = scannerFactory.instance(this);
        scanner.getOtherLocalAgents().forEach(agent -> otherAgentsMemo.put(agent, new ArrayList<>()));
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

    public Map<Coord, Boolean> getWorld() {
        return new HashMap<>(world);
    }

    public int getScansDone() {
        return scansDone;
    }

    public boolean getFinished() {
        return finished;
    }

    public void shareWorldInfo(List<TileStatus> newInformation) {
        newInformation.forEach(info -> {
            Coord tile = info.getCoord();
            boolean tileStatus = info.isStatus();
            world.put(tile, tileStatus);
        });
    }

    public List<TileStatus> serveNewTileStatuses(SwarmAgent agent) {
        List<TileStatus> tileStatuses = otherAgentsMemo.get(agent);
        ArrayList<TileStatus> statusesCopy = new ArrayList<>(tileStatuses);
        tileStatuses.clear();
        return statusesCopy;
    }

    public void processTurn() {
        // update all nearby agents with latest world info and get latest from them
        Set<SwarmAgent> otherLocalAgents = scanner.getOtherLocalAgents();
        otherLocalAgents.forEach(agent -> {
            List<TileStatus> tileStatuses = otherAgentsMemo.get(agent);
            ArrayList<TileStatus> statusesCopy = new ArrayList<>(tileStatuses);
            tileStatuses.clear();
            agent.shareWorldInfo(statusesCopy);
            shareWorldInfo(agent.serveNewTileStatuses(this));
        });

        if (turn > 0) {
            Set<SwarmAgent> headingSameWay = otherLocalAgents.stream()
                    .filter(agent -> agent.getCurrentGoal().distance(currentGoal) < SIGHT_RADIUS)
                    .collect(Collectors.toSet());
//            if (headingSameWay.size() > 0) {
//
//            }
        }


        // if agent is done or still on path we let it do its thing
        if (finished || currentPath.size() != 0) {
            return;
        }

        // agent has explored and now returned back to beginning
        if (position.equals(startPosition) && turn != 0) {
            finished = true;
            logger.info("Agent {} returned to start {} in {} turns", this,  position, turn);
        }
        else {
           chooseNextMove();
        }
        turn++;
    }

    private void chooseNextMove() {
        logger.info("Agent {} scanning at {}", this,  position);
        scanArea();

        BoundarySearchResult boundarySearchResult = calculateBoundaryTiles(position, world, blackList);
        List<Move> legalMoves = boundarySearchResult.getLegalMoves();

        if (legalMoves.size() != 0) {
            Coord tile = chooseNextMove(legalMoves);
            logger.info("Agent {} now moving to {}", this, tile.toString());
            currentPath = calculatePath(position, tile, world);
        }
        else {
            List<Move> blacklistedMoves = boundarySearchResult.getBlacklistedMoves();
            if (blacklistedMoves.size() == 0) {
                logger.info("Agent {} going back to start {} from {}", this,  startPosition, position);
                currentPath = calculatePath(position, startPosition, world);
            }
            else {
                Coord tile = chooseNextMove(blacklistedMoves);
                blackList.remove(tile);
                logger.info("Agent {} now moving to {}", this, tile.toString());
                currentPath = calculatePath(position, tile, world);
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
        return -(discoverable / log(distance));
    }

    /**
     * Calculate how many new tiles may be discovered from new position
     * @param coord Position to look for new
     * @return number of potentially visible tiles from coord
     */
    private int calculatePotentialNewVisible(Coord coord) {
        List<List<Coord>> rays = generateCircleRays(coord, SIGHT_RADIUS);
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
        scanner.scan();
        scansDone++;
    }

    public void applyNextMove() {
        if (!finished) {
            position = currentPath.remove(0);
        }
    }

    public void noteNewlyDone(TileStatus status) {
        otherAgentsMemo.forEach((agent, tileStatusList) -> tileStatusList.add(status));
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
}
