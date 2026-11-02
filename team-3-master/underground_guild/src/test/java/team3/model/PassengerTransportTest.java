package team3.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class PassengerTransportTest {

    @Test
    void boardsPassengersUpToCapacityAtNonSettlementDepot() {
        PassengerTransport unit = PassengerTransport.createStandard("p1", "Bus");
        Depot depot = createDepot(false);
        depot.addWaitingPassengers(70);

        unit.boardPassengers(depot);

        assertEquals(50, unit.getPassengersOnBoard());
    }

    @Test
    void passengersDisembarkOnlyAtMatchingDestination() {
        Settlement settlementA = createSettlement("sA", 10, 10);
        Settlement settlementB = createSettlement("sB", 20, 20);
        Depot depotA = new Depot("dA", "Depot A", new Tile(5, 5, "tunnel"), settlementA);
        Depot depotB = new Depot("dB", "Depot B", new Tile(6, 6, "tunnel"), settlementB);

        Route route = new Route("r1", "Route", List.of(depotA, depotB), 5.0);
        PassengerTransport unit = PassengerTransport.createStandard("p2", "City Bus");
        unit.setAssignedRoute(route);

        depotA.addWaitingPassengers(12);
        unit.boardPassengers(depotA);

        assertEquals(12, unit.getPassengersOnBoard());

        int deliveredAtA = unit.disembarkDeliveredPassengers(depotA);
        assertEquals(0, deliveredAtA);
        assertEquals(12, unit.getPassengersOnBoard());

        int deliveredAtB = unit.disembarkDeliveredPassengers(depotB);
        assertEquals(12, deliveredAtB);
        assertEquals(0, unit.getPassengersOnBoard());
    }

    @Test
    void fastVariantHasLowerCapacityThanStandard() {
        PassengerTransport standard = PassengerTransport.createStandard("s1", "Standard");
        PassengerTransport fast = PassengerTransport.createFast("f1", "Fast");

        assertTrue(fast.getCapacity() < standard.getCapacity());
        assertTrue(fast.getSpeed() > standard.getSpeed());
        assertTrue(fast.getMaintenanceCost() > standard.getMaintenanceCost());
    }

    @Test
    void disembarkDoesNothingAtNonSettlementDepot() {
        PassengerTransport unit = PassengerTransport.createStandard("p3", "Shuttle");
        Depot depot = createDepot(false);
        depot.addWaitingPassengers(20);

        unit.boardPassengers(depot);
        assertEquals(20, unit.getPassengersOnBoard());

        int delivered = unit.disembarkDeliveredPassengers(depot);

        assertEquals(0, delivered);
        assertEquals(20, unit.getPassengersOnBoard());
    }

    @Test
    void passengersWithNullDestinationDisembarkAtAnySettlement() {
        Settlement settlement = createSettlement("s1", 10, 10);
        Depot depot = new Depot("d1", "Settlement Depot", new Tile(5, 5, "tunnel"), settlement);

        PassengerTransport unit = PassengerTransport.createStandard("p4", "City Bus");
        depot.addWaitingPassengers(8);
        unit.boardPassengers(depot);

        assertEquals(8, unit.getPassengersOnBoard());

        int delivered = unit.disembarkDeliveredPassengers(depot);

        assertEquals(8, delivered);
        assertEquals(0, unit.getPassengersOnBoard());
        assertTrue(settlement.getPopulation() >= 100);
    }

    private Depot createDepot(boolean settlementHub) {
        Tile tile = new Tile(1, 1, "tunnel");
        Hub hub = settlementHub ? createSettlement("s1", 10, 10) : new Hub("h1", "Hub", new Tile(2, 2, Tile.TileType.ROCK)) { };
        return new Depot("d1", "Depot", tile, hub);
    }

    private Settlement createSettlement(String id, int startX, int startY) {
        List<Tile> tiles = List.of(
            new Tile(startX, startY, Tile.TileType.ROCK), new Tile(startX + 1, startY, Tile.TileType.ROCK), new Tile(startX + 2, startY, Tile.TileType.ROCK),
            new Tile(startX, startY + 1, Tile.TileType.ROCK), new Tile(startX + 1, startY + 1, Tile.TileType.ROCK), new Tile(startX + 2, startY + 1, Tile.TileType.ROCK),
            new Tile(startX, startY + 2, Tile.TileType.ROCK), new Tile(startX + 1, startY + 2, Tile.TileType.ROCK), new Tile(startX + 2, startY + 2, Tile.TileType.ROCK)
        );
        return new Settlement(id, "Settlement " + id, tiles, 100);
    }
}
