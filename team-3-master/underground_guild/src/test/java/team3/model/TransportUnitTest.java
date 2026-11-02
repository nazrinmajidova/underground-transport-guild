package team3.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

public class TransportUnitTest {
    private TransportUnit createTestUnit(Route route){
        TransportUnit unit = new CargoTransport(
                "u1", "Cargo Unit",
                10.0, 5, 100.0,
                "STANDARD",
                List.of(GoodType.FOOD)
        );
        unit.setAssignedRoute(route);
        return unit;
    }

    private Route createSimpleRoute() {
        Tile tile1 = new Tile(0, 0, Tile.TileType.ROCK);
        Tile tile2 = new Tile(1, 0, Tile.TileType.ROCK);

        Hub hub = new Hub("h1", "TestHub", tile1) {
            @Override
            public void addDepot(Depot depot) {
            }
        };

        Depot origin = new Depot("d1", "Origin", tile1, hub);
        Depot destination = new Depot("d2", "Destination", tile2, hub);

        return new Route("r1", "Test Route", origin, destination, 50);
    }

    @Test
    void moveIncreasesDistance() {
        TransportUnit unit = createTestUnit(createSimpleRoute());

        unit.move();

        assertTrue(unit.getDistanceTravelled() > 0);
    }

    @Test
    void reachingEndResetsDistance() {
        TransportUnit unit = createTestUnit(createSimpleRoute());

        for (int i = 0; i < 10; i++) {
            unit.move();
        }

        assertTrue(unit.getDistanceTravelled() == 0 || unit.getDistanceTravelled() <= unit.getAssignedRoute().getDistance());
    }
    @Test
    void getNextStopReturnsDepot() {
        TransportUnit unit = createTestUnit(createSimpleRoute());

        Depot next = unit.getNextStop();

        assertNotNull(next);
    }

    @Test
    void maintenanceCostUsesTileModifier() {
        TransportUnit unit = createTestUnit(createSimpleRoute());

        Tile tile = new Tile(0, 0, Tile.TileType.ROCK);
        tile.setMaintenanceCostModifier(2.0f);

        unit.setCurrentTile(tile);

        assertEquals(200.0, unit.calculateMaintenanceCost());
    }

    @Test
    void maintenanceCostWithoutTileUsesBaseValue() {
        TransportUnit unit = createTestUnit(createSimpleRoute());

        assertEquals(100.0, unit.calculateMaintenanceCost());
    }

    @Test
    void multiStopRouteCyclesAtoBtoCtoA() {
        Depot a = createDepot("a", 0, 0);
        Depot b = createDepot("b", 1, 0);
        Depot c = createDepot("c", 2, 0);
        Route loop = new Route("r-loop", "Loop", List.of(a, b, c), 6.0);

        TransportUnit unit = createTestUnit(loop);

        assertEquals(a, unit.getCurrentStop());
        assertEquals(b, unit.getNextStop());

        unit.advanceToNextStop();
        assertEquals(b, unit.getCurrentStop());
        assertEquals(c, unit.getNextStop());

        unit.advanceToNextStop();
        assertEquals(c, unit.getCurrentStop());
        assertEquals(a, unit.getNextStop());

        unit.advanceToNextStop();
        assertEquals(a, unit.getCurrentStop());
        assertEquals(b, unit.getNextStop());
    }

    private Depot createDepot(String suffix, int x, int y) {
        Tile tile = new Tile(x, y, "tunnel");
        Hub hub = new Hub("h-" + suffix, "Hub " + suffix, new Tile(x + 10, y + 10, Tile.TileType.ROCK)) {
            @Override
            public void addDepot(Depot depot) {
            }
        };
        return new Depot("d-" + suffix, "Depot " + suffix, tile, hub);
    }
}

