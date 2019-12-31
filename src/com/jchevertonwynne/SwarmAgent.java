package com.jchevertonwynne;

import com.jchevertonwynne.structures.AStarOption;
import com.jchevertonwynne.structures.Coord;
import com.jchevertonwynne.structures.Move;
import com.jchevertonwynne.structures.MoveHistory;

import java.awt.Color;
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
import static com.jchevertonwynne.structures.CircleOperations.angleBetween;
import static com.jchevertonwynne.structures.CircleOperations.calcCircle;
import static com.jchevertonwynne.structures.CircleOperations.mostSimilarAngle;
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

    public SwarmAgent(Coord position, Color color, Scanner scanner) {
        this.position = position;
        this.color = color;
        this.scanner = scanner;
        world = new HashMap<>();
        world.put(position, true);
        currentPath = new LinkedList<>();
        scansDone = 0;
    }

    public SwarmAgent(Coord position, Color color, Map<Coord, Boolean> world, List<Coord> currentPath, Scanner scanner, int scansDone) {
        this.position = position;
        this.color = color;
        this.world = world;
        this.currentPath = currentPath;
        this.scanner = scanner;
        this.scansDone = scansDone;
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

    public void setColor(Color color) {
        this.color = color;
    }

    public Map<Coord, Boolean> getWorld() {
        return world;
    }

    public void setWorld(Map<Coord, Boolean> world) {
        this.world = world;
    }

    public List<Coord> getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(List<Coord> currentPath) {
        this.currentPath = currentPath;
    }

    public int getScansDone() {
        return scansDone;
    }

    public void setScansDone(int scansDone) {
        this.scansDone = scansDone;
    }

    public SwarmAgent copy() {
        return new SwarmAgent(
                position,
                color,
                new HashMap<>(world),
                new LinkedList<>(currentPath),
                scanner,
                scansDone
        );
    }

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
            result.setCurrentPath(calculatePath(chosen));
            if (result.getCurrentPath() == null) {
                result.setCurrentPath(result.calculatePath(new Coord(780, 780)));
            }
        }
        return result;
    }

    public double evaluateGoodness(Move move) {
        if (move.getDistance() <= 1) {
            return 1000;
        }
        return getPotentialNewVisible(move.getTile()).size() / Math.log(move.getDistance());
    }

    public List<Move> findAvailable() {
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

    public Set<Coord> getPotentialNewVisible(Coord coord) {
        Set<Coord> result = new HashSet<>();
        calcAllVisible(coord, SIGHT_RADIUS).forEach(tile -> {
            if (!world.containsKey(tile)) {
                result.add(tile);
            }
        });
        return result;
    }

    public List<Coord> calcAllVisible(Coord centre, int n) {
        Set<Coord> circleEdges = calcCircle(centre, n);
        int sx = centre.getX();
        int sy = centre.getY();
        HashSet<Coord> allVisible = new HashSet<>();

        circleEdges.forEach(edge -> {
            int ex = edge.getX();
            int ey = edge.getY();

            double targetAngle = angleBetween(centre, edge);

            int dx = Integer.compare(ex - sx, 0);
            int dy = Integer.compare(ey - sy, 0);

            Coord current = centre;
            while (current.equals(new Coord(ex, ey)) && world.getOrDefault(current, false)) {
                int cx = current.getX();
                int cy = current.getY();
                Coord a = dx != 0 ? new Coord(cx + dx, cy) : null;
                Coord b = dy != 0 ? new Coord(cx, cy + dy) : null;
                current = mostSimilarAngle(a, b, edge, targetAngle);
                allVisible.add(current);
            }
        });
        return new LinkedList<>(allVisible);
    }

    public void scanArea() {
        scansDone++;
        scanner.scan(this);
    }

    public List<Coord> calculatePath(Coord destination) {
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

    public List<AStarOption> evaluateChoices(AStarOption aStarOption, Coord goal) {
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

    public static double distance(Coord a, Coord b) {
        return pow(pow(abs(a.getX() - b.getX()), 2) + pow(abs(a.getY() - b.getY()), 2), 0.5);
    }

    public static Stream<Coord> getUnvisited(Coord position, Set<Coord> checked) {
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
