package team3.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base for all location nodes on the map (settlements, depots, facilities).
 * A hub can occupy one or more tiles and may manage associated depots.
 */
public abstract class Hub implements Serializable {
    private final String id;
    private String name;
    private final List<Tile> tiles = new ArrayList<>();
    private final List<Depot> depots = new ArrayList<>();
    private final List<Route> connectedRoutes = new ArrayList<>();

    protected Hub(String id, String name, Tile tile) {
        this(id, name, tile != null ? List.of(tile) : List.of());
    }

    protected Hub(String id, String name, List<Tile> tiles) {
        this.id = id;
        this.name = name;

        if (tiles != null) {
            for (Tile tile : tiles) {
                if (tile != null) {
                    this.tiles.add(tile);
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Tile getTile() {
        return tiles.isEmpty() ? null : tiles.get(0);
    }

    public List<Tile> getTiles() {
        return Collections.unmodifiableList(tiles);
    }

    public List<Depot> getDepots() {
        return Collections.unmodifiableList(depots);
    }

    public void addDepot(Depot depot) {
        if (depot != null && !depots.contains(depot)) {
            depots.add(depot);
        }
    }

    public void removeDepot(Depot depot) {
        depots.remove(depot);
    }

    public List<Route> getConnectedRoutes() {
        return connectedRoutes;
    }

    public void addRoute(Route route) {
        connectedRoutes.add(route);
    }

    public void removeRoute(Route route) {
        connectedRoutes.remove(route);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTile(Tile tile) {
        tiles.clear();
        if (tile != null) {
            tiles.add(tile);
        }
    }

    public int getFootprintWidth() {
        if (tiles.isEmpty()) {
            return 1;
        }
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (Tile tile : tiles) {
            minX = Math.min(minX, tile.getX());
            maxX = Math.max(maxX, tile.getX());
        }
        return (maxX - minX) + 1;
    }

    public int getFootprintHeight() {
        if (tiles.isEmpty()) {
            return 1;
        }
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Tile tile : tiles) {
            minY = Math.min(minY, tile.getY());
            maxY = Math.max(maxY, tile.getY());
        }
        return (maxY - minY) + 1;
    }
}
