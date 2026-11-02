package team3.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import team3.model.Depot;
import team3.model.Farm;
import team3.model.Forge;
import team3.model.Hub;
import team3.model.Mine;
import team3.model.Settlement;
import team3.model.Tile;

class MapPaletteTest {

    @Test
    void returnsRockColorForDefaultRockTile() {
        Tile tile = new Tile(0, 0, Tile.TileType.ROCK);

        assertEquals(Color.web("#233142"), MapPalette.colorForTile(tile));
    }

    @Test
    void returnsTunnelColorForTunneledTile() {
        Tile tile = new Tile(0, 0, Tile.TileType.ROCK);
        tile.setHasTunnel(true);

        assertEquals(Color.web("#c48f3a"), MapPalette.colorForTile(tile));
    }

    @Test
    void returnsSettlementAndDepotColorsForOccupiedTiles() {
        Tile settlementTile = new Tile(0, 0, Tile.TileType.ROCK);
        Tile depotTile = new Tile(1, 1, Tile.TileType.ROCK);
        Settlement settlement = new Settlement(
            "s1",
            "Settlement 1",
            List.of(
                settlementTile, settlementTile, settlementTile,
                settlementTile, settlementTile, settlementTile,
                settlementTile, settlementTile, settlementTile
            ),
            100
        );
        Hub anchor = new Hub("h1", "Anchor", settlementTile) {};
        settlementTile.setOccupant(settlement);
        depotTile.setOccupant(new Depot("d1", "Depot 1", depotTile, anchor));

        assertEquals(Color.web("#4f8ee8"), MapPalette.colorForTile(settlementTile));
        assertEquals(Color.web("#e3a93b"), MapPalette.colorForTile(depotTile));
    }

    @Test
    void returnsFacilitySpecificColors() {
        Tile mineTile = new Tile(0, 0, Tile.TileType.ROCK);
        Tile farmTile = new Tile(1, 0, Tile.TileType.ROCK);
        Tile forgeTile = new Tile(2, 0, Tile.TileType.ROCK);

        mineTile.setOccupant(new Mine("m1", "Mine 1", List.of(mineTile, mineTile, mineTile, mineTile), 1.0f));
        farmTile.setOccupant(new Farm("f1", "Farm 1", List.of(farmTile, farmTile, farmTile, farmTile), 1.0f));
        forgeTile.setOccupant(new Forge("g1", "Forge 1", List.of(forgeTile, forgeTile, forgeTile, forgeTile), 0.8f, 200f));

        assertEquals(Color.web("#7a5b44"), MapPalette.colorForTile(mineTile));
        assertEquals(Color.web("#4ca66b"), MapPalette.colorForTile(farmTile));
        assertEquals(Color.web("#8b8f97"), MapPalette.colorForTile(forgeTile));
    }

    @Test
    void returnsDistinctRouteColorsForSelectionStates() {
        assertEquals(Color.web("#ff7b72"), MapPalette.routeColor(0, true));
        assertNotEquals(MapPalette.routeColor(1, true), MapPalette.routeColor(1, false));
    }
}
