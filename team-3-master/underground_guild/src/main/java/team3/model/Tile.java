package team3.model;

import java.io.Serializable;

/**
 * Represents a single tile on the game map grid.
 */
public class Tile implements Serializable {
    public enum TileType {
        ROCK,
        WATER,
        MOUNTAIN,
        FOREST
    }

    private final int x;
    private final int y;
    private String type;

    private float speedModifier;
    private float maintenanceCostModifier;

    private TransportUnit occupiedBy;
    private boolean hasTunnel;
    private Hub occupant;

    public Tile(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.speedModifier = 1.0f;
        this.maintenanceCostModifier = 1.0f;
        this.hasTunnel = "tunnel".equalsIgnoreCase(type);
    }

    public Tile(int x, int y, String type, float speedModifier, float maintenanceCostModifier) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.speedModifier = speedModifier;
        this.maintenanceCostModifier = maintenanceCostModifier;
        this.hasTunnel = "tunnel".equalsIgnoreCase(type);
    }

    public Tile(int x, int y, TileType type) {
        this(x, y, toLegacyType(type));
    }

    private static String toLegacyType(TileType type) {
        return switch (type) {
            case ROCK -> "rock";
            case WATER -> "water";
            case MOUNTAIN -> "mountain";
            case FOREST -> "forest";
        };
    }

    public static TileType toTileType(String type) {
        if (type == null) {
            return TileType.ROCK;
        }
        return switch (type.toLowerCase()) {
            case "water" -> TileType.WATER;
            case "mountain" -> TileType.MOUNTAIN;
            case "forest" -> TileType.FOREST;
            default -> TileType.ROCK;
        };
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getType() {
        return type;
    }

    public TileType getTileType() {
        return toTileType(type);
    }

    public void setType(String type) {
        this.type = type;
        if ("tunnel".equalsIgnoreCase(type)) {
            this.hasTunnel = true;
        }
    }

    public void setType(TileType type) {
        this.type = toLegacyType(type);
    }

    public boolean isOccupied() {
        return occupiedBy != null;
    }

    public TransportUnit getOccupiedBy() {
        return occupiedBy;
    }

    public void setOccupiedBy(TransportUnit unit) {
        this.occupiedBy = unit;
    }

    public float getSpeedModifier() {
        return speedModifier;
    }

    public void setSpeedModifier(float speedModifier) {
        this.speedModifier = speedModifier;
    }

    public float getMaintenanceCostModifier() {
        return maintenanceCostModifier;
    }

    public void setMaintenanceCostModifier(float maintenanceCostModifier) {
        this.maintenanceCostModifier = maintenanceCostModifier;
    }

    public boolean isHasTunnel() {
        return hasTunnel;
    }

    public void setHasTunnel(boolean val) {
        this.hasTunnel = val;
        if (val) {
            this.type = "tunnel";
        }
    }

    public Hub getOccupant() {
        return occupant;
    }

    public void setOccupant(Hub occupant) {
        this.occupant = occupant;
    }

    public boolean canBuildTunnel() {
        return getTileType() == TileType.ROCK && !hasTunnel && occupant == null;
    }
}
