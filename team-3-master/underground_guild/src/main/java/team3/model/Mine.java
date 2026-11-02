package team3.model;

import java.util.List;

/**
 * A mine produces ore and stores it until loaded.
 */
public class Mine extends IndustrialFacility {

    public Mine(String id, String name, List<Tile> tiles, float productionRate) {
        super(id, name, tiles, productionRate, GoodType.ORE);
    }
}
