package team3.model;

import java.util.List;

/**
 * A forge produces tools but requires ore as input.
 * A forge occupies at least a 2x2 tile area and only produces when sufficient ore is available.
 */
public class Forge extends IndustrialFacility {
    private float storedOre;

    public Forge(String id, String name, List<Tile> tiles, float productionRate, float initialOre) {
        super(id, name, tiles, productionRate, GoodType.TOOLS);
        this.storedOre = Math.max(0f, initialOre);
    }

    public float getStoredOre() {
        return storedOre;
    }

    public void addOre(float amount) {
        if (amount > 0f) {
            storedOre += amount;
        }
    }

    public boolean hasEnoughOre() {
        return getProductionRate() > 0f && storedOre >= getProductionRate();
    }

    public void consumeOre(float amount) {
        if (amount <= 0f) {
            return;
        }

        storedOre = Math.max(0f, storedOre - amount);
    }

    @Override
    public Good produce() {
        if (!hasEnoughOre()) {
            return null;
        }

        consumeOre(getProductionRate());
        return super.produce();
    }
}
