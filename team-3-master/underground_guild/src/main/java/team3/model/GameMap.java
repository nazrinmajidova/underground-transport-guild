package team3.model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.HashMap;
import java.util.function.Predicate;

/**
 * Represents the physical world grid.
 * Manages tiles and basic spatial access for the simulation.
 */
public class GameMap implements Serializable {
    public enum TunnelBuildStatus {
        OK,
        OUT_OF_BOUNDS,
        BLOCKED_BY_HUB,
        ALREADY_TUNNELED,
        INVALID_TERRAIN
    }

    public static final GenerationConfig DEFAULT_GENERATION_CONFIG =
        new GenerationConfig(3, 5, 2, 3, true);

    private final Tile[][] tiles;
    private final int width;
    private final int height;
    private final List<Hub> hubs;
    private final GenerationConfig generationConfig;
    private final Random random;

    /**
     * Configures the number of generated hubs and starter tunnels.
     */
    public record GenerationConfig(
        int minSettlements,
        int maxSettlements,
        int minFacilitiesPerType,
        int maxFacilitiesPerType,
        boolean includeStarterTunnels
    ) implements Serializable {}

    /**
     * Creates a map grid of specified dimensions.
     */
    public GameMap(int width, int height) {
        this(width, height, DEFAULT_GENERATION_CONFIG);
    }

    public GameMap(int width, int height, GenerationConfig generationConfig) {
        this(width, height, generationConfig, new Random());
    }

    public GameMap(int width, int height, GenerationConfig generationConfig, Random random) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];
        this.hubs = new ArrayList<>();
        this.generationConfig = validateGenerationConfig(generationConfig);
        this.random = random;
        generate();
    }

    /**
     * Procedurally generates the initial terrain layout.
     * Places settlements and facilities before hazards and optional starter tunnels.
     */
    protected void generate() {
        hubs.clear();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new Tile(x, y, Tile.TileType.ROCK);
            }
        }
        placeSettlements();
        placeIndustrialFacilities("Mine", "Ore", 1.0);
        placeIndustrialFacilities("Farm", "Food", 1.0);
        placeIndustrialFacilities("Forge", "Tools", 0.8);

        placeEnvironmentalHazards();
        if (generationConfig.includeStarterTunnels()) {
            placeStarterTunnels();
        }
    }

    /**
     * Randomly assigns environmental hazards (speed reduction and maintenance increase) 
     * to a portion of the ROCK tiles.
     */
    private void placeEnvironmentalHazards() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile tile = tiles[x][y];
                if (tile.getTileType() == Tile.TileType.ROCK && tile.getOccupant() == null) {
                    if (random.nextDouble() < 0.015) { 
                        tile.setSpeedModifier(0.6f); 
                        tile.setMaintenanceCostModifier(1.5f); 
                    }
                }
            }
        }
    }

    /**
     * Retrieves the tile at a specific coordinate.
     */
    public Tile getTileAt(int x, int y) {
        return isInBounds(x, y) ? tiles[x][y] : null;
    }

    /**
     * Checks whether the given coordinates are inside the map.
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Builds a tunnel at the requested position when the tile is valid.
     */
    public boolean buildTunnel(int x, int y) {
        if (getTunnelBuildStatus(x, y) != TunnelBuildStatus.OK) {
            return false;
        }

        Tile tile = getTileAt(x, y);
        tile.setHasTunnel(true);
        return true;
    }

    public TunnelBuildStatus getTunnelBuildStatus(int x, int y) {
        Tile tile = getTileAt(x, y);
        if (tile == null) {
            return TunnelBuildStatus.OUT_OF_BOUNDS;
        }
        if (tile.getOccupant() != null) {
            return TunnelBuildStatus.BLOCKED_BY_HUB;
        }
        if (tile.isHasTunnel()) {
            return TunnelBuildStatus.ALREADY_TUNNELED;
        }
        if (tile.getTileType() != Tile.TileType.ROCK) {
            return TunnelBuildStatus.INVALID_TERRAIN;
        }
        return TunnelBuildStatus.OK;
    }

    /**
     * Registers a hub on the map and marks its anchor tile as occupied.
     */
    public void addHub(Hub hub) {
        if (hub == null || hub.getTile() == null) {
            return;
        }

        if (!hubs.contains(hub)) {
            hubs.add(hub);
        }
        occupyRectangle(hub, hub.getTile().getX(), hub.getTile().getY(), hub.getFootprintWidth(), hub.getFootprintHeight());
    }

    /**
     * Unregisters a hub and clears its occupied footprint from the map.
     */
    public void removeHub(Hub hub) {
        if (hub == null || hub.getTile() == null) {
            return;
        }
        hubs.remove(hub);
        clearOccupiedRectangle(hub.getTile().getX(), hub.getTile().getY(), hub.getFootprintWidth(), hub.getFootprintHeight());
    }

    public boolean canPlaceHubAt(int startX, int startY, int footprintWidth, int footprintHeight) {
        if (!canPlaceRectangle(startX, startY, footprintWidth, footprintHeight)) {
            return false;
        }

        for (int x = startX; x < startX + footprintWidth; x++) {
            for (int y = startY; y < startY + footprintHeight; y++) {
                Tile tile = getTileAt(x, y);
                if (tile == null || tile.getTileType() != Tile.TileType.ROCK || tile.isHasTunnel()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks whether two hubs are connected through adjacent tunnel tiles.
     */
    public boolean isConnected(Hub hubA, Hub hubB) {
        return !findTunnelPath(hubA, hubB).isEmpty();
    }

    /**
     * Finds a connected tunnel path between two hubs.
     */
    public List<Tile> findTunnelPath(Hub hubA, Hub hubB) {
        return findPathBetweenHubs(hubA, hubB, Tile::isHasTunnel);
    }

    /**
     * Finds a rock-and-tunnel corridor that can be converted into a valid route path.
     */
    public List<Tile> findBuildableTunnelPath(Hub hubA, Hub hubB) {
        return findPathBetweenHubs(
            hubA,
            hubB,
            tile -> tile.getOccupant() == null && (tile.isHasTunnel() || tile.getTileType() == Tile.TileType.ROCK)
        );
    }

    /**
     * Helper method to find if a hub occupies a specific tile.
     */
    public Hub getHubAt(Tile tile) {
        if (tile == null) {
            return null;
        }
        for (Hub hub : hubs) {
            if (hub.getTiles().contains(tile)) {
                return hub;
            }
        }
        return null;
    }

    /**
     * Finds an adjacent non-depot hub around the given tile.
     */
    public Hub findAdjacentHub(Tile tile) {
        if (tile == null) {
            return null;
        }

        int x = tile.getX();
        int y = tile.getY();
        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] offset : offsets) {
            Tile neighbor = getTileAt(x + offset[0], y + offset[1]);
            if (neighbor != null) {
                Hub neighborHub = getHubAt(neighbor);
                if (neighborHub != null && !(neighborHub instanceof Depot)) {
                    return neighborHub;
                }
            }
        }

        return null;
    }

    /**
     * Uses multi-source breadth-first search between accessible hub boundary tiles.
     * Hub footprints are excluded; orthogonal steps have equal cost, so the first
     * reached target gives a shortest path in tile count. No connection returns an empty list.
     */
    private List<Tile> findPathBetweenHubs(Hub hubA, Hub hubB, Predicate<Tile> traversable) {
        if (hubA == null || hubB == null || traversable == null) {
            return List.of();
        }

        if (Objects.equals(hubA, hubB)) {
            return List.of();
        }

        Set<Tile> startTiles = getAccessibleBoundaryTiles(hubA, traversable);
        Set<Tile> targetTiles = getAccessibleBoundaryTiles(hubB, traversable);
        if (startTiles.isEmpty() || targetTiles.isEmpty()) {
            return List.of();
        }

        Queue<Tile> frontier = new ArrayDeque<>(startTiles);
        Set<Tile> visited = new HashSet<>(startTiles);
        Map<Tile, Tile> previous = new HashMap<>();

        while (!frontier.isEmpty()) {
            Tile current = frontier.poll();
            if (targetTiles.contains(current)) {
                return reconstructPath(previous, current);
            }

            for (Tile neighbor : getOrthogonalNeighbors(current)) {
                if (traversable.test(neighbor) && visited.add(neighbor)) {
                    previous.put(neighbor, current);
                    frontier.add(neighbor);
                }
            }
        }

        return List.of();
    }

    private List<Tile> reconstructPath(Map<Tile, Tile> previous, Tile targetTile) {
        List<Tile> path = new ArrayList<>();
        Tile current = targetTile;
        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }
        return path;
    }

    private Set<Tile> getAccessibleBoundaryTiles(Hub hub, Predicate<Tile> traversable) {
        Set<Tile> boundaryTiles = new LinkedHashSet<>();
        int startX = hub.getTile().getX();
        int startY = hub.getTile().getY();
        int endX = startX + hub.getFootprintWidth() - 1;
        int endY = startY + hub.getFootprintHeight() - 1;

        for (int x = startX; x <= endX; x++) {
            addBoundaryTileIfAccessible(boundaryTiles, x, startY - 1, traversable);
            addBoundaryTileIfAccessible(boundaryTiles, x, endY + 1, traversable);
        }
        for (int y = startY; y <= endY; y++) {
            addBoundaryTileIfAccessible(boundaryTiles, startX - 1, y, traversable);
            addBoundaryTileIfAccessible(boundaryTiles, endX + 1, y, traversable);
        }
        return boundaryTiles;
    }

    private void addBoundaryTileIfAccessible(Set<Tile> boundaryTiles, int x, int y, Predicate<Tile> traversable) {
        Tile candidate = getTileAt(x, y);
        if (candidate != null && traversable.test(candidate)) {
            boundaryTiles.add(candidate);
        }
    }

    private List<Tile> getOrthogonalNeighbors(Tile tile) {
        List<Tile> neighbors = new ArrayList<>(4);
        addNeighborIfPresent(neighbors, tile.getX() + 1, tile.getY());
        addNeighborIfPresent(neighbors, tile.getX() - 1, tile.getY());
        addNeighborIfPresent(neighbors, tile.getX(), tile.getY() + 1);
        addNeighborIfPresent(neighbors, tile.getX(), tile.getY() - 1);
        return neighbors;
    }

    private void addNeighborIfPresent(List<Tile> neighbors, int x, int y) {
        Tile candidate = getTileAt(x, y);
        if (candidate != null) {
            neighbors.add(candidate);
        }
    }

    private void placeSettlements() {
        int settlementCount = randomCount(
            generationConfig.minSettlements(),
            generationConfig.maxSettlements()
        );

        for (int index = 0; index < settlementCount; index++) {
            Tile anchor = findAnchorForFootprint(3, 3);
            if (anchor == null) {
                break;
            }

            Settlement settlement = new Settlement(
                "settlement-" + (index + 1),
                "Settlement " + (index + 1),
                collectFootprintTiles(anchor, 3, 3),
                100 + random.nextInt(401)
            );
            addHub(settlement);
        }
    }

    private void placeIndustrialFacilities(String facilityName, String producedGood, double productionRate) {
        int facilityCount = randomCount(
            generationConfig.minFacilitiesPerType(),
            generationConfig.maxFacilitiesPerType()
        );

        for (int index = 0; index < facilityCount; index++) {
            Tile anchor = findAnchorForFootprint(2, 2);
            if (anchor == null) {
                break;
            }

            List<Tile> facilityTiles = collectFootprintTiles(anchor, 2, 2);
            IndustrialFacility facility;
            String id = facilityName.toLowerCase() + "-" + (index + 1);
            String name = facilityName + " " + (index + 1);

            switch (facilityName.toLowerCase()) {
                case "mine" -> facility = new Mine(id, name, facilityTiles, (float) productionRate);
                case "farm" -> facility = new Farm(id, name, facilityTiles, (float) productionRate);
                case "forge" -> facility = new Forge(id, name, facilityTiles, (float) productionRate, 250f);
                default -> throw new IllegalArgumentException("Unsupported facility type: " + facilityName);
            }
            addHub(facility);
        }
    }

    private int randomCount(int minInclusive, int maxInclusive) {
        if (maxInclusive <= minInclusive) {
            return minInclusive;
        }
        return minInclusive + random.nextInt(maxInclusive - minInclusive + 1);
    }

    private Tile findAnchorForFootprint(int footprintWidth, int footprintHeight) {
        for (int attempt = 0; attempt < 200; attempt++) {
            int x = random.nextInt(Math.max(1, width - footprintWidth + 1));
            int y = random.nextInt(Math.max(1, height - footprintHeight + 1));
            if (canPlaceRectangle(x, y, footprintWidth, footprintHeight)) {
                return getTileAt(x, y);
            }
        }
        return null;
    }

    private List<Tile> collectFootprintTiles(Tile anchor, int footprintWidth, int footprintHeight) {
        List<Tile> footprintTiles = new ArrayList<>();
        int startX = anchor.getX();
        int startY = anchor.getY();
        for (int x = startX; x < startX + footprintWidth; x++) {
            for (int y = startY; y < startY + footprintHeight; y++) {
                Tile tile = getTileAt(x, y);
                if (tile != null) {
                    footprintTiles.add(tile);
                }
            }
        }
        return footprintTiles;
    }

    private boolean canPlaceRectangle(int startX, int startY, int footprintWidth, int footprintHeight) {
        for (int x = startX; x < startX + footprintWidth; x++) {
            for (int y = startY; y < startY + footprintHeight; y++) {
                Tile tile = getTileAt(x, y);
                if (tile == null || tile.getOccupant() != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private void occupyRectangle(Hub hub, int startX, int startY, int footprintWidth, int footprintHeight) {
        for (int x = startX; x < startX + footprintWidth; x++) {
            for (int y = startY; y < startY + footprintHeight; y++) {
                Tile tile = getTileAt(x, y);
                if (tile != null) {
                    tile.setOccupant(hub);
                }
            }
        }
    }

    private void clearOccupiedRectangle(int startX, int startY, int footprintWidth, int footprintHeight) {
        for (int x = startX; x < startX + footprintWidth; x++) {
            for (int y = startY; y < startY + footprintHeight; y++) {
                Tile tile = getTileAt(x, y);
                if (tile != null) {
                    tile.setOccupant(null);
                }
            }
        }
    }

    private void placeStarterTunnels() {
        for (Hub hub : hubs) {
            int startX = hub.getTile().getX();
            int startY = hub.getTile().getY();
            int endX = startX + hub.getFootprintWidth() - 1;
            int endY = startY + hub.getFootprintHeight() - 1;

            if (tryBuildStarterTunnel(endX + 1, startY)) {
                continue;
            }
            if (tryBuildStarterTunnel(startX - 1, startY)) {
                continue;
            }
            if (tryBuildStarterTunnel(startX, endY + 1)) {
                continue;
            }
            tryBuildStarterTunnel(startX, startY - 1);
        }
    }

    private boolean tryBuildStarterTunnel(int x, int y) {
        Tile tile = getTileAt(x, y);
        if (tile != null && tile.canBuildTunnel()) {
            tile.setHasTunnel(true);
            return true;
        }
        return false;
    }

    private GenerationConfig validateGenerationConfig(GenerationConfig config) {
        Objects.requireNonNull(config, "generationConfig");
        if (config.minSettlements() < 0
            || config.maxSettlements() < config.minSettlements()
            || config.minFacilitiesPerType() < 0
            || config.maxFacilitiesPerType() < config.minFacilitiesPerType()) {
            throw new IllegalArgumentException("Invalid generation settings");
        }
        return config;
    }


    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public List<Hub> getHubs() { return hubs; }
    public GenerationConfig getGenerationConfig() { return generationConfig; }
    protected Random getRandom() { return random; }
}
