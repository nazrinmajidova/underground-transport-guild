package team3.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A populated area that generates passengers and demands goods.
 * Settlements occupy at least a 3×3 tile area and manage internal demand.
 * Passenger demand grows each tick and is reduced when passengers depart.
 * Population reflects arrivals and departures.
 */
public class Settlement extends Hub {
    private static final float PASSENGER_DEMAND_GROWTH_PER_CAPITA = 0.02f;
    private static final float GOODS_BASE_DEMAND_FACTOR = 0.2f;
    private static final float GOODS_RANDOM_VARIATION_FACTOR = 0.06f;
    private static final float MAX_DEMAND_DELTA_PER_TICK_FACTOR = 0.03f;

    private int population;
    private final Map<GoodType, Float> goodsDemand = new EnumMap<>(GoodType.class);
    private float passengerDemand;

    public Settlement(String id, String name, List<Tile> tiles, int population) {
        super(id, name, tiles);
        if (tiles == null || tiles.size() < 9) {
            throw new IllegalArgumentException("Settlement must occupy at least a 3x3 tile area.");
        }
        this.population = population;
        initializeDemand();
    }

    public int getPopulation() { return population; }
    public Map<GoodType, Float> getGoodsDemand() { return Map.copyOf(goodsDemand); }
    public float getPassengerDemand() { return passengerDemand; }

    public void setPopulation(int population) {
        this.population = population;
    }

    /**
     * Called when goods are delivered to this settlement.
     * Reduces demand for that good type.
     */
    public void receiveGoods(GoodType type, float amount) {
        if (type == null || amount <= 0f) return;
        float current = goodsDemand.getOrDefault(type, 0f);
        goodsDemand.put(type, Math.max(0f, current - amount));
    }

    /**
     * Called when passengers arrive at this settlement.
     * Population grows to reflect new arrivals.
     */
    public void receivePassengers(int amount) {
        if (amount <= 0) return;
        this.population += amount;
    }

    /**
     * Called when passengers board a transport at this settlement.
     * Reduces demand and population to reflect departures.
     */
    public void departPassengers(int amount) {
        if (amount <= 0) return;
        passengerDemand = Math.max(0f, passengerDemand - amount);
        this.population = Math.max(0, population - amount);
    }

    /**
     * Each tick, passenger demand grows based on current population.
     * Goods demand drifts toward a population-based target with slight randomness.
     */
    public void updateDemandTick(Random random) {
        Random rng = random == null ? new Random() : random;

        // passenger demand only grows, reduced only by departPassengers()
        passengerDemand += population * PASSENGER_DEMAND_GROWTH_PER_CAPITA
            * (0.5f + rng.nextFloat() * 0.5f);

        // goods demand drifts toward a target
        float maxStep = Math.max(0.5f, population * MAX_DEMAND_DELTA_PER_TICK_FACTOR);
        for (GoodType type : GoodType.values()) {
            float current = goodsDemand.getOrDefault(type, 0f);
            float target = population * GOODS_BASE_DEMAND_FACTOR
                + population * GOODS_RANDOM_VARIATION_FACTOR * rng.nextFloat();
            goodsDemand.put(type, approach(current, target, maxStep));
        }
    }

    private void initializeDemand() {
        for (GoodType type : GoodType.values()) {
            goodsDemand.put(type, population * GOODS_BASE_DEMAND_FACTOR);
        }
        passengerDemand = 0f;
    }

    private float approach(float current, float target, float maxStep) {
        float delta = target - current;
        if (Math.abs(delta) <= maxStep) return Math.max(0f, target);
        return Math.max(0f, current + Math.signum(delta) * maxStep);
    }
}