package team3.service;

import java.io.Serializable;

/**
 * Controls the progression of time within the simulation.
 * Allows for pausing and varying the speed of the game logic.
 */
public class SimulationClock implements Serializable {
    private String speed;
    private String previousSpeed;
    private float elapsedTime;
    private long totalTicks;

    /**
     * Initializes a new SimulationClock with normal speed and zero elapsed time.
     */
    public SimulationClock() {
        this.speed = "normal";
        this.previousSpeed = "normal";
        this.elapsedTime = 0.0f;
        this.totalTicks = 0;
    }

    /**
     * Advances the simulation time based on the current speed multiplier.
     * Elapsed time accumulates faster or slower depending on the speed setting.
     */
    public void tick() {
        float multiplier = getSpeedMultiplier();
        elapsedTime += multiplier;
        
        if (!"pause".equals(speed)) {
            totalTicks++;
        }
    }

    /**
     * Sets the current speed of the simulation.
     * Valid values: "pause", "normal", "fast", "very_fast".
     */
    public void setSpeed(String newSpeed) {
        if (newSpeed == null) return;
        
        String formattedSpeed = newSpeed.toLowerCase();
        
        switch (formattedSpeed) {
            case "pause":
            case "normal":
            case "fast":
            case "very_fast":
                if (!"pause".equals(this.speed) && "pause".equals(formattedSpeed)) {
                    this.previousSpeed = this.speed;
                } 
                else if (!"pause".equals(formattedSpeed)) {
                    this.previousSpeed = formattedSpeed;
                }
                this.speed = formattedSpeed;
                break;
            default:
                System.err.println("Invalid speed setting provided: " + newSpeed);
        }
    }

    /**
     * Pauses the simulation clock. Saves the current speed so it can be resumed later.
     */
    public void pause() {
        if (!"pause".equals(this.speed)) {
            this.previousSpeed = this.speed;
            this.speed = "pause";
        }
    }

    /**
     * Resumes the simulation clock at the last active speed before it was paused.
     */
    public void resume() {
        if ("pause".equals(this.speed)) {
            this.speed = this.previousSpeed;
        }
    }

    /**
     * Determines the multiplier based on the current string speed.
     * returns the float multiplier (0.0f to 4.0f)
     */
    private float getSpeedMultiplier() {
        switch (speed) {
            case "normal": return 1.0f;
            case "fast": return 2.0f;
            case "very_fast": return 4.0f;
            case "pause": return 0.0f;
            default: return 1.0f;
        }
    }

    /**
     * returns the current string speed setting
     */
    public String getSpeed() {
        return speed;
    }

    /**
     * returns the total accumulated elapsed time based on speed multipliers
     */
    public float getElapsedTime() {
        return elapsedTime;
    }

    /**
     * returns the total number of unpaused ticks
     */
    public long getTotalTicks() {
        return totalTicks;
    }
}
