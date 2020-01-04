package com.jchevertonwynne;

import com.jchevertonwynne.structures.Coord;
import com.jchevertonwynne.structures.Move;
import com.jchevertonwynne.structures.MoveHistory;

import java.awt.Color;
import java.awt.geom.IllegalPathStateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.jchevertonwynne.structures.CircleOperations.getCircleRays;
import static com.jchevertonwynne.structures.Common.DFS_MAX_TURNS_WITHOUT_FIND;
import static com.jchevertonwynne.structures.Common.DFS_SOFT_LIST_RETURN_LIMIT;
import static com.jchevertonwynne.structures.Common.SIGHT_RADIUS;
import static java.lang.Math.pow;

public class SwarmAgent {
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
        if (finished) {
            return;
        }
        if (getCurrentPath().size() == 0) {
            if (position.equals(startPosition) && turns != 0) {
                finished = true;
                System.out.println("Agent has returned to start position!");
                return;
            }
            scanArea();
            List<Move> availableTiles = findAvailable();
            Optional<Move> closestMove = availableTiles.stream().max(Comparator.comparingDouble(this::evaluateGoodness));
            closestMove.ifPresentOrElse(move -> {
                Coord tile = move.getTile();
                setCurrentPath(AStarPathing.calculatePath(position, tile, world));
                int potentialDiscovered = getPotentialNewVisible(tile);
                double smallCutoff = 5 * pow(SIGHT_RADIUS, 2);
                DiscoveryMode m = potentialDiscovered > smallCutoff ? DiscoveryMode.LARGE : DiscoveryMode.SMALL;
                if (m != discoveryMode) {
                    System.out.printf("Switching mode to %s\n", m.toString());
                }
                discoveryMode = m;
                System.out.println(potentialDiscovered);
            }, () -> {
                System.out.printf("going back to start from coord %s\n", position.toString());
                setCurrentPath(AStarPathing.calculatePath(position, startPosition, world));
            });
        }
    }

    public void applyNextMove() {
        if (!finished) {
            newPathTaken.add(position);
            position = getCurrentPath().remove(0);
            turns++;
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
        return - move.getDistance();
    }

    private double largeDiscovery(Move move) {
        int discoverable = getPotentialNewVisible(move.getTile());
        double distance = move.getDistance();
        return discoverable / pow(distance, 2);
    }

    /**
     * Calculate all edge tiles on current agent's world knowledge
     * @return List of first <= ~400 boundary tiles
     */
    private List<Move> findAvailable() {
        List<MoveHistory> toCheck = new LinkedList<>();
        toCheck.add(new MoveHistory(null, position));

        Set<Coord> seen = new HashSet<>();
        seen.add(position);

        int distance = 0;
        List<Move> result = new LinkedList<>();
        Set<Coord> resultTiles = new HashSet<>();

        int turnsWithoutFind = 0;

        while (!toCheck.isEmpty() && result.size() < DFS_SOFT_LIST_RETURN_LIMIT) {
            if (!result.isEmpty() && turnsWithoutFind > DFS_MAX_TURNS_WITHOUT_FIND) {
                return result;
            }
            distance++;
            turnsWithoutFind++;
            List<MoveHistory> nextAvailable = new LinkedList<>();
            toCheck.forEach(move -> {
                Coord pos = move.getCurrentTile();
                if (world.getOrDefault(pos, false)) {
                    nextAvailable.addAll(
                            getUnvisited(pos, seen).stream()
                                    .map(newPos -> new MoveHistory(pos, newPos))
                                    .collect(Collectors.toList())
                    );
                }
            });
            List<MoveHistory> nextToCheck = new LinkedList<>();
            int finalDistance = distance;
            for (MoveHistory nextPair : nextAvailable) {
                Coord previousTile = nextPair.getLastTile();
                Coord possibleTile = nextPair.getCurrentTile();
                if (!world.containsKey(possibleTile)) {
                    if (!resultTiles.contains(previousTile)) {
                        result.add(new Move(previousTile, finalDistance));
                        resultTiles.add(previousTile);
                        turnsWithoutFind = 0;
                    }
                } else {
                    nextToCheck.add(new MoveHistory(previousTile, possibleTile));
                }
            }

            seen.addAll(toCheck.stream()
                    .map(MoveHistory::getCurrentTile)
                    .collect(Collectors.toSet())
            );
            toCheck = nextToCheck;
        }
        return result;
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

    /**
     * @param position Position to check surrounding tiles
     * @param checked Coords that have already been considered
     * @return Unconsidered neighbour tiles
     */
    private static List<Coord> getUnvisited(Coord position, Set<Coord> checked) {
        List<Coord> result = new LinkedList<>();
        int px = position.getX();
        int py = position.getY();

        List<Coord> options = Arrays.asList(
                new Coord(1, 0),
                new Coord(-1, 0),
                new Coord(0, 1),
                new Coord(0, -1)
        );
        options.forEach(option -> {
            int dx = option.getX();
            int dy = option.getY();
            Coord newPos = new Coord(px + dx, py + dy);
            if (!checked.contains(newPos)) {
                checked.add(newPos);
                result.add(newPos);
            }
        });
        return result;
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
        return Objects.hash(color);
    }
}
