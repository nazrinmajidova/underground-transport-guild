package team3.model;

import java.io.Serializable;
import java.util.List;

/**
 * Abstract base for all transport units (passenger or cargo).
 * Handles shared movement, routing, and maintenance logic.
 */
public abstract class TransportUnit implements Serializable {
    private final String id;
    private String name;
    private double speed;
    private int capacity;
    private double maintenanceCost;
    private Tile currentTile;
    private Route assignedRoute;
    private double distanceTravelled;
    private boolean movingToDestination = true;
    private int currentStopIndex;
    private boolean arrivedAtStop;
    private int upgradeLevel;

    protected TransportUnit(String id, String name, double speed, int capacity, double maintenanceCost) {
        this.id = id;
        this.name = name;
        this.speed = speed;
        this.capacity = capacity;
        this.maintenanceCost = maintenanceCost;
        this.distanceTravelled = 0;
        this.currentStopIndex = 0;
        this.arrivedAtStop = false;
        this.upgradeLevel = 0;
    }

    /**
     * Moves the unit along its assigned route.
     * When reaching the end, it reverses direction.
     */
    public void move(){
        if(assignedRoute == null) return;

        double currentSpeed = speed;
        if (currentTile != null) {
            currentSpeed *= currentTile.getSpeedModifier();
        }

        distanceTravelled += currentSpeed;

        double routeDistance = assignedRoute.getDistance();

        if (distanceTravelled >= routeDistance) {
            distanceTravelled = routeDistance;

            movingToDestination = !movingToDestination;
            distanceTravelled = 0;
        }
    }

    /**
     * Loads cargo/passengers at a depot.
     * Default implementation delegates to depot.
     * Subclasses may override.
     */
    public void loadCargo (Depot depot){
       if (depot != null) {
            depot.loadGoods(this);
            depot.loadPassengers(this);
        } 
    }

    /**
     * Unloads cargo/passengers at a depot.
     * Default implementation delegates to depot.
     * Subclasses may override.
     */
    public void unloadCargo(Depot depot){
        if (depot != null) {
            depot.unloadGoods(this);
            depot.unloadPassengers(this);
        }
    }

    /**
     * Calculates maintenance cost (can be extended later).
     */
    public double calculateMaintenanceCost(){
        if (currentTile != null) {
            return maintenanceCost * currentTile.getMaintenanceCostModifier();
        }
        return maintenanceCost;
    }

    /**
     * Returns the next depot based on direction.
     */
    public Depot getNextStop(){
        if (assignedRoute == null || assignedRoute.getDepots().isEmpty()) return null;

        int nextIndex = (normalizeStopIndex(currentStopIndex) + 1) % assignedRoute.getDepots().size();
        return assignedRoute.getDepots().get(nextIndex);
    }

    public Depot getCurrentStop() {
        if (assignedRoute == null || assignedRoute.getDepots().isEmpty()) {
            return null;
        }
        int normalized = normalizeStopIndex(currentStopIndex);
        return assignedRoute.getDepots().get(normalized);
    }

    public void advanceToNextStop() {
        if (assignedRoute == null || assignedRoute.getDepots().isEmpty()) {
            return;
        }

        int depotCount = assignedRoute.getDepots().size();
        currentStopIndex = (normalizeStopIndex(currentStopIndex) + 1) % depotCount;
        movingToDestination = (currentStopIndex == 0);
        distanceTravelled = 0;
        arrivedAtStop = true;
    }

    public boolean consumeArrivedAtStop() {
        boolean arrived = arrivedAtStop;
        arrivedAtStop = false;
        return arrived;
    }

    private int normalizeStopIndex(int index) {
        if (assignedRoute == null || assignedRoute.getDepots().isEmpty()) {
            return 0;
        }
        int size = assignedRoute.getDepots().size();
        int normalized = index % size;
        if (normalized < 0) {
            normalized += size;
        }
        return normalized;
    }

    /**
     * Updates the current tile based on the distance travelled along the route path.
     * This ensures speed and maintenance modifiers are applied accurately.
     */
    public void updateCurrentTile(List<Tile> routePath) {
        if (routePath == null || routePath.isEmpty() || assignedRoute == null || assignedRoute.getDistance() <= 0) {
            return;
        }

        List<Tile> directionalPath = new java.util.ArrayList<>(routePath);
        if (!movingToDestination) {
            java.util.Collections.reverse(directionalPath);
        }

        double progress = Math.max(0.0, Math.min(1.0, distanceTravelled / assignedRoute.getDistance()));
        double pathProgress = progress * (directionalPath.size() - 1);
        int index = (int) Math.floor(pathProgress);
        
        index = Math.min(index, directionalPath.size() - 1);
        this.currentTile = directionalPath.get(index);
    }

    public void upgrade(){
        setUpgradeLevel(getUpgradeLevel() + 1);
        setSpeed(getSpeed() + 2.0 / getUpgradeLevel());
        setCapacity(getCapacity() + (int) (20.0/getUpgradeLevel()));
        setMaintenanceCost(getMaintenanceCost() + getUpgradeLevel() * 2.0);
    }

    public String getId(){ 
        return id; 
    }
    
    public String getName(){ 
        return name; 
    }
    
    public double getSpeed(){ 
        return speed; 
    }

    public int getCapacity(){ 
        return capacity; 
    }
    
    public double getMaintenanceCost(){ 
        return maintenanceCost; 
    }
    
    public Tile getCurrenTile(){
        return currentTile;
    }
    
    public Route getAssignedRoute(){ 
        return assignedRoute; 
    }

    public double getDistanceTravelled(){
        return distanceTravelled;
    }

    public boolean isMovingToDestination() {
        return movingToDestination;
    }

    public int getCurrentStopIndex() {
        return currentStopIndex;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setAssignedRoute(Route route){ 
        this.assignedRoute = route;
        this.currentStopIndex = 0;
        this.arrivedAtStop = false;
        this.distanceTravelled = 0;
        this.movingToDestination = true;
    }
    
    public void setSpeed(double speed){ 
        this.speed = speed;
    }
    
    public void setCapacity(int capacity){ 
        this.capacity = capacity;
    }
    
    public void setCurrentTile(Tile tile) {
        this.currentTile = tile;
    }

    public void setMaintenanceCost(double cost){ 
        this.maintenanceCost = cost; 
    }

    public void setUpgradeLevel(int upgradeLevel) {
        this.upgradeLevel = Math.max(0, upgradeLevel);
    }
}
