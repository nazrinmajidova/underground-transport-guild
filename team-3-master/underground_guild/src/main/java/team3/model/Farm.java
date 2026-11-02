package team3.model;

import java.util.List;

/**
 * A farm produces food and stores it until loaded.
 */
public class Farm extends IndustrialFacility {

    public Farm(String id, String name, List<Tile> tiles, float productionRate) {
        super(id, name, tiles, productionRate, GoodType.FOOD);
    }
}
