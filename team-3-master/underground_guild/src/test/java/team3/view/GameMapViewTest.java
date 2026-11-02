package team3.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GameMapViewTest {

    @Test
    void computesMinimumAllowedOffsetFromViewportAndContentSize() {
        assertEquals(0, GameMapView.minAllowedOffset(400, 300));
        assertEquals(-200, GameMapView.minAllowedOffset(400, 600));
    }

    @Test
    void clampsOffsetsBetweenTheMapEdgeAndOrigin() {
        assertEquals(0, GameMapView.clampOffset(50, -200));
        assertEquals(-120, GameMapView.clampOffset(-120, -200));
        assertEquals(-200, GameMapView.clampOffset(-250, -200));
    }
}
