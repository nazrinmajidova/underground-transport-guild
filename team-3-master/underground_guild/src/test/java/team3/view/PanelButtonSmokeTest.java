package team3.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import team3.model.Depot;
import team3.model.Hub;
import team3.model.Route;
import team3.model.Tile;

class PanelButtonSmokeTest {

    @BeforeAll
    static void initFxToolkit() throws Exception {
        assumeFalse(isCiEnvironment(),
            "Skipping JavaFX smoke tests on CI/headless runner.");

        AtomicBoolean started = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                started.set(true);
                latch.countDown();
            });
            latch.await(3, TimeUnit.SECONDS);
        } catch (IllegalStateException ignored) {
            started.set(true);
        }
        assertTrue(started.get());
    }

    private static boolean isCiEnvironment() {
        String ci = System.getenv("CI");
        return ci != null && ci.equalsIgnoreCase("true");
    }

    @Test
    void buildViewButtonsChangeBuildMode() throws Exception {
        AtomicReference<BuildView> viewRef = new AtomicReference<>();
        runOnFxAndWait(() -> viewRef.set(new BuildView()));
        BuildView view = viewRef.get();

        AtomicReference<BuildView.BuildMode> modeRef = new AtomicReference<>(BuildView.BuildMode.NONE);
        runOnFxAndWait(() -> view.setOnModeChanged(modeRef::set));

        runOnFxAndWait(() -> findButtonByText(view, "Place tunnel").fire());
        assertEquals(BuildView.BuildMode.TUNNEL, modeRef.get());

        runOnFxAndWait(() -> findButtonByText(view, "Place depot").fire());
        assertEquals(BuildView.BuildMode.DEPOT, modeRef.get());

        runOnFxAndWait(() -> findButtonByText(view, "Remove").fire());
        assertEquals(BuildView.BuildMode.REMOVE, modeRef.get());

        runOnFxAndWait(() -> findButtonByText(view, "Select").fire());
        assertEquals(BuildView.BuildMode.NONE, modeRef.get());
    }

    @Test
    void technologyResearchButtonTransitionsToResearchedState() throws Exception {
        AtomicReference<TechnologyView> viewRef = new AtomicReference<>();
        runOnFxAndWait(() -> viewRef.set(new TechnologyView()));
        TechnologyView view = viewRef.get();

        runOnFxAndWait(() -> {
            Button researchButton = findButtonByText(view, "Research");
            researchButton.fire();
            assertEquals("Researched", researchButton.getText());
            assertTrue(researchButton.isDisable());
        });
    }

    @Test
    void routeManagementButtonsInvokeConfiguredHandlers() throws Exception {
        AtomicReference<RouteManagementView> viewRef = new AtomicReference<>();
        runOnFxAndWait(() -> viewRef.set(new RouteManagementView()));
        RouteManagementView view = viewRef.get();

        Depot d1 = createDepot("Depot A", 1, 1);
        Depot d2 = createDepot("Depot B", 2, 1);
        Route route = new Route("route-1", "R1", d1, d2, 6.0);
        runOnFxAndWait(() -> view.setRoutes(List.of(route)));

        AtomicBoolean newRouteRequested = new AtomicBoolean(false);
        AtomicBoolean deleteRequested = new AtomicBoolean(false);
        AtomicBoolean renameRequested = new AtomicBoolean(false);
        AtomicBoolean cancelRequested = new AtomicBoolean(false);

        runOnFxAndWait(() -> {
            view.setOnNewRouteRequested(() -> newRouteRequested.set(true));
            view.setOnDeleteRouteRequested(r -> deleteRequested.set(r == route));
            view.setOnRenameRouteRequested(r -> renameRequested.set(r == route));
            view.setOnCancelRoute(() -> cancelRequested.set(true));
        });

        runOnFxAndWait(() -> findButtonByText(view, "+ New route").fire());
        assertTrue(newRouteRequested.get());

        runOnFxAndWait(() -> view.setCreationMode(true, List.of(d1, d2)));
        runOnFxAndWait(() -> findButtonByText(view, "Cancel").fire());
        assertTrue(cancelRequested.get());

        runOnFxAndWait(() -> findButtonByText(view, "Modify").fire());
        runOnFxAndWait(() -> findButtonByText(view, "Delete").fire());

        assertTrue(renameRequested.get());
        assertTrue(deleteRequested.get());
    }

    private static Depot createDepot(String name, int x, int y) {
        Tile tile = new Tile(x, y, "tunnel");
        Hub hub = new Hub("hub-" + name, "Hub " + name, new Tile(x + 10, y + 10, Tile.TileType.ROCK)) {
        };
        return new Depot("depot-" + name, name, tile, hub);
    }

    private static Button findButtonByText(Parent root, String text) {
        List<Button> buttons = new ArrayList<>();
        collectButtons(root, buttons);
        for (Button button : buttons) {
            if (text.equals(button.getText())) {
                return button;
            }
        }
        throw new IllegalArgumentException("Button not found: " + text);
    }

    private static void collectButtons(Node node, List<Button> sink) {
        if (node instanceof Button b) {
            sink.add(b);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectButtons(child, sink);
            }
        }
    }

    private static void runOnFxAndWait(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for JavaFX action");
        assertFalse(failure.get() != null, () -> "JavaFX action failed: " + failure.get());
    }
}
