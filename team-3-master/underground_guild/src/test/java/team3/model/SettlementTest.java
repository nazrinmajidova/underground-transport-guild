package team3.model;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SettlementTest {

    @Test
    void demandGrowsGraduallyAfterBeingFulfilled() {
        Settlement settlement = createSettlement(100);

        settlement.receiveGoods(GoodType.FOOD, 1_000f);

        float beforeFood = settlement.getGoodsDemand().get(GoodType.FOOD);
        float beforePassengers = settlement.getPassengerDemand();

        settlement.updateDemandTick(new Random(42));

        float afterFood = settlement.getGoodsDemand().get(GoodType.FOOD);
        float afterPassengers = settlement.getPassengerDemand();

        assertTrue(afterFood > beforeFood);
        assertTrue(afterPassengers > beforePassengers);

        float maxGoodsStep = Math.max(0.5f, settlement.getPopulation() * 0.03f);
        assertTrue(afterFood - beforeFood <= maxGoodsStep + 0.01f);
    }

    @Test
    void unmetDemandIncreasesOverMultipleTicks() {
        Settlement settlement = createSettlement(100);
        settlement.receiveGoods(GoodType.TOOLS, 1_000f);

        for (int tick = 0; tick < 5; tick++) {
            settlement.updateDemandTick(new Random(100 + tick));
        }

        assertTrue(settlement.getGoodsDemand().get(GoodType.TOOLS) > 0f);
    }

    @Test
    void differentRandomSeedsCreateVariedDemandPatterns() {
        Settlement first = createSettlement(120);
        Settlement second = createSettlement(120);

        List<Float> firstPattern = new ArrayList<>();
        List<Float> secondPattern = new ArrayList<>();

        for (int tick = 0; tick < 6; tick++) {
            first.updateDemandTick(new Random(1000 + tick));
            second.updateDemandTick(new Random(2000 + tick));
            firstPattern.add(first.getGoodsDemand().get(GoodType.ORE));
            secondPattern.add(second.getGoodsDemand().get(GoodType.ORE));
        }

        assertNotEquals(firstPattern, secondPattern);
    }

    private Settlement createSettlement(int population) {
        List<Tile> tiles = List.of(
            new Tile(0, 0, Tile.TileType.ROCK), new Tile(1, 0, Tile.TileType.ROCK), new Tile(2, 0, Tile.TileType.ROCK),
            new Tile(0, 1, Tile.TileType.ROCK), new Tile(1, 1, Tile.TileType.ROCK), new Tile(2, 1, Tile.TileType.ROCK),
            new Tile(0, 2, Tile.TileType.ROCK), new Tile(1, 2, Tile.TileType.ROCK), new Tile(2, 2, Tile.TileType.ROCK)
        );
        return new Settlement("settlement-1", "Settlement 1", tiles, population);
    }
}
