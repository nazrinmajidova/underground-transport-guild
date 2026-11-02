package team3.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class RouteTest {

    @Test
    void getNextDepotWrapsAroundForMultiStopRoute() {
        Depot a = createDepot("a", 1, 1);
        Depot b = createDepot("b", 3, 1);
        Depot c = createDepot("c", 5, 1);

        Route route = new Route("r1", "Loop", List.of(a, b, c), 12.0);

        assertEquals(b, route.getNextDepot(a));
        assertEquals(c, route.getNextDepot(b));
        assertEquals(a, route.getNextDepot(c));
    }

    @Test
    void assignAndRemoveUnitKeepsSingleAssignment() {
        Depot a = createDepot("a", 1, 1);
        Depot b = createDepot("b", 3, 1);
        Route route = new Route("r2", "Simple", a, b, 4.0);

        CargoTransport unit = CargoTransport.createStandard("u1", "Cargo");
        route.assignUnit(unit);

        assertEquals(unit, route.getAssignedUnit());
        route.removeUnit(unit);
        assertNull(route.getAssignedUnit());
    }

    private Depot createDepot(String id, int x, int y) {
        Tile tile = new Tile(x, y, "tunnel");
        Hub hub = new Hub("hub-" + id, "Hub " + id, new Tile(x + 20, y + 20, Tile.TileType.ROCK)) { };
        return new Depot("depot-" + id, "Depot " + id, tile, hub);
    }
}
