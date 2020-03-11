package com.jchevertonwynne.simulation;

import com.jchevertonwynne.display.Displayable;
import com.jchevertonwynne.pathing.PathMediator;
import com.jchevertonwynne.structures.BoundarySearchResult;
import com.jchevertonwynne.structures.Coord;
import com.jchevertonwynne.structures.Move;
import com.jchevertonwynne.structures.TileStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import static com.jchevertonwynne.pathing.AStarPathing.calculatePath;
import static com.jchevertonwynne.pathing.BoundarySearch.calculateBoundaryTiles;
import static com.jchevertonwynne.structures.Coord.CARDINAL_DIRECTIONS;
import static com.jchevertonwynne.utils.CircleOperations.generateCircleRays;
import static com.jchevertonwynne.utils.Common.SIGHT_RADIUS;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Comparator.comparingDouble;
import static java.util.Objects.hash;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class SwarmAgent implements Displayable {
    private enum AgentState {
        EXPLORING,
        RETURNING,
        FINISHED
    }

    private final Logger logger = LoggerFactory.getLogger(SwarmAgent.class);

    private Coord position;
    private Coord currentGoal;
    private final Coord startPosition;
    private final Color color;
    private AgentState agentState;

    private Scanner scanner;

    private int distanceMoved = 0;
    private int scansDone = 0;
    private boolean mediated;

    private Map<Coord, Boolean> world = new HashMap<>();
    private Map<Coord, Integer> distanceFromStart = new HashMap<>();
    private LinkedList<Coord> currentPath = new LinkedList<>();

    private Map<SwarmAgent, Set<Coord>> shareCache = new HashMap<>();

    private Set<Coord> blackList = new HashSet<>();
    private Set<Coord> whiteList = new HashSet<>();

    public SwarmAgent(Coord position, Color color) {
        this.startPosition = position;
        this.position = position;
        this.currentGoal = position;
        this.color = color;
        this.agentState = AgentState.EXPLORING;
        whiteList.add(position);
        world.put(position, true);
        distanceFromStart.put(position, 0);
        logger.debug("Initialising agent {} at {}", this, startPosition);
    }

    public void initialiseScanner(ScannerFactory scannerFactory) {
        scanner = scannerFactory.instance(this);
        scanner.getOtherLocalAgents().forEach(agent -> shareCache.put(agent, new HashSet<>()));
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

    public Map<Coord, Integer> getDistances() {
        return new HashMap<>(distanceFromStart);
    }

    public int getScansDone() {
        return scansDone;
    }

    public boolean isFinished() {
        return agentState.equals(AgentState.FINISHED);
    }

    public void shareWorldInfo(Map<Coord, Boolean> newInformation) {
        if (newInformation.size() > 0) {
            world.putAll(newInformation);
            reflowDistances();
        }
    }

    public Map<Coord, Boolean> getToShare(SwarmAgent agent) {
        Set<Coord> coords = shareCache.get(agent);
        Map<Coord, Boolean> coordsToShare = coords
                .stream()
                .collect(toMap(c -> c, world::get));
        coords.clear();
        return coordsToShare;
    }

    public boolean shareWithNeighbours(PathMediator mediator) {
        if (agentState.equals(AgentState.FINISHED) || Objects.equals(currentGoal, startPosition)) return false;

        // update all nearby agents with latest world info and get latest from them
        Set<SwarmAgent> otherLocalAgents = scanner.getOtherLocalAgents();
        otherLocalAgents.forEach(agent -> {
            agent.shareWorldInfo(getToShare(agent));
            shareWorldInfo(agent.getToShare(this));
        });

        boolean repathed = false;
        if (currentGoal != null) {
            Set<SwarmAgent> headingSameWay = otherLocalAgents.stream()
                    .filter(agent -> agent.getCurrentGoal() != null)
                    .filter(agent -> agent.getCurrentGoal().distance(currentGoal) < SIGHT_RADIUS)
                    .collect(toSet());

            for (SwarmAgent swarmAgent : headingSameWay) {
                repathed |= mediator.mediate(this, swarmAgent);
            }
        }

        return repathed;
    }

    public void processTurn() {
        if (agentState.equals(AgentState.FINISHED)) return;

        if (agentState.equals(AgentState.RETURNING)) {
            if (position.equals(startPosition)) {
                BoundarySearchResult boundarySearchResult = calculateBoundaryTiles(position, world, blackList);
                if (!boundarySearchResult.movesAvailable()) {
                    logger.info("Agent {} returned to start {} with distance moved {} and scans {}", this, position, distanceMoved, scansDone);
                    agentState = AgentState.FINISHED;
                }
            }
            return;
        }

        if (currentPath.size() > 0) return;

        if (mediated) {
            // other agent took its path
            mediated = false;
        }
        else if (Objects.equals(currentGoal, position)) {
            // agent made it to its scan destination
            logger.info("Agent {} scanning at {}", this,  position);
            scanArea();
            reflowDistances();
        }

        chooseNextMove(calculateBoundaryTiles(position, world, blackList));
    }

    private void chooseNextMove(BoundarySearchResult boundarySearchResult) {
        List<Move> legalMoves = boundarySearchResult.getLegalMoves();

        if (legalMoves.size() != 0) {
            Coord tile = chooseNextMove(legalMoves);
            logger.info("Agent {} now moving to {} from {}", this, tile, position);
            currentPath = calculatePath(position, tile, world);
        }
        else {
            List<Move> blacklistedMoves = boundarySearchResult.getBlacklistedMoves();
            if (blacklistedMoves.size() != 0) {
                Coord tile = chooseNextMove(blacklistedMoves);
                blackList.remove(tile);
                whiteList.add(tile);
                logger.info("Agent {} white listing and going to {} from {}", this, tile, position);
                currentPath = calculatePath(position, tile, world);
            }
            else {
                logger.info("Agent {} going back to start {} from {}", this,  startPosition, position);
                currentPath = calculatePath(position, startPosition, world);
                agentState = AgentState.RETURNING;
            }
        }
        currentGoal = currentPath.getLast();
    }

    private Coord chooseNextMove(List<Move> choices) {
        Comparator<Move> moveComparator = comparingDouble(this::evaluateGoodness);
        if (choices.size() == 0) throw new IllegalArgumentException("A zero size list is not allowed");
        return choices.stream()
                .max(moveComparator)
                .get()
                .getTile();
    }

    /**
     * Measure of how 'good' a potential move is for ranking purposes
     * @param move Distance-Coord pair
     * @return double Arbitrary score number of goodness
     */
    private double evaluateGoodness(Move move) {
        try {
            Coord tile = move.getTile();
            int distance = distanceFromStart.get(tile);
            int distanceTo = move.getDistance();
            int discoverable = calculatePotentialNewVisible(tile);
            return distance * exp(- distanceTo) * log(discoverable);
        }
        catch (NullPointerException e) {
            System.out.println("lmao");
        }
        return 0;
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
        if (!agentState.equals(AgentState.FINISHED)) {
            try {
                position = currentPath.removeFirst();
                distanceMoved++;
            }
            catch (NoSuchElementException e) {
                System.out.println("stop this");
            }

        }
    }

    private List<Coord> neighbours(Coord coord) {
        return CARDINAL_DIRECTIONS.stream()
                .map(coord::combine)
                .filter(world::containsKey)
                .filter(world::get)
                .collect(toList());
    }

    private List<Coord> undiscoveredNeighbours(Coord coord) {
        if (coord == null) return emptyList();
        return CARDINAL_DIRECTIONS.stream()
                .map(coord::combine)
                .filter(neighbour -> !world.containsKey(neighbour))
                .collect(toList());
    }

    private void reflowDistances() {
        List<Coord> toTry = new LinkedList<>(neighbours(startPosition));
        Set<Coord> seen = new HashSet<>(singleton(startPosition));
        while (!toTry.isEmpty()) {
            Coord coord = toTry.remove(0);
            if (seen.contains(coord)) continue;
            seen.add(coord);
            List<Coord> neighbours = neighbours(coord);
            int neighbourScore = neighbours.stream()
                    .filter(seen::contains)
                    .mapToInt(distanceFromStart::get)
                    .min()
                    .getAsInt();
            distanceFromStart.put(coord, neighbourScore + 1);
            neighbours.stream()
                    .filter(n -> !seen.contains(n))
                    .forEach(toTry::add);
        }
    }

    public void setWorldStatus(TileStatus status) {
        Coord coord = status.getCoord();
        world.put(coord, status.isPathable());
        shareCache.forEach((agent, toShare) -> toShare.add(coord));
    }

    public boolean blacklistCoord(Coord coord) {
        if (whiteList.stream().allMatch(w -> w.distance(coord) > SIGHT_RADIUS)) {
            blackList.add(coord);
            currentPath.clear();
            mediated = true;
            return true;
        }
        return false;
    }

    public void clearCurrentPath() {
        currentPath.clear();
        currentGoal = startPosition;
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
        graphics.setColor(Color.BLACK);
        graphics.drawOval(position.getX() - 4, position.getY() - 4, 8, 8);
        graphics.drawOval(position.getX() - 3, position.getY() - 3, 6, 6);
        graphics.setColor(color);
        graphics.fillOval(position.getX() - 4, position.getY() - 4, 8, 8);
    }
}
