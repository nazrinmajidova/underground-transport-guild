package team3.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a transport route between two or more hubs.
 */
public class Route implements Serializable {
    private final String id;
    private String name;
    private double distance;
    private final List<Depot> depots = new ArrayList<>();
    private TransportUnit assignedUnit;

    /**
     * Constructs a new Route.
     * Initializes the circular sequence with the origin and destination depots.
     */
    public Route(String id, String name, Hub origin, Hub destination, double distance) {
        this.id = id;
        this.name = name;
        this.distance = distance;

        if (origin instanceof Depot depot) {
            this.depots.add(depot);
        }
        if (destination instanceof Depot depot && !this.depots.contains(depot)) {
            this.depots.add(depot);
        }
    }

    /**
     * Constructs a new Route from a specific sequence of depots.
     */
    public Route(String id, String name, List<Depot> sequence, double distance) {
        this.id = id;
        this.name = name;
        this.distance = distance;
        if (sequence != null) {
            this.depots.addAll(sequence);
        }
    }

    /**
     * Gets the next depot in the circular sequence.
     * Wraps around to the first depot if the current depot is the last one in the list.
     * Returns the next Depot in the sequence, or the first depot if the current is not found.
     */
    public Depot getNextDepot(Depot currentDepot) {
        if (depots.isEmpty()) {
            return null;
        }
        int currentIndex = depots.indexOf(currentDepot);
        if (currentIndex == -1) {
            return depots.get(0);
        }
        int nextIndex = (currentIndex + 1) % depots.size();
        return depots.get(nextIndex);
    }

    /**
     * Adds a new depot to the end of the route sequence.
     */
    public void addDepot(Depot depot) {
        if (depot != null && !depots.contains(depot)) {
            depots.add(depot);
        }
    }

    /**
     * Removes a depot from the route sequence.
     */
    public void removeDepot(Depot depot) {
        depots.remove(depot);
    }

    public String getId(){ 
        return id; 
    }
    
    public String getName(){ 
        return name; 
    }
    
    public double getDistance(){ 
        return distance;
    }
    
    public void setName(String name){ 
        this.name = name;
    }

    public void setDistance(double distance){ 
        this.distance = distance;
    }

    public List<Depot> getDepots() {
        return Collections.unmodifiableList(depots);
    }

    public TransportUnit getAssignedUnit() {
        return assignedUnit;
    }

    public void setAssignedUnit(TransportUnit assignedUnit) {
        this.assignedUnit = assignedUnit;
    }

    public Hub getOrigin() { 
        return depots.isEmpty() ? null : depots.get(0);
    }
    
    public Hub getDestination() { 
        return depots.size() > 1 ? depots.get(depots.size() - 1) : getOrigin();
    }

    public List<TransportUnit> getAssignedUnits() { 
        return assignedUnit != null ? List.of(assignedUnit) : List.of();
    }
    
    public void assignUnit(TransportUnit unit) { 
        this.assignedUnit = unit;
    }
    
    public void removeUnit(TransportUnit unit) { 
        if (this.assignedUnit != null && this.assignedUnit.equals(unit)) {
            this.assignedUnit = null;
        }
    }
}
