package team3.model;

/**
 * Represents a passenger travelling between two hubs.
 */
public class Passenger {
    private final String id;
    private Settlement origin;
    private Settlement destination;
    private boolean isInTransit;
    private float waitTime;

    public Passenger(String id, Settlement origin, Settlement destination, float waitTime) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.isInTransit = false;
        this.waitTime = waitTime;
    }

    public String getId(){ 
        return id; 
    }
    
    public Settlement getOrigin(){ 
        return origin; 
    }
    
    public Settlement getDestination(){ 
        return destination; 
    }
    
    public float getWaitTime(){ 
        return waitTime; 
    }

    public void incrementWaitTime(float delta) {
        if (delta > 0f) {
            this.waitTime += delta;
        }
    }
    
    public boolean isInTransit(){ 
        return isInTransit; 
    }
    
    public void setInTransit(boolean inTransit){ 
        this.isInTransit = inTransit; 
    }
}