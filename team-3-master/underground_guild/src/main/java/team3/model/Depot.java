package team3.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A depot is a loading and unloading stop where transport units interact with a hub.
 * Depots store goods and waiting passengers, and register routes that pass through them.
 */
public class Depot extends Hub {
    private final Hub hub;
    private final List<Route> routes = new ArrayList<>();
    private final List<Good> storedGoods = new ArrayList<>();
    private int waitingPassengers;

    public Depot(String id, String name, Tile tile, Hub hub) {
        super(id, name, tile);
        this.hub = Objects.requireNonNull(hub, "hub must not be null");
        this.hub.addDepot(this);
    }

    public Hub getHub() {
        return hub;
    }

    public List<Route> getRoutes() {
        return Collections.unmodifiableList(routes);
    }

    public List<Good> getStoredGoods() {
        return Collections.unmodifiableList(storedGoods);
    }

    public int getWaitingPassengers() {
        return waitingPassengers;
    }

    public void addRoute(Route route) {
        if (route != null && !routes.contains(route)) {
            routes.add(route);
        }
    }

    @Override
    public void removeRoute(Route route) {
        routes.remove(route);
    }

    public void loadGoods(TransportUnit unit) {
        if (unit instanceof CargoTransport cargoUnit) {
            cargoUnit.loadGoods(this);
        }
    }

    public void unloadGoods(TransportUnit unit) {
        if (unit instanceof CargoTransport cargoUnit) {
            cargoUnit.unloadGoods(this);
        }
    }

     /**
     * Adds goods to the depot (used by CargoTransport).
     */
    public void addGoods(List<Good> goods) {
        if (goods != null && !goods.isEmpty()) {
            storedGoods.addAll(goods);
        }
    }

    /**
     * Removes a specific good from depot (used by CargoTransport).
     */
    public void removeGood(Good good) {
        storedGoods.remove(good);
    }

    public void loadPassengers(TransportUnit unit) {
        if (!(unit instanceof PassengerTransport) || waitingPassengers <= 0) {
            return;
        }

        PassengerTransport passengerUnit = (PassengerTransport) unit;
        passengerUnit.boardPassengers(this);
    }

    public void unloadPassengers(TransportUnit unit) {
        if (!(unit instanceof PassengerTransport)) {
            return;
        }

        PassengerTransport passengerUnit = (PassengerTransport) unit;
        passengerUnit.disembarkPassengers(this);
    }

    public void modifyWaitingPassengers() {
        if (this.hub instanceof Settlement settlement) {
            this.waitingPassengers = (int) settlement.getPassengerDemand();
        }
    }

    public void addWaitingPassengers(int count) {
        if (count > 0) {
            this.waitingPassengers += count;
        }
    }
}
