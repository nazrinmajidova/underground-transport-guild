package team3.model;

import java.io.Serializable;

/**
 * Represents a transportable good with a specific type and amount.
 */
public class Good implements Serializable {
    private GoodType type;
    private float amount;
    // private final String id;
    // private String type;
    // private double weight;
    // private double value;
    // private IndustrialFacility source;
    // private Settlement destination;


    /**
     * Constructs a Good with a given type and amount.
     */
    public Good(GoodType type, float amount) {
        this.type = type;
        this.amount = amount;
    }
    
    /**
     * Returns the type of the good.
     */
    public GoodType getType() {
        return type;
    }

    /**
     * Returns the amount of the good.
     */
    public float getAmount() {
        return amount;
    }

    /**
     * Increases the amount of the good.
     */
    public void addAmount(float value) {
        this.amount += value;
    }

    /**
     * Decreases the amount of the good.
     */
    public void removeAmount(float value) {
        this.amount -= value;
        if (this.amount < 0) {
            this.amount = 0;
        }
    }
}
