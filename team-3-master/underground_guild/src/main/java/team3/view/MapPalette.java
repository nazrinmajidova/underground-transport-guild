package team3.view;

import java.util.List;
import javafx.scene.paint.Color;
import team3.model.Depot;
import team3.model.Hub;
import team3.model.IndustrialFacility;
import team3.model.Settlement;
import team3.model.Tile;

/**
 * Centralizes the color palette used for map rendering.
 */
public final class MapPalette {
    private static final List<Color> ROUTE_COLORS = List.of(
        Color.web("#ff7b72"),
        Color.web("#56d4c6"),
        Color.web("#fbbf24"),
        Color.web("#60a5fa")
    );

    private MapPalette() {
    }

    public static Color colorForTile(Tile tile) {
        if (tile.getOccupant() instanceof Settlement) {
            return Color.web("#4f8ee8");
        }
        if (tile.getOccupant() instanceof Depot) {
            return Color.web("#e3a93b");
        }
        if (tile.getOccupant() instanceof IndustrialFacility facility) {
            if (facility.getName().startsWith("Mine")) {
                return Color.web("#7a5b44");
            }
            if (facility.getName().startsWith("Farm")) {
                return Color.web("#4ca66b");
            }
            if (facility.getName().startsWith("Forge")) {
                return Color.web("#8b8f97");
            }
            return Color.web("#5c6f7b");
        }
        if (tile.isHasTunnel()) {
            return Color.web("#c48f3a");
        }
        return switch (tile.getTileType()) {
            case ROCK -> Color.web("#233142");
            case WATER -> Color.web("#2b7a9a");
            case MOUNTAIN -> Color.web("#5f6d7a");
            case FOREST -> Color.web("#3c7a4d");
        };
    }

    public static Color gridLineColor() {
        return Color.web("#0b1220", 0.55);
    }

    public static Color hubOutlineColor(Hub hub) {
        if (hub instanceof Settlement) {
            return Color.web("#dbeafe");
        }
        if (hub instanceof Depot) {
            return Color.web("#fef3c7");
        }
        if (hub instanceof IndustrialFacility facility) {
            if (facility.getName().startsWith("Mine")) {
                return Color.web("#ead6c2");
            }
            if (facility.getName().startsWith("Farm")) {
                return Color.web("#d1fae5");
            }
            if (facility.getName().startsWith("Forge")) {
                return Color.web("#e2e8f0");
            }
        }
        return Color.web("#f5f7fa");
    }

    public static Color hubAccentColor(Hub hub) {
        if (hub instanceof Settlement) {
            return Color.web("#1d4ed8");
        }
        if (hub instanceof Depot) {
            return Color.web("#d97706");
        }
        if (hub instanceof IndustrialFacility facility) {
            if (facility.getName().startsWith("Mine")) {
                return Color.web("#7c4a21");
            }
            if (facility.getName().startsWith("Farm")) {
                return Color.web("#15803d");
            }
            if (facility.getName().startsWith("Forge")) {
                return Color.web("#475569");
            }
        }
        return Color.web("#64748b");
    }

    public static Color routeColor(int routeIndex, boolean selected) {
        Color baseColor = ROUTE_COLORS.get(Math.floorMod(routeIndex, ROUTE_COLORS.size()));
        return selected ? baseColor : baseColor.deriveColor(0, 0.98, 0.96, 0.8);
    }

    public static Color cargoUnitColor() {
        return Color.web("#38bdf8");
    }

    public static Color passengerUnitColor() {
        return Color.web("#22c55e");
    }

    public static Color selectedHubGlow() {
        return Color.web("#93c5fd", 0.5);
    }

    public static Color routePreviewValidColor() {
        return Color.web("#f59e0b");
    }

    public static Color routePreviewInvalidColor() {
        return Color.web("#ef4444");
    }
}
