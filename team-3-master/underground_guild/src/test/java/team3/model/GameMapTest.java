package team3.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class GameMapTest {
    private static final GameMap.GenerationConfig EMPTY_GENERATION =
        new GameMap.GenerationConfig(0, 0, 0, 0, false);

    @Test
    void returnsNullForOutOfBoundsTiles() {
        GameMap map = new GameMap(4, 4, EMPTY_GENERATION);

        assertNull(map.getTileAt(-1, 0));
        assertNull(map.getTileAt(4, 0));
        assertNull(map.getTileAt(0, 4));
    }

    @Test
    void generatesRockTilesByDefault() {
        GameMap map = new GameMap(3, 3, EMPTY_GENERATION);

        Tile tile = map.getTileAt(1, 1);
        assertNotNull(tile);
        assertTrue(tile.getTileType() == Tile.TileType.ROCK);
        assertFalse(tile.isHasTunnel());
    }

    @Test
    void buildsTunnelOnlyOnFreeRockTiles() {
        GameMap map = new GameMap(3, 3, EMPTY_GENERATION);

        assertTrue(map.buildTunnel(1, 1));
        assertTrue(map.getTileAt(1, 1).isHasTunnel());
        assertFalse(map.buildTunnel(1, 1));
    }

    @Test
    void checksTunnelConnectivityBetweenHubs() {
        GameMap map = new GameMap(5, 3, EMPTY_GENERATION);
        Hub anchor = new Settlement("s1", "Anchor", footprint(map, 0, 0, 3, 3), 120);
        Depot left = new Depot("d1", "Left", map.getTileAt(0, 1), anchor);
        Depot right = new Depot("d2", "Right", map.getTileAt(4, 1), anchor);
        map.addHub(left);
        map.addHub(right);

        map.buildTunnel(1, 1);
        map.buildTunnel(2, 1);
        map.buildTunnel(3, 1);

        assertTrue(map.isConnected(left, right));
    }

    @Test
    void rejectsConnectivityWhenTunnelChainIsBroken() {
        GameMap map = new GameMap(5, 3, EMPTY_GENERATION);
        Hub anchor = new Settlement("s1", "Anchor", footprint(map, 0, 0, 3, 3), 120);
        Depot left = new Depot("d1", "Left", map.getTileAt(0, 1), anchor);
        Depot right = new Depot("d2", "Right", map.getTileAt(4, 1), anchor);
        map.addHub(left);
        map.addHub(right);

        map.buildTunnel(1, 1);
        map.buildTunnel(3, 1);

        assertFalse(map.isConnected(left, right));
    }

    @Test
    void findsTunnelPathBetweenConnectedDepots() {
        GameMap map = new GameMap(5, 3, EMPTY_GENERATION);
        Hub anchor = new Settlement("s1", "Anchor", footprint(map, 0, 0, 3, 3), 120);
        Depot left = new Depot("d1", "Left", map.getTileAt(0, 1), anchor);
        Depot right = new Depot("d2", "Right", map.getTileAt(4, 1), anchor);
        map.addHub(left);
        map.addHub(right);

        map.buildTunnel(1, 1);
        map.buildTunnel(2, 1);
        map.buildTunnel(3, 1);

        assertTrue(map.findTunnelPath(left, right).size() == 3);
    }

    @Test
    void findsBuildableTunnelPathAcrossRockBetweenDepots() {
        GameMap map = new GameMap(6, 4, EMPTY_GENERATION);
        Hub anchor = new Settlement("s1", "Anchor", footprint(map, 0, 0, 3, 3), 120);
        Depot left = new Depot("d1", "Left", map.getTileAt(0, 1), anchor);
        Depot right = new Depot("d2", "Right", map.getTileAt(5, 1), anchor);
        map.addHub(left);
        map.addHub(right);

        assertFalse(map.findBuildableTunnelPath(left, right).isEmpty());
        assertNotEquals(map.findBuildableTunnelPath(left, right).getFirst(), left.getTile());
    }

    @Test
    void generatesConfiguredSettlementAndFacilityCounts() {
        GameMap map = new GameMap(50, 50, GameMap.DEFAULT_GENERATION_CONFIG, new java.util.Random(7));

        long settlementCount = map.getHubs().stream().filter(Settlement.class::isInstance).count();
        long mineCount = map.getHubs().stream()
            .filter(IndustrialFacility.class::isInstance)
            .map(IndustrialFacility.class::cast)
            .filter(facility -> facility.getName().startsWith("Mine"))
            .count();
        long farmCount = map.getHubs().stream()
            .filter(IndustrialFacility.class::isInstance)
            .map(IndustrialFacility.class::cast)
            .filter(facility -> facility.getName().startsWith("Farm"))
            .count();
        long forgeCount = map.getHubs().stream()
            .filter(IndustrialFacility.class::isInstance)
            .map(IndustrialFacility.class::cast)
            .filter(facility -> facility.getName().startsWith("Forge"))
            .count();

        assertTrue(settlementCount >= 3 && settlementCount <= 5);
        assertTrue(mineCount >= 2 && mineCount <= 3);
        assertTrue(farmCount >= 2 && farmCount <= 3);
        assertTrue(forgeCount >= 2 && forgeCount <= 3);
    }

    @Test
    void generatedHubFootprintsDoNotOverlapAndStarterTunnelsExist() {
        GameMap map = new GameMap(50, 50, GameMap.DEFAULT_GENERATION_CONFIG, new java.util.Random(11));
        Set<String> occupiedCoordinates = new HashSet<>();
        int occupiedTileCount = 0;
        int footprintArea = 0;
        int starterTunnelCount = 0;

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Tile tile = map.getTileAt(x, y);
                if (tile.getOccupant() != null) {
                    occupiedCoordinates.add(x + ":" + y);
                    occupiedTileCount++;
                }
                if (tile.isHasTunnel()) {
                    starterTunnelCount++;
                }
            }
        }

        for (Hub hub : map.getHubs()) {
            footprintArea += hub.getFootprintWidth() * hub.getFootprintHeight();
        }

        assertTrue(starterTunnelCount > 0);
        assertTrue(occupiedTileCount == footprintArea);
        assertTrue(occupiedCoordinates.size() == occupiedTileCount);
    }

    private List<Tile> footprint(GameMap map, int startX, int startY, int width, int height) {
        java.util.ArrayList<Tile> tiles = new java.util.ArrayList<>();
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                tiles.add(map.getTileAt(x, y));
            }
        }
        return tiles;
    }
}
