package com.jchevertonwynne;

import com.jchevertonwynne.structures.AStarOption;
import com.jchevertonwynne.structures.CircleOperations;
import com.jchevertonwynne.structures.Coord;
import com.jchevertonwynne.structures.Move;
import com.jchevertonwynne.structures.MoveHistory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jchevertonwynne.structures.AStarOption.aStarOptionComparator;
import static com.jchevertonwynne.structures.Common.SIGHT_RADIUS;
import static java.lang.Math.abs;
import static java.lang.StrictMath.pow;

public class SwarmAgent {
    private Coord position;
    private Color color;
    private Map<Coord, Boolean> world;
    private List<Coord> currentPath;
    private Scanner scanner;
    private int scansDone;
    private List<Coord> newlyDone;
    private Coord lastScanLocation;
    private Coord recentScanLocation;

    public SwarmAgent(Coord position, Color color, Scanner scanner) {
        this.position = position;
        this.color = color;
        this.scanner = scanner;
        world = new HashMap<>();
        world.put(position, true);
        currentPath = new LinkedList<>();
        scansDone = 0;
        newlyDone = new LinkedList<>();
        lastScanLocation = position;
        recentScanLocation = position;
    }

    public SwarmAgent(
            Coord position,
            Color color,
            Map<Coord, Boolean> world,
            List<Coord> currentPath,
            Scanner scanner,
            int scansDone,
            List<Coord> newlyDone,
            Coord lastScanLocation,
            Coord recentScanLocation
    ) {
        this.position = position;
        this.color = color;
        this.world = world;
        this.currentPath = currentPath;
        this.scanner = scanner;
        this.scansDone = scansDone;
        this.newlyDone = newlyDone;
        this.lastScanLocation = lastScanLocation;
        this.recentScanLocation = recentScanLocation;
    }

    public Coord getPosition() {
        return position;
    }

    public void setPosition(Coord position) {
        this.position = position;
    }

    public Color getColor() {
        return color;
    }

    public Map<Coord, Boolean> getWorld() {
        return world;
    }

    public List<Coord> getCurrentPath() {
        return currentPath;
    }

    private void setCurrentPath(List<Coord> currentPath) {
        this.currentPath = currentPath;
    }

    public int getScansDone() {
        return scansDone;
    }

    public Coord getLastScanLocation() {
        return lastScanLocation;
    }

    public SwarmAgent copy() {
        return new SwarmAgent(
                position,
                color,
                new HashMap<>(world),
                new LinkedList<>(currentPath),
                scanner,
                scansDone,
                newlyDone,
                lastScanLocation,
                recentScanLocation
        );
    }

    /**
     * Advance an agent to its next state and return a new instance
     * @return SwarmAgent
     */
    public SwarmAgent nextMove() {
        SwarmAgent result = copy();
        if (result.getCurrentPath().size() > 0) {
            result.setPosition(result.getCurrentPath().remove(0));
        }
        else {
            result.scanArea();
            List<Move> availableTiles = result.findAvailable();
            availableTiles.sort(Comparator.comparingDouble(this::evaluateGoodness));
            Coord chosen = availableTiles.get(availableTiles.size() - 1).getTile();
            result.setCurrentPath(result.calculatePath(chosen));
            if (result.getCurrentPath() == null) {
                System.out.println("going back to start...");
                result.setCurrentPath(result.calculatePath(new Coord(780, 780)));
            }
            System.out.printf("new path of length %d\n", result.getCurrentPath().size());
        }
        return result;
    }

    /**
     * Measure of how 'good' a potential move is for ranking purposes
     * @param move Distance-Coord pair
     * @return double Arbitrary score number of goodness
     */
    private double evaluateGoodness(Move move) {
        if (move.getDistance() <= 1) {
            return 1000;
        }
        return getPotentialNewVisible(move.getTile()) / pow(move.getDistance(), 2);
    }

    /**
     * Calculate all edge tiles on current agent's world knowledge
     * @return List of first <= ~200 boundary tiles
     */
    private List<Move> findAvailable() {
        final int MAX_RESULTS = 200;
        List<MoveHistory> toCheck = new LinkedList<>();
        toCheck.add(new MoveHistory(null, position));

        Set<Coord> seen = new HashSet<>();
        seen.add(position);

        int distance = 0;
        List<Move> result = new LinkedList<>();
        Set<Coord> resultTiles = new HashSet<>();

        while (!toCheck.isEmpty() && result.size() < MAX_RESULTS) {
            distance++;
            List<MoveHistory> nextAvailable = new LinkedList<>();
            toCheck.forEach(move -> {
                Coord pos = move.getCurrentTile();
                if (world.getOrDefault(pos, false)) {
                    nextAvailable.addAll(
                            getUnvisited(pos, seen).map(newPos -> new MoveHistory(pos, newPos)).collect(Collectors.toList())
                    );
                }
            });
            List<MoveHistory> nextToCheck = new LinkedList<>();
            int finalDistance = distance;
            nextAvailable.forEach(nextPair -> {
                Coord previousTile = nextPair.getLastTile();
                Coord possibleTile = nextPair.getCurrentTile();
                if (!world.containsKey(possibleTile)) {
                    if (!resultTiles.contains(previousTile)) {
                        result.add(new Move(previousTile, finalDistance));
                        resultTiles.add(previousTile);
                    }
                }
                else {
                    nextToCheck.add(new MoveHistory(previousTile, possibleTile));
                }
            });
            seen.addAll(toCheck.stream().map(MoveHistory::getCurrentTile).collect(Collectors.toSet()));
            toCheck = nextToCheck;
        }
        return result;
    }

    /**
     * Calculate how many new tiles may be discovered from new position
     * @param coord Position to look for new
     * @return
     */
    private int getPotentialNewVisible(Coord coord) {
        int result = 0;
        for (Coord tile : calcAllPotentiallyVisible(coord, SIGHT_RADIUS)) {
            if (!world.containsKey(tile)) {
                result++;
            }
        }
        return result;
    }


    /**
     * Perform a maximal estimate scan, stopping at walls already discovered
     * @param centre Centre to discover around
     * @param n Radius to 'scan' in
     * @return All real and potential tiles
     */
    private List<Coord> calcAllPotentiallyVisible(Coord centre, int n) {
        List<List<Coord>> rays = CircleOperations.getCircleRays(centre, n);
        Set<Coord> result = new HashSet<>();

        for (List<Coord> ray : rays) {
            for (Coord coord : ray) {
                if (!world.getOrDefault(coord, true)) {
                    break;
                }
                result.add(coord);
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * Scan area, increase scan counter and store previous positions
     */
    public void scanArea() {
        scansDone++;
        scanner.scan(this);
        lastScanLocation = recentScanLocation;
        recentScanLocation = position;
    }

    /**
     * A* to path from current position to destination
     * @param destination Goal coordinate
     * @return Path to destination, or null
     */
    private List<Coord> calculatePath(Coord destination) {
        Set<Coord> seen = new HashSet<>();
        seen.add(position);

        PriorityQueue<AStarOption> toTry = new PriorityQueue<>(aStarOptionComparator);
        toTry.add(new AStarOption(distance(position, destination), 0, position, new LinkedList<>()));

        while (!toTry.isEmpty()) {
            AStarOption nextOption = toTry.remove();
            List<AStarOption> nextOptions = evaluateChoices(nextOption, destination);
            for (AStarOption aStarOption : nextOptions) {
                Coord tile = aStarOption.getTile();
                if (tile.equals(destination)) {
                    return aStarOption.getHistory();
                }
            }
            nextOptions.forEach(option -> {
                Coord tile = option.getTile();
                if (!seen.contains(tile)) {
                    toTry.add(option);
                    seen.add(tile);
                }
            });
        }
        return null;
    }

    /**
     * From a tile, find all not checked neighbour tiles
     * @param aStarOption Current A* state
     * @param goal Destination
     * @return Unchecked neighbour A* states
     */
    private List<AStarOption> evaluateChoices(AStarOption aStarOption, Coord goal) {
        List<AStarOption> result = new LinkedList<>();
        int currDist = aStarOption.getActualDistance();
        Coord currTile = aStarOption.getTile();
        int cx = currTile.getX();
        int cy = currTile.getY();
        List<Coord> options = Arrays.asList(
                new Coord(1, 0),
                new Coord(-1, 0),
                new Coord(0, 1),
                new Coord(0, -1)
        );
        options.forEach(option -> {
            int dx = option.getX();
            int dy = option.getY();
            Coord nextCoord = new Coord(cx + dx, cy + dy);
            if (world.getOrDefault(nextCoord, false)) {
                LinkedList<Coord> newHistory = new LinkedList<>(aStarOption.getHistory());
                newHistory.add(nextCoord);
                result.add(new AStarOption(distance(nextCoord, goal), currDist + 1, nextCoord, newHistory));
            }
        });
        return result;
    }

    /**
     * Distance underestimate for A*
     * @param a Coordinate
     * @param b Coordinate
     * @return Euclidian distance between coordinates
     */
    private static double distance(Coord a, Coord b) {
        return pow(pow(abs(a.getX() - b.getX()), 2) + pow(abs(a.getY() - b.getY()), 2), 0.5);
    }

    /**
     * @param position
     * @param checked
     * @return
     */
    private static Stream<Coord> getUnvisited(Coord position, Set<Coord> checked) {
        LinkedList<Coord> result = new LinkedList<>();
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
        return result.stream();
    }

    public void noteNewlyDone(Coord coord) {
        newlyDone.add(coord);
    }

    /**
     * Keeps record of newly discoved tiles so that drawing to screen can be more efficient
     * @return Tiles discovered since last scan
     */
    public List<Coord> getNewlyDone() {
        List<Coord> result = newlyDone;
        newlyDone = new LinkedList<>();
        return result;
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
