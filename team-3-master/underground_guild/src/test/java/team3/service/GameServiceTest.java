package team3.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import team3.model.CargoTransport;
import team3.model.Depot;
import team3.model.Game;
import team3.model.GameMap;
import team3.model.Good;
import team3.model.GoodType;
import team3.model.Hub;
import team3.model.PassengerTransport;
import team3.model.Route;
import team3.model.Settlement;
import team3.model.Tile;
import team3.model.TransportUnit;

class GameServiceTest {

    @Test
    void buildsTunnelOnAValidRockTileAndChargesThePlayer() {
        GameService service = new GameService();
        service.startNewGame();

        Tile candidate = findFirstBuildableTile(service.getCurrentGame().getGameMap());
        double startingBalance = service.getCurrentGame().getEconomy().getBalance();

        String message = service.buildTunnelAt(candidate.getX(), candidate.getY());

        assertEquals("Tunnel built at (" + candidate.getX() + ", " + candidate.getY() + ").", message);
        assertTrue(candidate.isHasTunnel());
        assertEquals(startingBalance - GameService.TUNNEL_COST, service.getCurrentGame().getEconomy().getBalance());
    }

    @Test
    void rejectsBuildInsideHubAreas() {
        GameService service = new GameService();
        service.startNewGame();

        Tile occupied = service.getCurrentGame().getGameMap().getHubs().getFirst().getTile();

        String message = service.buildTunnelAt(occupied.getX(), occupied.getY());

        assertEquals("Cannot build inside a hub area.", message);
        assertFalse(occupied.isHasTunnel());
    }

    @Test
    void rejectsBuildWhenFundsAreTooLow() {
        GameService service = new GameService();
        service.startNewGame();
        service.getCurrentGame().getEconomy().charge(9990, "test");

        Tile candidate = findFirstBuildableTile(service.getCurrentGame().getGameMap());
        String message = service.buildTunnelAt(candidate.getX(), candidate.getY());

        assertEquals("Insufficient funds to build a tunnel.", message);
        assertFalse(candidate.isHasTunnel());
    }

    @Test
    void returnsZeroSnapshotWhenNoGameIsActive() {
        GameService service = new GameService();

        GameService.FinancialSnapshot snapshot = service.getFinancialSnapshot();

        assertEquals(0.0f, snapshot.balance());
        assertEquals(0.0f, snapshot.totalIncome());
        assertEquals(0.0f, snapshot.totalExpenses());
        assertEquals(0.0f, snapshot.deliveryIncome());
        assertEquals(0.0f, snapshot.maintenanceExpense());
        assertFalse(snapshot.lowBalanceWarning());
    }

    @Test
    void returnsLiveFinancialSnapshotWithWarningWhenBalanceIsLow() {
        GameService service = new GameService();
        service.startNewGame();
        service.getCurrentGame().getEconomy().earn(200.0f, "test income");
        service.getCurrentGame().getEconomy().charge(9300.0f, "test expense");

        GameService.FinancialSnapshot snapshot = service.getFinancialSnapshot();

        assertEquals(900.0f, snapshot.balance());
        assertEquals(200.0f, snapshot.totalIncome());
        assertEquals(9300.0f, snapshot.totalExpenses());
        assertEquals(0.0f, snapshot.deliveryIncome());
        assertEquals(0.0f, snapshot.maintenanceExpense());
        assertTrue(snapshot.lowBalanceWarning());
    }

    @Test
    void deliveryTickAwardsIncomeForGoodsDeliveredToSettlementDemand() {
        GameService service = new GameService();
        service.startNewGame();
        Game game = service.getCurrentGame();

        Route route = setupRouteWithSettlementDestination(game, "Goods Route", 5.0);
        Depot origin = (Depot) route.getOrigin();
        Depot destination = (Depot) route.getDestination();
        Settlement settlement = (Settlement) destination.getHub();

        float startingFoodDemand = settlement.getGoodsDemand().get(GoodType.FOOD);
        float startingBalance = game.getEconomy().getBalance();

        origin.addGoods(List.of(new Good(GoodType.FOOD, 5f)));
        CargoTransport unit = CargoTransport.createStandard("cargo-1", "Cargo #1");
        unit.setAssignedRoute(route);
        unit.setCurrentTile(origin.getTile());
        unit.loadGoods(origin);
        game.getUnits().add(unit);

        service.advanceSimulationTick();
        GameService.FinancialSnapshot snapshot = service.getFinancialSnapshot();

        assertTrue(game.getEconomy().getTotalIncome() > 0f);
        assertTrue(settlement.getGoodsDemand().get(GoodType.FOOD) < startingFoodDemand);
        assertTrue(snapshot.deliveryIncome() > 0f);
        assertEquals(
            startingBalance - snapshot.maintenanceExpense() + snapshot.deliveryIncome(),
            game.getEconomy().getBalance(),
            0.001f
        );
    }

    @Test
    void deliveryTickAwardsIncomeForPassengersDeliveredToSettlement() {
        GameService service = new GameService();
        service.startNewGame();
        Game game = service.getCurrentGame();

        Route route = setupRouteWithSettlementDestination(game, "Passenger Route", 5.0);
        Depot origin = (Depot) route.getOrigin();
        Depot destination = (Depot) route.getDestination();
        Settlement settlement = (Settlement) destination.getHub();

        float startingBalance = game.getEconomy().getBalance();

        origin.addWaitingPassengers(15);
        PassengerTransport unit = PassengerTransport.createStandard("passenger-1", "Passenger #1");
        unit.setAssignedRoute(route);
        unit.setCurrentTile(origin.getTile());
        unit.boardPassengers(origin);
        game.getUnits().add(unit);

        service.advanceSimulationTick();
        GameService.FinancialSnapshot snapshot = service.getFinancialSnapshot();

        assertTrue(game.getEconomy().getTotalIncome() > 0f);
        assertTrue(snapshot.deliveryIncome() > 0f);
        assertTrue(settlement.getPopulation() > 100);
        assertEquals(
            startingBalance - snapshot.maintenanceExpense() + snapshot.deliveryIncome(),
            game.getEconomy().getBalance(),
            0.001f
        );
    }

    @Test
    void cargoDeliveryAwardsFreightIncomeEvenWithoutSettlementDemandFulfillment() {
        GameService service = new GameService();
        service.startNewGame();
        Game game = service.getCurrentGame();
        GameMap map = game.getGameMap();

        Tile originTile = map.getTileAt(3, 3);
        Tile destinationTile = map.getTileAt(7, 3);
        Hub originHub = new Hub("freight-origin-hub", "Freight Origin Hub", originTile) {
        };
        Hub destinationHub = new Hub("freight-destination-hub", "Freight Destination Hub", destinationTile) {
        };
        Depot originDepot = new Depot("freight-origin-depot", "Freight Origin Depot", originTile, originHub);
        Depot destinationDepot = new Depot("freight-destination-depot", "Freight Destination Depot", destinationTile, destinationHub);
        map.addHub(originDepot);
        map.addHub(destinationDepot);

        Route route = new Route("freight-route", "Freight Route", originDepot, destinationDepot, 5.0);
        game.getRoutes().add(route);
        originDepot.addRoute(route);
        destinationDepot.addRoute(route);

        originDepot.addGoods(List.of(new Good(GoodType.ORE, 8f)));
        CargoTransport unit = CargoTransport.createStandard("cargo-freight", "Cargo Freight");
        unit.setAssignedRoute(route);
        unit.setCurrentTile(originTile);
        unit.loadGoods(originDepot);
        game.getUnits().add(unit);

        service.advanceSimulationTick();
        GameService.FinancialSnapshot snapshot = service.getFinancialSnapshot();

        assertTrue(snapshot.deliveryIncome() > 0f);
        assertTrue(game.getEconomy().getTotalIncome() > 0f);
    }

    @Test
    void facilityWithAttachedDepotTransfersProducedGoodsToDepotStorage() {
        GameService service = new GameService();
        service.startNewGame();
        Game game = service.getCurrentGame();
        GameMap map = game.getGameMap();

        var facility = map.getHubs().stream()
            .filter(team3.model.IndustrialFacility.class::isInstance)
            .map(team3.model.IndustrialFacility.class::cast)
            .findFirst()
            .orElseThrow();

        Tile facilityTile = facility.getTile();
        Tile[] candidates = new Tile[] {
            map.getTileAt(facilityTile.getX() + 2, facilityTile.getY()),
            map.getTileAt(facilityTile.getX() - 1, facilityTile.getY()),
            map.getTileAt(facilityTile.getX(), facilityTile.getY() + 2),
            map.getTileAt(facilityTile.getX(), facilityTile.getY() - 1)
        };
        Tile depotTile = null;
        for (Tile candidate : candidates) {
            if (candidate != null && map.getHubAt(candidate) == null) {
                depotTile = candidate;
                break;
            }
        }
        assertNotNull(depotTile);

        Depot facilityDepot = new Depot("facility-depot-test", "Facility Depot Test", depotTile, facility);
        map.addHub(facilityDepot);
        int initialGoodsCount = facilityDepot.getStoredGoods().size();

        service.advanceSimulationTick();

        assertTrue(facilityDepot.getStoredGoods().size() > initialGoodsCount);
    }

    @Test
    void maintenanceTickChargesOnlyActiveUnitsAndTracksMaintenanceExpenses() {
        GameService service = new GameService();
        service.startNewGame();
        Game game = service.getCurrentGame();

        Route activeRoute = setupRouteWithSettlementDestination(game, "Maintenance Route", 20.0);
        Depot origin = (Depot) activeRoute.getOrigin();

        CargoTransport activeUnit = CargoTransport.createStandard("active-cargo", "Active Cargo");
        activeUnit.setAssignedRoute(activeRoute);
        activeUnit.setCurrentTile(origin.getTile());
        game.getUnits().add(activeUnit);

        PassengerTransport inactiveUnit = PassengerTransport.createStandard("idle-passenger", "Idle Passenger");
        game.getUnits().add(inactiveUnit);

        float startingBalance = game.getEconomy().getBalance();
        service.advanceSimulationTick();

        float expectedMaintenance = (float) activeUnit.calculateMaintenanceCost();
        assertEquals(expectedMaintenance, service.getFinancialSnapshot().maintenanceExpense());
        assertEquals(expectedMaintenance, game.getEconomy().getTotalExpenses());
        assertEquals(startingBalance - expectedMaintenance, game.getEconomy().getBalance());
    }

    @Test
    void removeTunnelModeRemovesExistingTunnelTile() {
        GameService service = new GameService();
        service.startNewGame();

        Tile candidate = findFirstBuildableTile(service.getCurrentGame().getGameMap());
        assertEquals(
            "Tunnel built at (" + candidate.getX() + ", " + candidate.getY() + ").",
            service.buildTunnelAt(candidate.getX(), candidate.getY())
        );

        String message = service.removeTunnelAt(candidate.getX(), candidate.getY());
        assertEquals("Tunnel removed at (" + candidate.getX() + ", " + candidate.getY() + ").", message);
        assertFalse(candidate.isHasTunnel());
    }

    @Test
    void routeCanBeRenamedAndDeleted() {
        GameService service = new GameService();
        service.startNewGame();
        Game game = service.getCurrentGame();
        Route route = setupRouteWithSettlementDestination(game, "Manage Route", 8.0);

        String renameResult = service.renameRoute(route.getId(), "Renamed Route");
        assertEquals("Route renamed to Renamed Route.", renameResult);
        assertEquals("Renamed Route", route.getName());

        String deleteResult = service.deleteRoute(route.getId());
        assertEquals("Deleted route Renamed Route.", deleteResult);
        assertTrue(game.getRoutes().stream().noneMatch(r -> r.getId().equals(route.getId())));
    }

    @Test
    void depotCanBeRenamed() {
        GameService service = new GameService();
        service.startNewGame();
        Game game = service.getCurrentGame();
        Route route = setupRouteWithSettlementDestination(game, "Depot Rename Route", 8.0);
        Depot depot = (Depot) route.getOrigin();

        String renameResult = service.renameDepot(depot.getId(), "Central Depot");
        assertEquals("Depot renamed to Central Depot.", renameResult);
        assertEquals("Central Depot", depot.getName());
    }

    @Test
    void removeStructureRemovesDepotAndAffectedRoutes() {
        GameService service = new GameService();
        service.startNewGame();
        Game game = service.getCurrentGame();
        Route route = setupRouteWithSettlementDestination(game, "Depot Removal Route", 7.0);
        Depot depot = (Depot) route.getOrigin();
        CargoTransport unit = CargoTransport.createStandard("u-depot-remove", "Depot Remove Unit");
        unit.setAssignedRoute(route);
        unit.setCurrentTile(depot.getTile());
        route.assignUnit(unit);
        game.getUnits().add(unit);

        String result = service.removeStructureAt(depot.getTile().getX(), depot.getTile().getY());

        assertTrue(result.startsWith("Depot removed:"));
        assertTrue(game.getRoutes().stream().noneMatch(r -> r.getId().equals(route.getId())));
        assertFalse(game.getGameMap().getHubs().contains(depot));
        assertTrue(unit.getAssignedRoute() == null);
        assertTrue(unit.getCurrenTile() != null);
        assertFalse(unit.getCurrenTile().equals(depot.getTile()));
    }

    @Test
    void routeCanUpdateStopsAndUnassignVehicle() {
        GameService service = new GameService();
        service.startNewGame();
        Game game = service.getCurrentGame();
        Route route = setupRouteWithSettlementDestination(game, "Modify Route", 8.0);

        Depot origin = (Depot) route.getOrigin();
        Depot destination = (Depot) route.getDestination();

        int ox = origin.getTile().getX();
        int oy = origin.getTile().getY();
        int dx = destination.getTile().getX();
        int dy = destination.getTile().getY();

        if (oy == dy) {
            int step = ox < dx ? 1 : -1;
            for (int x = ox; x != dx + step; x += step) {
                game.getGameMap().buildTunnel(x, oy);
            }
        } else {
            for (int x = ox; x != dx; x += (ox < dx ? 1 : -1)) {
                game.getGameMap().buildTunnel(x, oy);
            }
            int xEnd = dx + (ox < dx ? -1 : 1);
            for (int y = oy; y != dy; y += (oy < dy ? 1 : -1)) {
                game.getGameMap().buildTunnel(xEnd, y);
            }
        }

        List<Depot> sequence = List.of(destination, origin);
        String updateResult = service.updateRouteStops(route.getId(), sequence);
        assertTrue(updateResult.startsWith("Route stops updated"));

        CargoTransport unit = CargoTransport.createStandard("assign-u1", "Assign U1");
        game.getUnits().add(unit);
        assertTrue(service.assignUnitToRoute(unit.getId(), route.getId()).startsWith("Assigned"));
        assertTrue(service.unassignVehicleFromRoute(route.getId()).startsWith("Unassigned"));
        assertTrue(unit.getAssignedRoute() == null);
    }

    @Test
    void vehicleCanBeSoldAndRefundsBalance() {
        GameService service = new GameService();
        service.startNewGame();
        Game game = service.getCurrentGame();

        float beforePurchase = game.getEconomy().getBalance();
        assertTrue(service.purchaseTransportUnit("Passenger", "STANDARD", "SellMe", null).startsWith("Purchase successful"));
        TransportUnit soldUnit = game.getUnits().stream().filter(u -> "SellMe".equals(u.getName())).findFirst().orElse(null);
        assertNotNull(soldUnit);

        float afterPurchase = game.getEconomy().getBalance();
        String sellResult = service.sellTransportUnit(soldUnit.getId());
        assertTrue(sellResult.startsWith("Sold"));
        assertTrue(game.getUnits().stream().noneMatch(u -> u.getId().equals(soldUnit.getId())));
        assertTrue(game.getEconomy().getBalance() > afterPurchase);
        assertTrue(game.getEconomy().getBalance() < beforePurchase);
    }

    @Test
    void vehicleCanBeUpgradedAndStatsIncrease() {
        GameService service = new GameService();
        service.startNewGame();
        Game game = service.getCurrentGame();

        assertTrue(service.purchaseTransportUnit("Cargo", "STANDARD", "UpgradeMe", null).startsWith("Purchase successful"));
        TransportUnit unit = game.getUnits().stream().filter(u -> "UpgradeMe".equals(u.getName())).findFirst().orElse(null);
        assertNotNull(unit);

        double beforeSpeed = unit.getSpeed();
        int beforeCapacity = unit.getCapacity();
        double beforeMaintenance = unit.getMaintenanceCost();

        String result = service.upgradeTransportUnit(unit.getId());
        assertTrue(result.startsWith("Upgraded"));
        assertTrue(unit.getSpeed() > beforeSpeed);
        assertTrue(unit.getCapacity() > beforeCapacity);
        assertTrue(unit.getMaintenanceCost() > beforeMaintenance);
        assertEquals(1, unit.getUpgradeLevel());
    }

    private Tile findFirstBuildableTile(GameMap gameMap) {
        for (int x = 0; x < gameMap.getWidth(); x++) {
            for (int y = 0; y < gameMap.getHeight(); y++) {
                Tile tile = gameMap.getTileAt(x, y);
                if (tile.canBuildTunnel()) {
                    return tile;
                }
            }
        }
        throw new IllegalStateException("No buildable rock tile found");
    }

    private Route setupRouteWithSettlementDestination(Game game, String routeName, double distance) {
        GameMap map = game.getGameMap();
        Tile originTile = findFreeTile(map);
        Tile destinationDepotTile = findFreeTile(map);
        Settlement settlement = buildSettlementAt(map, 10, 10);

        Hub originHub = new Hub("origin-hub", "Origin Hub", originTile) {
            @Override
            public void addDepot(Depot depot) {
            }
        };

        Depot originDepot = new Depot("origin-depot", "Origin Depot", originTile, originHub);
        Depot destinationDepot = new Depot("destination-depot", "Destination Depot", destinationDepotTile, settlement);
        map.addHub(originDepot);
        map.addHub(destinationDepot);

        Route route = new Route("test-route-" + routeName, routeName, originDepot, destinationDepot, distance);
        game.getRoutes().add(route);
        originDepot.addRoute(route);
        destinationDepot.addRoute(route);
        return route;
    }

    private Tile findFreeTile(GameMap map) {
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Tile tile = map.getTileAt(x, y);
                if (tile != null && map.getHubAt(tile) == null && !tile.isHasTunnel()) {
                    return tile;
                }
            }
        }
        throw new IllegalStateException("No free tile found on map");
    }

    private Settlement buildSettlementAt(GameMap map, int startX, int startY) {
        List<Tile> tiles = new ArrayList<>();
        for (int x = startX; x < startX + 3; x++) {
            for (int y = startY; y < startY + 3; y++) {
                Tile tile = map.getTileAt(x, y);
                assertNotNull(tile);
                tiles.add(tile);
            }
        }
        return new Settlement("settlement-test", "Settlement Test", tiles, 100);
    }
}
