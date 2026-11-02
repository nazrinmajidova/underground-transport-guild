package team3.model;

import java.io.Serializable;
import team3.service.SimulationClock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The central class of the simulation. It coordinates the map, economy, 
 * and simulation clock, and manages the main update loop.
 */

public class Game implements Serializable {
    private final GameMap gameMap;
    private final Economy economy;
    private final SimulationClock clock;
    private final Random demandRandom;
    private final List<TransportUnit> units;
    private final List<Route> routes;
    private final Map<String, MovementState> movementStates;
    private transient Runnable onGameOver;
    private String gameState;

    /**
     * Initializes a new Game session with default map size and starting capital.
     */
    public Game() {
        this.gameMap = new GameMap(50, 50); 
        this.economy = new Economy(10000.0); 
        this.clock = new SimulationClock();
        this.demandRandom = new Random();
        this.units = new ArrayList<>();
        this.routes = new ArrayList<>();
        this.movementStates = new HashMap<>();
        this.gameState = "RUNNING";
    }

    public void handleGameOver() {
        if (onGameOver != null) {
            javafx.application.Platform.runLater(onGameOver);
        }
    }

    public GameMap getGameMap() { return gameMap; }
    public Economy getEconomy() { return economy; }
    public SimulationClock getClock() { return clock; }
    public List<TransportUnit> getUnits() {return units; }
    public List<Route> getRoutes() { return routes; }
    public Random getRandom() { return demandRandom; }
    public List<TransportUnit> getTransportUnits() { return units; }
    public String getGameState() { return gameState; }
    public void setGameState(String gameState) { this.gameState = gameState; }

    public void moveFacilityOutputToAttachedDepots(IndustrialFacility facility, Good produced) {
        if (facility == null || produced == null) {
            return;
        }

        List<Depot> attachedDepots = facility.getDepots();
        if (attachedDepots.isEmpty()) {
            return;
        }

        Good extracted = facility.loadGoods(produced.getAmount());
        if (extracted == null || extracted.getAmount() <= 0f) {
            return;
        }

        float totalAmount = extracted.getAmount();
        int depotCount = attachedDepots.size();
        float remaining = totalAmount;

        for (int index = 0; index < depotCount; index++) {
            Depot depot = attachedDepots.get(index);
            float share = index == depotCount - 1
                ? remaining
                : totalAmount / depotCount;
            if (share <= 0f) {
                continue;
            }

            depot.addGoods(List.of(new Good(extracted.getType(), share)));
            remaining -= share;
        }
    }
   
    /**
     * Moves units along route tunnel tiles while enforcing single-unit tile occupancy.
     */
    public void updateUnitOccupancyAndMovement() {
        List<TransportUnit> activeUnits = new ArrayList<>();
        for (TransportUnit unit : units) {
            if (unit == null) {
                continue;
            }

            Route route = unit.getAssignedRoute();
            if (route == null || route.getDepots().size() < 2) {
                releaseTileOccupancy(unit);
                movementStates.remove(unit.getId());
                continue;
            }

            Depot currentStop = unit.getCurrentStop();
            Depot nextStop = unit.getNextStop();
            if (currentStop == null || nextStop == null) {
                continue;
            }

            List<Tile> path = gameMap.findTunnelPath(currentStop, nextStop);
            if (path.isEmpty()) {
                continue;
            }

            MovementState state = movementStates.computeIfAbsent(unit.getId(), ignored -> new MovementState());
            state.segmentPath = path;
            state.segmentStartStopIndex = unit.getCurrentStopIndex();

            if (!claimInitialTile(unit, state.segmentPath)) {
                continue;
            }
            activeUnits.add(unit);
        }

        for (TransportUnit unit : activeUnits) {
            MovementState state = movementStates.get(unit.getId());
            if (state == null) {
                continue;
            }

            List<Tile> activePath = state.segmentPath;
            if (activePath.isEmpty()) {
                continue;
            }

            int currentIndex = indexOfTile(activePath, unit.getCurrenTile());
            if (currentIndex < 0) {
                unit.setCurrentTile(activePath.getFirst());
                currentIndex = 0;
            }

            if (currentIndex >= activePath.size() - 1) {
                Depot arrivedDepot = unit.getNextStop();
                if (arrivedDepot != null && arrivedDepot.getTile() != null) {
                    Tile currentTile = unit.getCurrenTile();
                    if (currentTile != null && currentTile.getOccupiedBy() == unit) {
                        currentTile.setOccupiedBy(null);
                    }
                    Tile depotTile = arrivedDepot.getTile();
                    if (depotTile.getOccupiedBy() == null || depotTile.getOccupiedBy() == unit) {
                        depotTile.setOccupiedBy(unit);
                    }
                    unit.setCurrentTile(depotTile);
                }
                unit.advanceToNextStop();
                continue;
            }

            Tile currentTile = activePath.get(currentIndex);
            Tile nextTile = activePath.get(currentIndex + 1);
            if (nextTile.isOccupied() && nextTile.getOccupiedBy() != unit) {
                continue;
            }

            if (currentTile.getOccupiedBy() == unit) {
                currentTile.setOccupiedBy(null);
            }
            nextTile.setOccupiedBy(unit);
            unit.setCurrentTile(nextTile);
        }
    }

    private boolean claimInitialTile(TransportUnit unit, List<Tile> fallbackPath) {
        Tile tile = unit.getCurrenTile();
        if (tile == null) {
            tile = fallbackPath.getFirst();
            unit.setCurrentTile(tile);
        }

        if (tile.getOccupiedBy() == null || tile.getOccupiedBy() == unit) {
            tile.setOccupiedBy(unit);
            return true;
        }
        return false;
    }

    private void releaseTileOccupancy(TransportUnit unit) {
        Tile tile = unit.getCurrenTile();
        if (tile != null && tile.getOccupiedBy() == unit) {
            tile.setOccupiedBy(null);
        }
    }

    private int indexOfTile(List<Tile> path, Tile tile) {
        for (int i = 0; i < path.size(); i++) {
            Tile candidate = path.get(i);
            if (candidate.getX() == tile.getX() && candidate.getY() == tile.getY()) {
                return i;
            }
        }
        return -1;
    }

    private List<Tile> reversePath(List<Tile> path) {
        List<Tile> reversed = new ArrayList<>(path);
        java.util.Collections.reverse(reversed);
        return reversed;
    }

    private static class MovementState implements Serializable {
        private int segmentStartStopIndex = 0;
        private List<Tile> segmentPath = List.of();
    }
}
