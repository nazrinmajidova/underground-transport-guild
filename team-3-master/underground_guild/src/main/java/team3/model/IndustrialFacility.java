package team3.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A facility that produces and stores goods over time.
 * Industrial facilities occupy at least a 2x2 tile area.
 */
public abstract class IndustrialFacility extends Hub {
    private float productionRate;
    private final List<Good> storedGoods = new ArrayList<>();
    private final GoodType producedGoodType;

    protected IndustrialFacility(String id, String name, List<Tile> tiles,
                                 float productionRate, GoodType producedGoodType) {
        super(id, name, tiles);

        if (tiles == null || tiles.size() < 4) {
            throw new IllegalArgumentException("Industrial facilities must occupy at least a 2x2 tile area.");
        }

        this.productionRate = productionRate;
        this.producedGoodType = producedGoodType;
    }

    public float getProductionRate() {
        return productionRate;
    }

    public void setProductionRate(float productionRate) {
        this.productionRate = productionRate;
    }

    public GoodType getProducedGoodType() {
        return producedGoodType;
    }

    public String getProducedGood() {
        return producedGoodType == null ? "Unknown" : producedGoodType.name();
    }

    public List<Good> getStoredGoods() {
        return Collections.unmodifiableList(storedGoods);
    }

    public Good produce() {
        if (producedGoodType == null || productionRate <= 0f) {
            return null;
        }

        Good produced = new Good(producedGoodType, productionRate);
        storedGoods.add(produced);
        return produced;
    }

    public Good loadGoods(float amount) {
        if (amount <= 0f || storedGoods.isEmpty()) {
            return null;
        }

        float remaining = amount;
        float totalExtracted = 0f;

        while (remaining > 0f && !storedGoods.isEmpty()) {
            Good top = storedGoods.get(0);
            float topAmount = (float) top.getAmount();

            if (topAmount <= remaining) {
                totalExtracted += topAmount;
                remaining -= topAmount;
                storedGoods.remove(0);
            } else {
                totalExtracted += remaining;
                top.removeAmount(remaining);
                remaining = 0f;
            }
        }

        if (totalExtracted <= 0f) {
            return null;
        }

        return new Good(producedGoodType, totalExtracted);
    }
}
