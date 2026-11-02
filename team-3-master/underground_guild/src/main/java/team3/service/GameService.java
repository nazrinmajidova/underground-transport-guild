package team3.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import team3.model.CargoTransport;
import team3.model.Depot;
import team3.model.Economy;
import team3.model.Game;
import team3.model.GameMap;
import team3.model.Good;
import team3.model.Hub;
import team3.model.IndustrialFacility;
import team3.model.PassengerTransport;
import team3.model.Route;
import team3.model.Settlement;
import team3.model.Tile;
import team3.model.TransportUnit;
import team3.persistence.SaveManager;

/**
 * Coordinates construction, routes, vehicle purchases, persistence, and simulation updates.
 * JavaFX views call this layer rather than coordinating domain updates themselves.
 */
public class GameService {
    public static final double DEPOT_PLACEMENT_COST = 500.0;
    public static final double TUNNEL_COST = 50.0;
    public static final float LOW_BALANCE_WARNING_THRESHOLD = 1000.0f;
    public static final float GOODS_DELIVERY_INCOME_PER_UNIT = 20.0f;
    public static final float CARGO_FREIGHT_INCOME_PER_UNIT = 12.0f;
    public static final float CARGO_TRANSIT_INCOME_PER_UNIT = 1.5f;
    public static final float PASSENGER_DELIVERY_INCOME_PER_UNIT = 10.0f;
    public static final float PASSENGER_TRANSIT_INCOME_PER_UNIT = 2.0f;
    public static final float DELIVERY_DISTANCE_BONUS_FACTOR = 1.0f;
    private Runnable onGameOver;

    private final SaveManager saveManager = new SaveManager();
    private Game currentGame;
    private float deliveryIncomeTotal;
    private float maintenanceExpenseTotal;
    private float simulationTickAccumulator;

    public static class DepotPlacementResult {
        private final boolean success;
        private final String message;

        public DepotPlacementResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Lightweight read model used by UI panels to display current financial status.
     */
    public record FinancialSnapshot(
        float balance,
        float totalIncome,
        float totalExpenses,
        float deliveryIncome,
        float maintenanceExpense,
        boolean lowBalanceWarning
    ) {
    }

    public void startNewGame() {
        this.currentGame = new Game();
        this.deliveryIncomeTotal = 0.0f;
        this.maintenanceExpenseTotal = 0.0f;
        this.simulationTickAccumulator = 0.0f;
        seedStarterDepots();
        seedStarterRoutes();
    }

    public void loadGame(int slot) {
        this.currentGame = saveManager.load(slot);
        this.deliveryIncomeTotal = 0.0f;
        this.maintenanceExpenseTotal = 0.0f;
        this.simulationTickAccumulator = 0.0f;
    }

    public void saveCurrentGame(int slot) {
        if (currentGame != null) {
            saveManager.save(currentGame, slot);
        }
    }

    public String saveGameToSlot(int slot) {
        if (currentGame == null) {
            return "No active game to save.";
        }

        try {
            saveManager.save(currentGame, slot);
            return "Saved game to slot " + slot + ".";
        } catch (RuntimeException ex) {
            return "Failed to save slot " + slot + ": " + ex.getMessage();
        }
    }

    public String loadGameFromSlot(int slot) {
        try {
            loadGame(slot);
            return "Loaded save slot " + slot + ".";
        } catch (RuntimeException ex) {
            return "Unable to load slot " + slot + ": " + ex.getMessage();
        }
    }

    public boolean hasSaveSlot(int slot) {
        return saveManager.hasSave(slot);
    }

    public String getSaveSlotMetadata(int slot) {
        return saveManager.getSaveMetadata(slot);
    }

    public String openSettings() {
        return "Settings not implemented yet.";
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public DepotPlacementResult placeDepotAt(int x, int y) {
        if (currentGame == null) {
            return new DepotPlacementResult(false, "No active game.");
        }

        GameMap map      = currentGame.getGameMap();
        Economy economy  = currentGame.getEconomy();
        Tile    tile     = map.getTileAt(x, y);

        if (tile == null) {
            return new DepotPlacementResult(false, "Selected tile is out of map bounds.");
        }
        if (!tile.isHasTunnel() && !"tunnel".equalsIgnoreCase(tile.getType())) {
            return new DepotPlacementResult(false, "Depot must be placed on a tunnel tile.");
        }
        if (map.getHubAt(tile) != null) {
            return new DepotPlacementResult(false, "A hub is already placed on this tile.");
        }

        Hub adjacentHub = map.findAdjacentHub(tile);
        if (adjacentHub == null) {
            return new DepotPlacementResult(false, "Depot must be adjacent to an existing hub.");
        }
        if (economy.getBalance() < DEPOT_PLACEMENT_COST) {
            return new DepotPlacementResult(false, "Not enough funds to place a depot.");
        }

        Depot depot = new Depot(UUID.randomUUID().toString(), "Depot", tile, adjacentHub);
        map.addHub(depot);
        economy.charge((float) DEPOT_PLACEMENT_COST, "Depot placement");
        return new DepotPlacementResult(true, "Depot placed successfully.");
    }

    public boolean hasActiveGame() {
        return currentGame != null;
    }

    /**
     * Returns current financial figures for the active game session.
     */
    public FinancialSnapshot getFinancialSnapshot() {
        if (currentGame == null) {
            return new FinancialSnapshot(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false);
        }

        Economy economy = currentGame.getEconomy();
        float balance = economy.getBalance();
        return new FinancialSnapshot(
            balance,
            economy.getTotalIncome(),
            economy.getTotalExpenses(),
            deliveryIncomeTotal,
            maintenanceExpenseTotal,
            balance <= LOW_BALANCE_WARNING_THRESHOLD
        );
    }

    /**
     * Advances the simulation by one tick and resolves transport deliveries.
     */
    public void advanceSimulationTick() {
        advanceSimulationFrame(1.0f);
    }

    /**
     * Accumulates frame time and resolves whole simulation ticks at the selected speed.
     * Arrival candidates are captured before movement so each delivery is paid once,
     * even when distance-based movement and tile occupancy report the same arrival.
     *
     * @param deltaSeconds elapsed frame time in seconds; non-positive values are ignored
     */
    public void advanceSimulationFrame(float deltaSeconds) {
        if (currentGame == null) {
            return;
        }

        float speedMultiplier = getSpeedMultiplier();
        if (speedMultiplier <= 0f || deltaSeconds <= 0f) {
            return;
        }

        simulationTickAccumulator += deltaSeconds * speedMultiplier;
        while (simulationTickAccumulator >= 1.0f) {
            Economy economy = currentGame.getEconomy();
            float expensesBeforeTick = economy.getTotalExpenses();
            List<ArrivalCandidate> arrivals = captureArrivalCandidates();

            update();
            float expensesDelta = economy.getTotalExpenses() - expensesBeforeTick;
            if (expensesDelta > 0f) {
                maintenanceExpenseTotal += expensesDelta;
            }

            java.util.Set<String> processedUnits = new java.util.HashSet<>();
            for (ArrivalCandidate arrival : arrivals) {
                if (!arrival.arrived()) {
                    continue;
                }
                processedUnits.add(arrival.unit().getId());
                arrival.unit().setCurrentTile(arrival.depot().getTile());
                processDeliveryIncome(arrival.unit(), arrival.depot(), economy);
                arrival.unit().loadCargo(arrival.depot());
            }

            for (TransportUnit unit : currentGame.getUnits()) {
                if (processedUnits.contains(unit.getId())) {
                    unit.consumeArrivedAtStop();
                    continue;
                }
                if (!unit.consumeArrivedAtStop()) {
                    continue;
                }

                Depot arrivalDepot = unit.getCurrentStop();
                if (arrivalDepot == null || arrivalDepot.getTile() == null) {
                    continue;
                }

                unit.setCurrentTile(arrivalDepot.getTile());
                processDeliveryIncome(unit, arrivalDepot, economy);
                unit.loadCargo(arrivalDepot);
            }
            simulationTickAccumulator -= 1.0f;
        }
    }

    public void update() {
        if(currentGame.getGameState().equals("GAMEOVER")) {return;}
        currentGame.getClock().tick();
        updateFacilities();
        updateSettlements();
        updateUnits();
        awardInTransitCargoIncome();
        deductMaintenanceCosts();
        
        if (currentGame.getEconomy().isBankrupt()) {
            currentGame.setGameState("GAMEOVER");
            currentGame.getClock().pause();
             if (onGameOver != null) {
                onGameOver.run();
            }
        }
    }

    private void deductMaintenanceCosts() {
        for (TransportUnit unit : currentGame.getTransportUnits()) {
            if (unit == null || unit.getAssignedRoute() == null) {
                continue;
            }
            currentGame.getEconomy().charge((float) unit.calculateMaintenanceCost(), "Transport maintenance: " + unit.getName());
        }
    }

    private void updateSettlements() {
        for (Hub hub : currentGame.getGameMap().getHubs()) {
            if (hub instanceof Settlement settlement) {
                settlement.updateDemandTick(currentGame.getRandom());
                for (Depot depot : settlement.getDepots()) {
                    depot.modifyWaitingPassengers();
                }
            }
        }
    }

    private void updateFacilities() {
        for (Hub hub : currentGame.getGameMap().getHubs()) {
            if (hub instanceof IndustrialFacility facility) {
                Good produced = facility.produce();
                currentGame.moveFacilityOutputToAttachedDepots(facility, produced);
            }
        }
    }

     private void updateUnits() {
        for (TransportUnit unit : currentGame.getTransportUnits()) {
            if (unit == null || unit.getAssignedRoute() == null) {
                continue;
            }
            unit.move();
        }
        currentGame.updateUnitOccupancyAndMovement();
    }

    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;   
    }

    public float getMovementInterpolationAlpha() {
        return Math.max(0f, Math.min(1f, simulationTickAccumulator));
    }

    private float getSpeedMultiplier() {
        if (currentGame == null) {
            return 0f;
        }
        return switch (currentGame.getClock().getSpeed()) {
            case "pause" -> 0f;
            case "fast" -> 2f;
            case "very_fast" -> 4f;
            default -> 1f;
        };
    }

    private List<ArrivalCandidate> captureArrivalCandidates() {
        if (currentGame == null) {
            return List.of();
        }

        List<ArrivalCandidate> candidates = new java.util.ArrayList<>();
        for (TransportUnit unit : currentGame.getUnits()) {
            if (unit.getAssignedRoute() == null) {
                continue;
            }

            Depot expectedArrival = unit.getNextStop();
            if (expectedArrival == null) {
                continue;
            }

            double routeDistance = unit.getAssignedRoute().getDistance();
            if (routeDistance <= 0) {
                continue;
            }

            double previousDistance = unit.getDistanceTravelled();
            boolean arrived = previousDistance + unit.getSpeed() >= routeDistance;
            candidates.add(new ArrivalCandidate(unit, expectedArrival, arrived));
        }
        return candidates;
    }

    private void processDeliveryIncome(TransportUnit unit, Depot arrivalDepot, Economy economy) {
        if (unit instanceof CargoTransport cargoUnit) {
            float transportedGoods = (float) cargoUnit.getCurrentCargoWeight();
            float deliveredGoods = cargoUnit.unloadDemandedGoods(arrivalDepot);

            float income = 0f;
            if (transportedGoods > 0f) {
                income += calculateIncomeWithDistanceBonus(
                    transportedGoods * CARGO_FREIGHT_INCOME_PER_UNIT,
                    unit.getAssignedRoute().getDistance()
                );
            }
            if (deliveredGoods > 0f) {
                income += deliveredGoods * GOODS_DELIVERY_INCOME_PER_UNIT;
            }

            if (income > 0f) {
                economy.earn(
                    income,
                    "Cargo service to " + arrivalDepot.getHub().getName()
                        + " (moved: " + transportedGoods + ", fulfilled: " + deliveredGoods + ")"
                );
                deliveryIncomeTotal += income;
            }
            return;
        }

        if (unit instanceof PassengerTransport passengerUnit) {
            int onboardBeforeStop = passengerUnit.getPassengersOnBoard();
            int deliveredPassengers = passengerUnit.disembarkDeliveredPassengers(arrivalDepot);
            float income = 0f;
            if (onboardBeforeStop > 0) {
                income += onboardBeforeStop * PASSENGER_TRANSIT_INCOME_PER_UNIT;
            }
            if (deliveredPassengers > 0) {
                income += calculateIncomeWithDistanceBonus(
                    deliveredPassengers * PASSENGER_DELIVERY_INCOME_PER_UNIT,
                    unit.getAssignedRoute().getDistance()
                );
            }
            if (income > 0f) {
                economy.earn(income, "Delivered " + deliveredPassengers + " passengers to " + arrivalDepot.getHub().getName());
                deliveryIncomeTotal += income;
            }
            return;
        }
    }

    private void awardInTransitCargoIncome() {
        Economy economy = currentGame.getEconomy();
        for (TransportUnit unit : currentGame.getUnits()) {
            if (!(unit instanceof CargoTransport cargoUnit)) {
                continue;
            }
            if (unit.getAssignedRoute() == null) {
                continue;
            }
            float onboard = (float) cargoUnit.getCurrentCargoWeight();
            if (onboard <= 0f) {
                continue;
            }
            float income = onboard * CARGO_TRANSIT_INCOME_PER_UNIT;
            economy.earn(income, "Cargo in transit by " + unit.getName());
            deliveryIncomeTotal += income;
        }
    }

    private float calculateIncomeWithDistanceBonus(float baseIncome, double routeDistance) {
        if (baseIncome <= 0f) {
            return 0f;
        }
        return baseIncome + (float) routeDistance * DELIVERY_DISTANCE_BONUS_FACTOR;
    }

    private record ArrivalCandidate(TransportUnit unit, Depot depot, boolean arrived) {
    }

    public String buildTunnelAt(int x, int y) {
        if (currentGame == null) {
            return "No active game.";
        }

        GameMap gameMap = currentGame.getGameMap();
        GameMap.TunnelBuildStatus status = gameMap.getTunnelBuildStatus(x, y);
        if (status != GameMap.TunnelBuildStatus.OK) {
            return switch (status) {
                case OUT_OF_BOUNDS -> "Cannot build outside the map.";
                case BLOCKED_BY_HUB -> "Cannot build inside a hub area.";
                case ALREADY_TUNNELED -> "This tile already contains a tunnel.";
                case INVALID_TERRAIN -> "Tunnels can only be built on rock tiles.";
                case OK -> "Ready to build.";
            };
        }

        if (currentGame.getEconomy().getBalance() < TUNNEL_COST) {
            return "Insufficient funds to build a tunnel.";
        }

        gameMap.buildTunnel(x, y);
        currentGame.getEconomy().charge((float) TUNNEL_COST, "Tunnel build");
        return "Tunnel built at (" + x + ", " + y + ").";
    }

    public String removeTunnelAt(int x, int y) {
        if (currentGame == null) {
            return "No active game.";
        }

        GameMap gameMap = currentGame.getGameMap();
        Tile tile = gameMap.getTileAt(x, y);
        if (tile == null) {
            return "Cannot remove outside the map.";
        }
        if (!tile.isHasTunnel()) {
            return "No tunnel exists on this tile.";
        }
        if (gameMap.getHubAt(tile) != null || tile.getOccupant() != null) {
            return "Cannot remove tunnel under a hub.";
        }
        if (tile.isOccupied()) {
            return "Cannot remove tunnel while a vehicle is on the tile.";
        }

        tile.setHasTunnel(false);
        tile.setType(Tile.TileType.ROCK);
        return "Tunnel removed at (" + x + ", " + y + ").";
    }

    public String removeStructureAt(int x, int y) {
        if (currentGame == null) {
            return "No active game.";
        }

        GameMap gameMap = currentGame.getGameMap();
        Tile tile = gameMap.getTileAt(x, y);
        if (tile == null) {
            return "Cannot remove outside the map.";
        }

        Hub hub = gameMap.getHubAt(tile);
        if (hub instanceof Depot depot) {
            return removeDepot(depot);
        }

        return removeTunnelAt(x, y);
    }

    private String removeDepot(Depot depot) {
        if (currentGame == null || depot == null) {
            return "No active game.";
        }

        List<Route> affectedRoutes = currentGame.getRoutes().stream()
            .filter(route -> route.getDepots().contains(depot))
            .toList();

        for (Route route : affectedRoutes) {
            detachAssignedUnit(route, depot);
            for (Depot d : new java.util.ArrayList<>(route.getDepots())) {
                d.removeRoute(route);
            }
        }
        currentGame.getRoutes().removeIf(route -> route.getDepots().contains(depot));
        depot.getHub().removeDepot(depot);
        currentGame.getGameMap().removeHub(depot);
        return "Depot removed: " + depot.getName() + ".";
    }

    /**
     * Retrieves a list of all depots currently placed on the map.
     * Returns list of available Depots, or empty list if none or no active game.
     */
    public List<Depot> getAvailableDepots() {
        if (currentGame == null) return List.of();
        
        return currentGame.getGameMap().getHubs().stream()
            .filter(Depot.class::isInstance)
            .map(Depot.class::cast)
            .toList();
    }

    /**
     * Purchases a new transport unit if the player has sufficient funds.
     * Returns a status message reporting success or the reason for failure
     */
    public String purchaseTransportUnit(String category, String variant, String name, Depot spawnDepot) {
        if (currentGame == null) {
            return "No active game.";
        }
        
        double cost = 0;
        TransportUnit unit;
        String id = UUID.randomUUID().toString();

        if ("Cargo".equalsIgnoreCase(category)) {
            if ("FAST".equalsIgnoreCase(variant)) {
                cost = 1300.0;
                unit = CargoTransport.createFast(id, name);
            } else {
                cost = 800.0;
                unit = CargoTransport.createStandard(id, name);
            }
        } else {
            if ("FAST".equalsIgnoreCase(variant)) {
                cost = 1000.0;
                unit = PassengerTransport.createFast(id, name);
            } else {
                cost = 600.0;
                unit = PassengerTransport.createStandard(id, name);
            }
        }

        Economy econ = currentGame.getEconomy();
        if (econ.getBalance() < cost) {
            return "Insufficient funds. Need $" + cost;
        }

        econ.charge((float) cost, "Purchased " + category + " Transport: " + name);
        if (spawnDepot != null) {
            unit.setCurrentTile(spawnDepot.getTile());
        } else {
            List<Depot> depots = getAvailableDepots();
            if (!depots.isEmpty()) {
                unit.setCurrentTile(depots.get(0).getTile());
            }
        }
        
        currentGame.getUnits().add(unit);
        return "Purchase successful: " + name;
    }

    public String sellTransportUnit(String unitId) {
        if (currentGame == null) {
            return "No active game.";
        }

        TransportUnit unit = currentGame.getUnits().stream()
            .filter(candidate -> candidate.getId().equals(unitId))
            .findFirst()
            .orElse(null);
        if (unit == null) {
            return "Selected vehicle not found.";
        }

        Route assigned = unit.getAssignedRoute();
        if (assigned != null) {
            assigned.removeUnit(unit);
            unit.setAssignedRoute(null);
        }

        Tile currentTile = unit.getCurrenTile();
        if (currentTile != null && currentTile.getOccupiedBy() == unit) {
            currentTile.setOccupiedBy(null);
        }

        float refund = calculateVehicleResaleValue(unit);
        currentGame.getUnits().remove(unit);
        currentGame.getEconomy().earn(refund, "Sold transport unit: " + unit.getName());
        return "Sold " + unit.getName() + " for $" + String.format("%.2f", refund) + ".";
    }

    public String upgradeTransportUnit(String unitId) {
        if (currentGame == null) {
            return "No active game.";
        }

        TransportUnit unit = currentGame.getUnits().stream()
            .filter(candidate -> candidate.getId().equals(unitId))
            .findFirst()
            .orElse(null);
        if (unit == null) {
            return "Selected vehicle not found.";
        }

        float upgradeCost = (unit instanceof CargoTransport) ? 320.0f : 260.0f;
        Economy economy = currentGame.getEconomy();
        if (economy.getBalance() < upgradeCost) {
            return "Insufficient funds to upgrade. Need $" + upgradeCost;
        }
        upgradeCost = upgradeCost * (1 + 0.5f * unit.getUpgradeLevel());
        unit.upgrade();
        economy.charge(upgradeCost, "Vehicle upgrade: " + unit.getName());
        return "Upgraded " + unit.getName() + " to level " + unit.getUpgradeLevel() + ".";
    }

    private float calculateVehicleResaleValue(TransportUnit unit) {
        if (unit instanceof CargoTransport cargo) {
            return "FAST".equalsIgnoreCase(cargo.getVariant()) ? 480.0f : 300.0f;
        }
        if (unit instanceof PassengerTransport passenger) {
            return "FAST".equalsIgnoreCase(passenger.getVariant()) ? 420.0f : 240.0f;
        }
        return 200.0f;
    }

    /**
     * Validates and creates a new route based on a sequence of player-selected depots.
     * Returns "Success" if valid, otherwise an error message describing the failure.
     */
    public String createRoute(String name, List<Depot> sequence) {
        if (currentGame == null) return "No active game.";
        if (sequence == null || sequence.size() < 2) return "Route must have at least 2 depots.";

        GameMap map = currentGame.getGameMap();
        double totalDistance = 0;

        for (int i = 0; i < sequence.size() - 1; i++) {
            Depot d1 = sequence.get(i);
            Depot d2 = sequence.get(i + 1);
            List<Tile> path = map.findTunnelPath(d1, d2);
            
            if (path.isEmpty()) {
                return "Depots '" + d1.getName() + "' and '" + d2.getName() + "' are not connected by tunnels.";
            }
            totalDistance += path.size();
        }

        Route route = new Route(UUID.randomUUID().toString(), name, sequence, totalDistance);
        currentGame.getRoutes().add(route);
        return "Success";
    }

    public String renameRoute(String routeId, String newName) {
        if (currentGame == null) {
            return "No active game.";
        }
        if (newName == null || newName.trim().isEmpty()) {
            return "Route name cannot be empty.";
        }

        Route route = currentGame.getRoutes().stream()
            .filter(candidate -> candidate.getId().equals(routeId))
            .findFirst()
            .orElse(null);
        if (route == null) {
            return "Selected route not found.";
        }

        route.setName(newName.trim());
        return "Route renamed to " + route.getName() + ".";
    }

    public String renameDepot(String depotId, String newName) {
        if (currentGame == null) {
            return "No active game.";
        }
        if (depotId == null || depotId.isBlank()) {
            return "Selected depot not found.";
        }
        if (newName == null || newName.trim().isEmpty()) {
            return "Depot name cannot be empty.";
        }

        Depot depot = currentGame.getGameMap().getHubs().stream()
            .filter(Depot.class::isInstance)
            .map(Depot.class::cast)
            .filter(candidate -> candidate.getId().equals(depotId))
            .findFirst()
            .orElse(null);
        if (depot == null) {
            return "Selected depot not found.";
        }

        depot.setName(newName.trim());
        return "Depot renamed to " + depot.getName() + ".";
    }

    public String deleteRoute(String routeId) {
        if (currentGame == null) {
            return "No active game.";
        }

        Route route = currentGame.getRoutes().stream()
            .filter(candidate -> candidate.getId().equals(routeId))
            .findFirst()
            .orElse(null);
        if (route == null) {
            return "Selected route not found.";
        }

        detachAssignedUnit(route, null);

        if (route.getOrigin() instanceof Depot originDepot) {
            originDepot.removeRoute(route);
        }
        if (route.getDestination() instanceof Depot destinationDepot) {
            destinationDepot.removeRoute(route);
        }

        currentGame.getRoutes().remove(route);
        return "Deleted route " + route.getName() + ".";
    }

    public String unassignVehicleFromRoute(String routeId) {
        if (currentGame == null) {
            return "No active game.";
        }

        Route route = currentGame.getRoutes().stream()
            .filter(candidate -> candidate.getId().equals(routeId))
            .findFirst()
            .orElse(null);
        if (route == null) {
            return "Selected route not found.";
        }
        if (route.getAssignedUnit() == null) {
            return "No vehicle is assigned to this route.";
        }

        TransportUnit unit = route.getAssignedUnit();
        detachAssignedUnit(route, null);
        return "Unassigned " + unit.getName() + " from route " + route.getName() + ".";
    }

    public String updateRouteStops(String routeId, List<Depot> sequence) {
        if (currentGame == null) {
            return "No active game.";
        }
        if (sequence == null || sequence.size() < 2) {
            return "Route must have at least 2 depots.";
        }

        Route existing = currentGame.getRoutes().stream()
            .filter(candidate -> candidate.getId().equals(routeId))
            .findFirst()
            .orElse(null);
        if (existing == null) {
            return "Selected route not found.";
        }

        GameMap map = currentGame.getGameMap();
        double totalDistance = 0;
        for (int i = 0; i < sequence.size() - 1; i++) {
            Depot d1 = sequence.get(i);
            Depot d2 = sequence.get(i + 1);
            List<Tile> path = map.findTunnelPath(d1, d2);
            if (path.isEmpty()) {
                List<Tile> buildable = map.findBuildableTunnelPath(d1, d2);
                if (buildable.isEmpty()) {
                    return "Depots '" + d1.getName() + "' and '" + d2.getName() + "' are not connected by tunnels.";
                }
                for (Tile tile : buildable) {
                    if (tile.canBuildTunnel()) {
                        map.buildTunnel(tile.getX(), tile.getY());
                    }
                }
                path = map.findTunnelPath(d1, d2);
                if (path.isEmpty()) {
                    return "Depots '" + d1.getName() + "' and '" + d2.getName() + "' are not connected by tunnels.";
                }
            }
            totalDistance += path.size();
        }

        TransportUnit assigned = existing.getAssignedUnit();
        for (Depot depot : new java.util.ArrayList<>(existing.getDepots())) {
            depot.removeRoute(existing);
        }

        Route updated = new Route(existing.getId(), existing.getName(), sequence, totalDistance);
        if (assigned != null) {
            updated.assignUnit(assigned);
            assigned.setAssignedRoute(updated);
            if (updated.getOrigin() instanceof Depot originDepot) {
                assigned.setCurrentTile(originDepot.getTile());
            }
        }
        for (Depot depot : sequence) {
            depot.addRoute(updated);
        }

        int index = currentGame.getRoutes().indexOf(existing);
        if (index >= 0) {
            currentGame.getRoutes().set(index, updated);
        }
        return "Route stops updated for " + updated.getName() + ".";
    }

    private void detachAssignedUnit(Route route, Depot removedDepot) {
        if (route == null) {
            return;
        }

        TransportUnit unit = route.getAssignedUnit();
        if (unit == null) {
            return;
        }

        route.removeUnit(unit);
        unit.setAssignedRoute(null);

        Tile currentTile = unit.getCurrenTile();
        if (currentTile != null && currentTile.getOccupiedBy() == unit) {
            currentTile.setOccupiedBy(null);
        }

        if (currentGame == null) {
            return;
        }
        Depot fallbackDepot = currentGame.getGameMap().getHubs().stream()
            .filter(Depot.class::isInstance)
            .map(Depot.class::cast)
            .filter(candidate -> removedDepot == null || !candidate.getId().equals(removedDepot.getId()))
            .findFirst()
            .orElse(null);
        if (fallbackDepot != null) {
            unit.setCurrentTile(fallbackDepot.getTile());
            fallbackDepot.getTile().setOccupiedBy(unit);
        }
    }

    /**
     * Assigns an existing transport unit to an existing route.
     */
    public String assignUnitToRoute(String unitId, String routeId) {
        if (currentGame == null) {
            return "No active game.";
        }

        TransportUnit unit = currentGame.getUnits().stream()
            .filter(candidate -> candidate.getId().equals(unitId))
            .findFirst()
            .orElse(null);
        if (unit == null) {
            return "Selected unit not found.";
        }

        Route route = currentGame.getRoutes().stream()
            .filter(candidate -> candidate.getId().equals(routeId))
            .findFirst()
            .orElse(null);
        if (route == null) {
            return "Selected route not found.";
        }

        TransportUnit previousAssigned = route.getAssignedUnit();
        if (previousAssigned != null && previousAssigned != unit) {
            detachAssignedUnit(route, null);
        }

        if (unit.getAssignedRoute() != null && unit.getAssignedRoute() != route) {
            unit.getAssignedRoute().removeUnit(unit);
        }

        route.assignUnit(unit);
        unit.setAssignedRoute(route);
        if (route.getOrigin() instanceof Depot originDepot && originDepot.getTile() != null) {
            unit.setCurrentTile(originDepot.getTile());
        }

        if (previousAssigned != null && previousAssigned != unit) {
            return "Assigned " + unit.getName() + " to route " + route.getName()
                + " and unassigned " + previousAssigned.getName() + ".";
        }
        return "Assigned " + unit.getName() + " to route " + route.getName() + ".";
    }

    private void seedStarterDepots() {
        int depotIndex = 1;
        List<Hub> starterHubs = new ArrayList<>(currentGame.getGameMap().getHubs());
        for (Hub hub : starterHubs) {
            if (!(hub instanceof Settlement)) {
                continue;
            }

            Tile depotTile = findAdjacentDepotTile(hub);
            if (depotTile != null) {
                currentGame.getGameMap().addHub(new Depot("depot-" + depotIndex, "Depot " + depotIndex, depotTile, hub));
                depotIndex++;
            }

            if (depotIndex > 3) {
                break;
            }
        }
    }

    private void seedStarterRoutes() {
        List<Depot> depots = currentGame.getGameMap().getHubs().stream()
            .filter(Depot.class::isInstance)
            .map(Depot.class::cast)
            .collect(Collectors.toList());

        for (int index = 0; index < depots.size() - 1; index++) {
            Depot origin = depots.get(index);
            Depot destination = depots.get(index + 1);
            List<Tile> path = currentGame.getGameMap().findBuildableTunnelPath(origin, destination);
            if (path.isEmpty()) {
                continue;
            }

            carveStarterRoute(path);

            Route route = new Route(
                "route-" + (index + 1),
                origin.getName() + " -> " + destination.getName(),
                origin,
                destination,
                path.size()
            );
            currentGame.getRoutes().add(route);
            origin.addRoute(route);
            destination.addRoute(route);
        }
    }

    private Tile findAdjacentDepotTile(Hub hub) {
        int startX = hub.getTile().getX();
        int startY = hub.getTile().getY();
        int endX = startX + hub.getFootprintWidth() - 1;
        int endY = startY + hub.getFootprintHeight() - 1;

        Tile[] candidates = new Tile[] {
            currentGame.getGameMap().getTileAt(endX + 1, startY),
            currentGame.getGameMap().getTileAt(startX - 1, startY),
            currentGame.getGameMap().getTileAt(startX, endY + 1),
            currentGame.getGameMap().getTileAt(startX, startY - 1)
        };

        for (Tile candidate : candidates) {
            if (candidate != null && currentGame.getGameMap().canPlaceHubAt(candidate.getX(), candidate.getY(), 1, 1)) {
                return candidate;
            }
        }
        return null;
    }

    private void carveStarterRoute(List<Tile> path) {
        for (Tile tile : path) {
            if (tile.canBuildTunnel()) {
                currentGame.getGameMap().buildTunnel(tile.getX(), tile.getY());
            }
        }
    }
}
