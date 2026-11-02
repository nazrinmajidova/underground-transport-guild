package team3.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

public class CargoTransportTest {
    @Test
    void loadsGoodsUpToCapacity() {
        CargoTransport transport = CargoTransport.createStandard("c1", "Cargo");

        Depot depot = createDepotWithGoods(10);

        transport.loadGoods(depot);

        assertTrue(transport.getCargo().size() <= 40);
    }

    @Test
    void unloadsGoodsIntoDepot() {
        CargoTransport transport = CargoTransport.createStandard("c1", "Cargo");
        Depot depot = createDepotWithGoods(5);

        transport.loadGoods(depot);
        transport.unloadGoods(depot);

        assertTrue(transport.getCargo().isEmpty());
        assertTrue(depot.getStoredGoods().size() >= 5);
    }

    @Test
    void fastVariantHasLowerCapacityThanStandard() {
        CargoTransport standard = CargoTransport.createStandard("cs1", "Standard Cargo");
        CargoTransport fast = CargoTransport.createFast("cf1", "Fast Cargo");

        assertTrue(fast.getCapacity() < standard.getCapacity());
        assertTrue(fast.getSpeed() > standard.getSpeed());
        assertTrue(fast.getMaintenanceCost() > standard.getMaintenanceCost());
    }

    private Depot createDepotWithGoods(int amount) {
        Tile tile = new Tile(0, 0, Tile.TileType.ROCK);
        Hub hub = new Hub("h1", "TestHub", tile) {
            @Override
            public void addDepot(Depot depot) {}
        };
        
        Depot depot = new Depot("d1", "Depot", tile, hub);

        for (int i = 0; i < amount; i++) {
            depot.addGoods(List.of(new Good(GoodType.FOOD, 1)));
        }

        return depot;
    }
}
