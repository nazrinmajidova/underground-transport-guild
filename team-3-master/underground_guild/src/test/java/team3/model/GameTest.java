package team3.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team3.service.GameService;

class GameTest {

    private GameService gameService;
    private Game game;

    @BeforeEach
    void setUp() {
        gameService = new GameService();
        gameService.startNewGame();
        gameService.setOnGameOver(() -> {});
        game = gameService.getCurrentGame();
    }

    @Test
    void seedsStarterDepotsIntoNewGames() {
        long depotCount = game.getGameMap().getHubs().stream()
            .filter(Depot.class::isInstance)
            .count();

        assertTrue(depotCount >= 1);
    }

    @Test
    void keepsGeneratedSettlementsAndFacilitiesAlongsideDepots() {
        boolean hasSettlement = game.getGameMap().getHubs().stream().anyMatch(Settlement.class::isInstance);
        boolean hasFacility = game.getGameMap().getHubs().stream().anyMatch(IndustrialFacility.class::isInstance);

        assertTrue(hasSettlement);
        assertTrue(hasFacility);
        assertFalse(game.getGameMap().getHubs().isEmpty());
    }

    @Test
    void seedsStarterRoutesIntoNewGames() {
        assertFalse(game.getRoutes().isEmpty());
        assertTrue(game.getRoutes().stream()
            .allMatch(route -> !game.getGameMap().findTunnelPath(route.getOrigin(), route.getDestination()).isEmpty()));
    }

    @Test
    void unitsWaitWhenNextTunnelTileIsOccupied() {
        Route route = game.getRoutes().getFirst();
        List<Tile> tunnelPath = game.getGameMap().findTunnelPath(route.getOrigin(), route.getDestination());
        assertTrue(tunnelPath.size() >= 3);

        CargoTransport leadingUnit = CargoTransport.createStandard("u-lead", "Lead");
        leadingUnit.setAssignedRoute(route);
        leadingUnit.setCurrentTile(tunnelPath.get(1));

        CargoTransport trailingUnit = CargoTransport.createStandard("u-trail", "Trail");
        trailingUnit.setAssignedRoute(route);
        trailingUnit.setCurrentTile(tunnelPath.get(0));

        game.getUnits().add(trailingUnit);
        game.getUnits().add(leadingUnit);

        gameService.update();

        assertEquals(tunnelPath.get(0), trailingUnit.getCurrenTile());
        assertEquals(tunnelPath.get(2), leadingUnit.getCurrenTile());
        assertEquals(trailingUnit, tunnelPath.get(0).getOccupiedBy());
        assertEquals(leadingUnit, tunnelPath.get(2).getOccupiedBy());
    }

    @Test
    void noTwoUnitsShareSameTunnelTileAfterUpdate() {
        Route route = game.getRoutes().getFirst();
        List<Tile> tunnelPath = game.getGameMap().findTunnelPath(route.getOrigin(), route.getDestination());
        assertTrue(tunnelPath.size() >= 4);

        CargoTransport first = CargoTransport.createStandard("u1", "First");
        first.setAssignedRoute(route);
        first.setCurrentTile(tunnelPath.get(0));

        CargoTransport second = CargoTransport.createStandard("u2", "Second");
        second.setAssignedRoute(route);
        second.setCurrentTile(tunnelPath.get(2));

        game.getUnits().add(first);
        game.getUnits().add(second);

        gameService.update();

        assertNotEquals(first.getCurrenTile(), second.getCurrenTile());
        assertEquals(first, first.getCurrenTile().getOccupiedBy());
        assertEquals(second, second.getCurrenTile().getOccupiedBy());
    }

    @Test
    void integratedGameLoopUpdatesClockProductionDemandMovementAndMaintenance() {
        Route route = game.getRoutes().getFirst();

        CargoTransport unit = CargoTransport.createStandard("u-loop", "Loop Unit");
        unit.setAssignedRoute(route);
        unit.setCurrentTile(((Depot) route.getOrigin()).getTile());
        unit.setSpeed(1.0);
        game.getUnits().add(unit);

        Optional<Settlement> settlement = game.getGameMap().getHubs().stream()
            .filter(Settlement.class::isInstance)
            .map(Settlement.class::cast)
            .findFirst();
        assertTrue(settlement.isPresent());

        Optional<IndustrialFacility> facility = game.getGameMap().getHubs().stream()
            .filter(IndustrialFacility.class::isInstance)
            .map(IndustrialFacility.class::cast)
            .findFirst();
        assertTrue(facility.isPresent());

        float beforeDemand = settlement.get().getPassengerDemand();
        int beforeStoredGoods = facility.get().getStoredGoods().size();

        gameService.update();

        assertEquals(1, game.getClock().getTotalTicks());
        assertTrue(settlement.get().getPassengerDemand() > beforeDemand);
        assertTrue(facility.get().getStoredGoods().size() > beforeStoredGoods);
        assertTrue(unit.getDistanceTravelled() > 0);
        assertEquals((float) unit.calculateMaintenanceCost(), game.getEconomy().getTotalExpenses());
    }

    @Test
    void integratedGameLoopTriggersGameOverOnBankruptcyAfterMaintenance() {
        Route route = game.getRoutes().getFirst();

        CargoTransport unit = CargoTransport.createStandard("u-bankrupt", "Bankrupt Unit");
        unit.setAssignedRoute(route);
        unit.setCurrentTile(((Depot) route.getOrigin()).getTile());
        unit.setMaintenanceCost(500.0);
        game.getUnits().add(unit);

        game.getEconomy().charge(9950.0f, "force low funds");
        gameService.update();

        assertEquals("GAMEOVER", game.getGameState());
        assertTrue(game.getEconomy().isBankrupt());
    }

    @Test
    void updateHandlesEmptyRoutesAndUnitsWithoutCrashing() {
        game.getUnits().clear();
        game.getRoutes().clear();

        assertDoesNotThrow(gameService::update);
        assertEquals(1, game.getClock().getTotalTicks());
    }
}