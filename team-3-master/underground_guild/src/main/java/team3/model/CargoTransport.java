package team3.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A transport unit that carries goods between depots.
 * Supports loading and unloading goods with capacity and type restrictions.
 */
public class CargoTransport extends TransportUnit {
    private String variant;
    private final List<Good> cargo;
    private final List<GoodType> acceptedGoodTypes;

    /**
     * Constructs a CargoTransport with specific properties.
     */
    public CargoTransport(String id, String name, double speed, int capacity,
                          double maintenanceCost, String variant, List<GoodType> acceptedGoodTypes) {
        super(id, name, speed, capacity, maintenanceCost);
        this.variant = variant;
        this.cargo = new ArrayList<>();
        this.acceptedGoodTypes = new ArrayList<>(acceptedGoodTypes);
    }

    /**
     * Loads goods from a depot based on capacity and accepted types.
     */
    public void loadGoods(Depot depot){
        if (depot == null) return;

        List<Good> depotGoods = depot.getStoredGoods();
        if (depotGoods.isEmpty()) return;

        List<Good> copy = new ArrayList<>(depotGoods);

        for (Good good : copy) {
            if (cargo.size() >= getCapacity()) break;

            if (acceptedGoodTypes.contains(good.getType())) {
                cargo.add(good);
                depot.removeGood(good); 
            }
        }
    }

    /**
     * Unloads all cargo into the depot.
     */
    public void unloadGoods(Depot depot) {
        unloadDemandedGoods(depot);
    }

    /**
     * Unloads cargo and fulfills settlement demand when possible.
     * Returns the total delivered amount that counted as settlement delivery.
     */
    public float unloadDemandedGoods(Depot depot) {
        if (depot == null || cargo.isEmpty()) {
            return 0f;
        }

        float deliveredAmount = 0f;
        List<Good> remainingGoods = new ArrayList<>();

        if (depot.getHub() instanceof Settlement settlement) {
            Map<GoodType, Float> demand = settlement.getGoodsDemand();
            for (Good good : cargo) {
                float demandForType = demand.getOrDefault(good.getType(), 0f);
                float fulfilled = Math.min(demandForType, good.getAmount());
                if (fulfilled > 0f) {
                    settlement.receiveGoods(good.getType(), fulfilled);
                    deliveredAmount += fulfilled;
                }

                float leftover = good.getAmount() - fulfilled;
                if (leftover > 0f) {
                    remainingGoods.add(new Good(good.getType(), leftover));
                }
            }
        } else {
            remainingGoods.addAll(cargo);
        }

        if (!remainingGoods.isEmpty()) {
            depot.addGoods(remainingGoods);
        }
        cargo.clear();
        return deliveredAmount;
    }
    
    /**
     * Creates a STANDARD cargo transport.
     */
    public static CargoTransport createStandard(String id, String name) {
        return new CargoTransport(
                id,
                name,
                5.0,
                40,   
                10.0,
                "STANDARD",
                List.of(GoodType.FOOD, GoodType.ORE, GoodType.TOOLS)
        );
    }

    public static CargoTransport createFast(String id, String name) {
        return new CargoTransport(
                id,
                name,
                10.0,
                20,    
                14.0,
                "FAST",
                List.of(GoodType.FOOD, GoodType.ORE, GoodType.TOOLS)
        );
    }

    public String getVariant(){ 
        return variant; 
    }

    public List<Good> getCargo(){
        return Collections.unmodifiableList(cargo);
    }

    public double getCurrentCargoWeight() {
        return cargo.stream().mapToDouble(Good::getAmount).sum();
    }
}
