package team3.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * A transport unit that carries passengers between depots.
 * Supports boarding and disembarking logic with capacity limits
 * and destination-aware delivery.
 */
public class PassengerTransport extends TransportUnit {
    private String variant;
    private final List<Passenger> passengers = new ArrayList<>();

    public PassengerTransport(String id, String name, double speed, int capacity,
                              double maintenanceCost, String variant) {
        super(id, name, speed, capacity, maintenanceCost);
        this.variant = variant;
    }

    public void boardPassengers(Depot depot) {
        if (depot == null) return;

        int waiting = depot.getWaitingPassengers();
        int space = getCapacity() - passengers.size();
        int boarded = Math.min(waiting, space);

        if (boarded > 0) {
            Settlement origin = (depot.getHub() instanceof Settlement s) ? s : null;
            Settlement destination = findDestinationSettlement(depot);
            for (int i = 0; i < boarded; i++) {
                passengers.add(new Passenger(
                    UUID.randomUUID().toString().substring(0, 8),
                    origin, destination, 0f
                ));
            }
        }
        if (depot.getHub() instanceof Settlement settlement) {
                settlement.departPassengers(boarded);
                depot.modifyWaitingPassengers();
        }
    }

    public void disembarkPassengers(Depot depot) {
        disembarkDeliveredPassengers(depot);
    }

    public int disembarkDeliveredPassengers(Depot depot) {
        if (depot == null || passengers.isEmpty()) return 0;

        int delivered = 0;
        if (depot.getHub() instanceof Settlement settlement) {
            Iterator<Passenger> it = passengers.iterator();
            while (it.hasNext()) {
                Passenger p = it.next();
                if (p.getDestination() == null || p.getDestination().equals(settlement)) {
                    it.remove();
                    delivered++;
                }
            }
            if (delivered > 0) {
                settlement.receivePassengers(delivered);
            }
        }
        return delivered;
    }

    private Settlement findDestinationSettlement(Depot boardingDepot) {
        if (getAssignedRoute() == null) return null;
        for (Depot depot : getAssignedRoute().getDepots()) {
            if (depot != boardingDepot && depot.getHub() instanceof Settlement s) {
                return s;
            }
        }
        return null;
    }

    public static PassengerTransport createStandard(String id, String name) {
        return new PassengerTransport(
                id,
                name,
                6.0,
                50,
                8.0,
                "STANDARD"
        );
    }

    public static PassengerTransport createFast(String id, String name) {
        return new PassengerTransport(
                id,
                name,
                12.0,
                30,
                12.0,
                "FAST"
        );
    }

    public String getVariant() {
        return variant;
    }

    public int getPassengersOnBoard() {
        return passengers.size();
    }

    public List<Passenger> getPassengers() {
        return Collections.unmodifiableList(passengers);
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }
}
