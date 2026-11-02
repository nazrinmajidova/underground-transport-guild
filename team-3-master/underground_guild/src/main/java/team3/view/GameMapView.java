package team3.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import team3.model.CargoTransport;
import team3.model.Depot;
import team3.model.Hub;
import team3.model.IndustrialFacility;
import team3.model.Route;
import team3.model.Settlement;
import team3.model.GameMap;
import team3.model.Tile;
import team3.model.TransportUnit;

/**
 * Lightweight canvas-backed map view used by the main game screen.
 */
public class GameMapView extends StackPane {

    private static final double DEFAULT_WIDTH  = 720;
    private static final double DEFAULT_HEIGHT = 520;
    private static final double TILE_SIZE      = 16;
    private static final double PAN_STEP       = 32;
    private static final double MINIMAP_SIZE   = 170;

    private final Canvas   canvas        = new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    private final Canvas   overlayCanvas = new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    private final Canvas   minimapCanvas = new Canvas(MINIMAP_SIZE, MINIMAP_SIZE);
    private final Pane     mapLayer      = new Pane();
    private final Pane     labelLayer    = new Pane();
    private final StackPane minimapFrame = new StackPane();
    private final VBox cornerCluster = new VBox(8);
    private final Rectangle viewportClip = new Rectangle(DEFAULT_WIDTH, DEFAULT_HEIGHT);

    private GameMap              currentMap;
    private BiConsumer<Integer, Integer> tileClickHandler;
    private List<Route>         visibleRoutes  = List.of();
    private List<TransportUnit> visibleUnits   = List.of();
    private List<Depot>         pendingRouteDepots;
    private Route               selectedRoute;
    private Hub                 selectedHub;
    private float               movementInterpolationAlpha;

    private double  offsetX;
    private double  offsetY;
    private double  lastDragSceneX;
    private double  lastDragSceneY;
    private boolean dragging;

    /**
     * Constructs the map view, sets up canvas layers, and registers input handlers.
     */
    public GameMapView() {
        getStyleClass().add("map-view");
        mapLayer.getChildren().addAll(canvas, overlayCanvas, labelLayer);
        minimapFrame.getStyleClass().add("minimap-frame");
        minimapFrame.getChildren().add(minimapCanvas);
        cornerCluster.getChildren().addAll(minimapFrame, createLegend());
        getChildren().add(mapLayer);
        StackPane.setAlignment(mapLayer, Pos.TOP_LEFT);
        setClip(viewportClip);
        setFocusTraversable(true);
        labelLayer.setMouseTransparent(true);
        // minimapCanvas.setMouseTransparent(true);
        minimapCanvas.setOnMouseClicked(event -> jumpCameraToMinimapCoordinate(event.getX(), event.getY()));
        minimapCanvas.setOnMouseDragged(event -> jumpCameraToMinimapCoordinate(event.getX(), event.getY()));
        StackPane.setAlignment(minimapCanvas, Pos.TOP_RIGHT);
        StackPane.setMargin(minimapCanvas, new javafx.geometry.Insets(10));

        widthProperty().addListener((obs, oldW, newW) -> {
            viewportClip.setWidth(newW.doubleValue());
            applyCameraOffset();
        });
        heightProperty().addListener((obs, oldH, newH) -> {
            viewportClip.setHeight(newH.doubleValue());
            applyCameraOffset();
        });

        setOnMousePressed(event -> {
            requestFocus();
            dragging = false;
            lastDragSceneX = event.getSceneX();
            lastDragSceneY = event.getSceneY();
        });

        setOnMouseDragged(event -> {
            double dx = event.getSceneX() - lastDragSceneX;
            double dy = event.getSceneY() - lastDragSceneY;
            dragging = dragging || Math.abs(dx) > 0 || Math.abs(dy) > 0;
            lastDragSceneX = event.getSceneX();
            lastDragSceneY = event.getSceneY();
            moveCameraBy(dx, dy);
        });

        setOnMouseReleased(event -> {
            if (!dragging) {
                emitTileClick(event.getX(), event.getY());
            }
        });

        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT  || event.getCode() == KeyCode.A) moveCameraBy( PAN_STEP, 0);
            else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) moveCameraBy(-PAN_STEP, 0);
            else if (event.getCode() == KeyCode.UP    || event.getCode() == KeyCode.W) moveCameraBy(0,  PAN_STEP);
            else if (event.getCode() == KeyCode.DOWN  || event.getCode() == KeyCode.S) moveCameraBy(0, -PAN_STEP);
        });

        minimapCanvas.setOnMouseClicked(event -> {
            if (currentMap == null) return;

            double clickX = event.getX();
            double clickY = event.getY();

            double worldWidth = canvas.getWidth();
            double worldHeight = canvas.getHeight();
            double mmw = minimapCanvas.getWidth();
            double mmh = minimapCanvas.getHeight();

            double scale = Math.min((mmw - 10) / worldWidth, (mmh - 10) / worldHeight);
            double mapW = worldWidth * scale;
            double mapH = worldHeight * scale;
            double originX = (mmw - mapW) / 2.0;
            double originY = (mmh - mapH) / 2.0;

            double worldTargetX = (clickX - originX) / scale;
            double worldTargetY = (clickY - originY) / scale;

            offsetX = -(worldTargetX - (viewportClip.getWidth() / 2.0));
            offsetY = -(worldTargetY - (viewportClip.getHeight() / 2.0));

            applyCameraOffset();
        });
    }

    public void setGameMap(GameMap gameMap) {
        this.currentMap = gameMap;
        refresh();
    }

    public void setVisibleRoutes(List<Route> routes) {
        this.visibleRoutes = routes == null ? List.of() : List.copyOf(routes);
        if (selectedRoute != null && !visibleRoutes.contains(selectedRoute)) {
            selectedRoute = null;
        }
        refresh();
    }

    public void setPendingRoute(List<Depot> pendingRouteDepots) {
        this.pendingRouteDepots = pendingRouteDepots;
        refresh();
    }

    public void setVisibleUnits(List<TransportUnit> units) {
        this.visibleUnits = units == null ? List.of() : List.copyOf(units);
        refresh();
    }

    public void setSelectedRoute(Route route) {
        this.selectedRoute = route;
        refresh();
    }

    public void setSelectedHub(Hub hub) {
        this.selectedHub = hub;
        refresh();
    }

    public void setMovementInterpolationAlpha(float alpha) {
        this.movementInterpolationAlpha = Math.max(0f, Math.min(1f, alpha));
    }

    public void setTileClickHandler(BiConsumer<Integer, Integer> handler) {
        this.tileClickHandler = handler;
    }

    public VBox getMapToolsPanel() {
        return cornerCluster;
    }

    public double getTileSize() {
        return TILE_SIZE;
    }

    /**
     * Redraws the entire map: tiles, route overlays, pending-route preview, hub markers,
     * hub labels, and vehicle positions.
     */
    public void refresh() {
        if (currentMap == null) {
            GraphicsContext g = canvas.getGraphicsContext2D();
            g.setFill(Color.web("#10161f"));
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            return;
        }

        double targetW = Math.max(DEFAULT_WIDTH,  currentMap.getWidth()  * TILE_SIZE);
        double targetH = Math.max(DEFAULT_HEIGHT, currentMap.getHeight() * TILE_SIZE);
        canvas.setWidth(targetW);
        canvas.setHeight(targetH);
        overlayCanvas.setWidth(targetW);
        overlayCanvas.setHeight(targetH);
        applyCameraOffset();

        GraphicsContext g  = canvas.getGraphicsContext2D();
        GraphicsContext og = overlayCanvas.getGraphicsContext2D();
        g.setFill(Color.web("#10161f"));
        g.fillRect(0, 0, targetW, targetH);
        og.clearRect(0, 0, targetW, targetH);
        labelLayer.getChildren().clear();

        for (int x = 0; x < currentMap.getWidth(); x++) {
            for (int y = 0; y < currentMap.getHeight(); y++) {
                Tile tile   = currentMap.getTileAt(x, y);
                double drawX = x * TILE_SIZE;
                double drawY = y * TILE_SIZE;
                g.setFill(MapPalette.colorForTile(tile));
                g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);

                if (tile.getSpeedModifier() < 1.0f || tile.getMaintenanceCostModifier() > 1.0f) {
                    g.setFill(Color.web("#e63946", 0.25)); 
                    g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                    g.setFill(Color.web("#e63946", 0.7)); 
                    g.fillOval(drawX + TILE_SIZE - 4, drawY + 2, 2, 2);
                }

                g.setStroke(MapPalette.gridLineColor());
                g.strokeRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
            }
        }

        renderRouteOverlay();
        renderPendingRouteOverlay();
        renderHubOverlay();
        renderVehicleOverlay();
        renderMinimap();
    }

    /**
     * Draws coloured dots on the overlay canvas for every visible transport unit.
     * Cargo units are drawn in dodger-blue; passenger units in lime-green.
     */
    private void renderVehicleOverlay() {
        if (visibleUnits == null || visibleUnits.isEmpty()) return;

        GraphicsContext g = overlayCanvas.getGraphicsContext2D();
        Map<Route, Integer> routeOffsets = new HashMap<>();
        for (TransportUnit unit : visibleUnits) {
            double[] position = resolveVehiclePosition(unit);
            double cx = position[0];
            double cy = position[1];
            if (Double.isNaN(cx) || Double.isNaN(cy)) continue;

            double[] direction = resolveVehicleDirection(unit);
            int routeIndex = routeOffsets.merge(unit.getAssignedRoute(), 1, Integer::sum) - 1;
            double laneOffset = ((routeIndex % 3) - 1) * 4.0;
            if (Math.abs(direction[0]) + Math.abs(direction[1]) > 0) {
                double px = -direction[1];
                double py = direction[0];
                cx += px * laneOffset;
                cy += py * laneOffset;
            }

            g.setStroke(Color.web("#e2e8f0"));
            g.setLineWidth(1.4);
            if (unit instanceof CargoTransport) {
                g.setFill(MapPalette.cargoUnitColor());
                g.fillRoundRect(cx - 6, cy - 4.5, 12, 9, 4, 4);
                g.strokeRoundRect(cx - 6, cy - 4.5, 12, 9, 4, 4);
            } else {
                g.setFill(MapPalette.passengerUnitColor());
                g.fillOval(cx - 5.5, cy - 5.5, 11, 11);
                g.strokeOval(cx - 5.5, cy - 5.5, 11, 11);
            }
        }
    }

    private double[] resolveVehiclePosition(TransportUnit unit) {
        Tile fallback = unit.getCurrenTile();
        if (currentMap == null || unit.getAssignedRoute() == null) {
            if (fallback == null) return new double[] {Double.NaN, Double.NaN};
            return new double[] {tileCenterX(fallback), tileCenterY(fallback)};
        }

        List<Tile> path = currentMap.findTunnelPath(unit.getAssignedRoute().getOrigin(), unit.getAssignedRoute().getDestination());
        if (path.isEmpty() || unit.getAssignedRoute().getDistance() <= 0) {
            if (fallback == null) return new double[] {Double.NaN, Double.NaN};
            return new double[] {tileCenterX(fallback), tileCenterY(fallback)};
        }

        if (!unit.isMovingToDestination()) {
            path = new ArrayList<>(path);
            java.util.Collections.reverse(path);
        }

        double interpolatedDistance = unit.getDistanceTravelled() + unit.getSpeed() * movementInterpolationAlpha;
        double progress = Math.max(0.0, Math.min(1.0, interpolatedDistance / unit.getAssignedRoute().getDistance()));
        double pathProgress = progress * (path.size() - 1);
        int index = (int) Math.floor(pathProgress);
        int nextIndex = Math.min(index + 1, path.size() - 1);
        double t = pathProgress - index;

        Tile from = path.get(index);
        Tile to = path.get(nextIndex);
        double x = tileCenterX(from) + (tileCenterX(to) - tileCenterX(from)) * t;
        double y = tileCenterY(from) + (tileCenterY(to) - tileCenterY(from)) * t;
        return new double[] {x, y};
    }

    private double[] resolveVehicleDirection(TransportUnit unit) {
        Tile fallback = unit.getCurrenTile();
        if (currentMap == null || unit.getAssignedRoute() == null) {
            return new double[] {0, 0};
        }

        List<Tile> path = currentMap.findTunnelPath(unit.getAssignedRoute().getOrigin(), unit.getAssignedRoute().getDestination());
        if (path.size() < 2 || unit.getAssignedRoute().getDistance() <= 0) {
            return new double[] {0, 0};
        }

        if (!unit.isMovingToDestination()) {
            path = new ArrayList<>(path);
            java.util.Collections.reverse(path);
        }

        double interpolatedDistance = unit.getDistanceTravelled() + unit.getSpeed() * movementInterpolationAlpha;
        double progress = Math.max(0.0, Math.min(1.0, interpolatedDistance / unit.getAssignedRoute().getDistance()));
        double pathProgress = progress * (path.size() - 1);
        int index = (int) Math.floor(pathProgress);
        int nextIndex = Math.min(index + 1, path.size() - 1);

        Tile from = path.get(index);
        Tile to = path.get(nextIndex);
        double dx = tileCenterX(to) - tileCenterX(from);
        double dy = tileCenterY(to) - tileCenterY(from);
        double len = Math.hypot(dx, dy);
        if (len == 0) {
            if (fallback == null) {
                return new double[] {0, 0};
            }
            return new double[] {0, 0};
        }
        return new double[] {dx / len, dy / len};
    }

    /**
     * Draws the confirmed route paths and their endpoint markers on the overlay canvas.
     * The selected route is drawn last so it appears on top with greater visual weight.
     */
    private void renderRouteOverlay() {
        if (currentMap == null || visibleRoutes.isEmpty()) return;

        GraphicsContext og = overlayCanvas.getGraphicsContext2D();
        List<Route> toDraw = new ArrayList<>(visibleRoutes);

        if (selectedRoute != null && toDraw.remove(selectedRoute)) {
            toDraw.add(selectedRoute);
        }

        for (int i = 0; i < toDraw.size(); i++) {
            Route route = toDraw.get(i);
            boolean isSelected = route.equals(selectedRoute)
                    || (selectedRoute == null && i == toDraw.size() - 1);
            drawRouteEndpoints(og, route, i, isSelected);
        }
    }

    /**
     * Draws the dashed preview path and numbered depot markers for the route currently
     * being assembled by the player. Orange indicates a valid tunnel connection; red
     * indicates no tunnel path exists between two consecutive depots.
     */
    private void renderPendingRouteOverlay() {
        if (pendingRouteDepots == null || pendingRouteDepots.isEmpty() || currentMap == null) return;

        GraphicsContext g = overlayCanvas.getGraphicsContext2D();
        g.setLineWidth(3.5);
        g.setLineDashOffset(5);
        g.setLineDashes(10, 10);

        for (int i = 0; i < pendingRouteDepots.size() - 1; i++) {
            Depot d1   = pendingRouteDepots.get(i);
            Depot d2   = pendingRouteDepots.get(i + 1);
            List<Tile> path = currentMap.findTunnelPath(d1, d2);

            if (!path.isEmpty()) {
                Color preview = MapPalette.routePreviewValidColor();
                g.setStroke(preview.deriveColor(0, 1, 1.05, 0.28));
                g.setLineWidth(7);
                for (int p = 0; p < path.size() - 1; p++) {
                    g.strokeLine(tileCenterX(path.get(p)),   tileCenterY(path.get(p)),
                                 tileCenterX(path.get(p + 1)), tileCenterY(path.get(p + 1)));
                }
                g.setStroke(preview);
                g.setLineWidth(3.5);
                for (int p = 0; p < path.size() - 1; p++) {
                    g.strokeLine(tileCenterX(path.get(p)),   tileCenterY(path.get(p)),
                                 tileCenterX(path.get(p + 1)), tileCenterY(path.get(p + 1)));
                }
            } else {
                g.setStroke(MapPalette.routePreviewInvalidColor());
                g.setLineWidth(3.5);
                g.strokeLine(tileCenterX(d1.getTile()), tileCenterY(d1.getTile()),
                             tileCenterX(d2.getTile()), tileCenterY(d2.getTile()));
            }
        }
        g.setLineDashes(null);

        for (int i = 0; i < pendingRouteDepots.size(); i++) {
            Depot  depot = pendingRouteDepots.get(i);
            double cx    = tileCenterX(depot.getTile());
            double cy    = tileCenterY(depot.getTile());

            g.setFill(MapPalette.routePreviewValidColor());
            g.fillOval(cx - 10, cy - 10, 20, 20);
            g.setStroke(Color.WHITE);
            g.setLineWidth(1.5);
            g.strokeOval(cx - 10, cy - 10, 20, 20);
            g.setFill(Color.BLACK);
            g.fillText(String.valueOf(i + 1), cx - 3.5, cy + 4);
        }
    }

    /**
     * Draws endpoint markers and the connecting path for a single route.
     */
    private void drawRouteEndpoints(GraphicsContext og, Route route, int routeIndex, boolean isSelected) {
        if (route.getOrigin() == null || route.getDestination() == null) return;

        Color routeColor = MapPalette.routeColor(routeIndex, isSelected);
        drawRoutePath(og, route, routeColor, isSelected);
        drawRouteEndpoint(og, route.getOrigin(),      routeColor, "1", isSelected);
        drawRouteEndpoint(og, route.getDestination(), routeColor, "2", isSelected);
    }

    /**
     * Draws the polyline path of a route along the tunnel tiles, including direction arrows
     * and (for the selected route) a floating name label.
     */
    private void drawRoutePath(GraphicsContext og, Route route, Color routeColor, boolean isSelected) {
        List<Tile> path = currentMap.findTunnelPath(route.getOrigin(), route.getDestination());
        if (path.isEmpty()) return;

        og.setStroke(routeColor.deriveColor(0, 1, 1.05, isSelected ? 0.28 : 0.18));
        og.setLineWidth(isSelected ? 9 : 6);
        og.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        og.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        for (int i = 0; i < path.size() - 1; i++) {
            og.strokeLine(tileCenterX(path.get(i)),     tileCenterY(path.get(i)),
                          tileCenterX(path.get(i + 1)), tileCenterY(path.get(i + 1)));
        }

        og.setStroke(routeColor);
        og.setLineWidth(isSelected ? 4.5 : 3);
        og.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        og.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        og.setGlobalAlpha(isSelected ? 1.0 : 0.86);

        for (int i = 0; i < path.size() - 1; i++) {
            og.strokeLine(tileCenterX(path.get(i)),     tileCenterY(path.get(i)),
                          tileCenterX(path.get(i + 1)), tileCenterY(path.get(i + 1)));
        }

        drawDirectionMarkers(og, path, routeColor, isSelected);
        if (isSelected) {
            drawRouteName(og, route, path, routeColor);
        }
        og.setGlobalAlpha(1.0);
    }

    /**
     * Draws the circular endpoint marker for a single hub.
     */
    private void drawRouteEndpoint(GraphicsContext og, Hub hub, Color routeColor,
                                   String orderLabel, boolean isSelected) {
        double cx     = (hub.getTile().getX() + (hub.getFootprintWidth()  / 2.0)) * TILE_SIZE;
        double cy     = (hub.getTile().getY() + (hub.getFootprintHeight() / 2.0)) * TILE_SIZE;
        double radius = isSelected ? 12 : 9;

        og.setLineWidth(isSelected ? 3 : 2);
        og.setStroke(routeColor);
        og.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2);
        og.setFill(routeColor.deriveColor(0, 1, 1, isSelected ? 0.95 : 0.7));
        og.fillOval(cx - (radius - 4), cy - (radius - 4), (radius - 4) * 2, (radius - 4) * 2);
        og.setFill(Color.web("#10161f"));
        og.fillText(orderLabel, cx - 3, cy + 4);
    }

    /**
     * Draws small arrow-head markers at regular intervals along the path to indicate direction.
     */
    private void drawDirectionMarkers(GraphicsContext og, List<Tile> path,
                                      Color routeColor, boolean isSelected) {
        if (path.size() < 2) return;

        int step = Math.max(2, path.size() / 3);
        og.setFill(routeColor.deriveColor(0, 1, 1, isSelected ? 0.95 : 0.7));
        for (int i = step; i < path.size(); i += step) {
            drawArrowHead(og,
                tileCenterX(path.get(i - 1)), tileCenterY(path.get(i - 1)),
                tileCenterX(path.get(i)),     tileCenterY(path.get(i)));
        }
    }

    /**
     * Draws a single filled arrow-head pointing from (startX, startY) toward (endX, endY).
     */
    private void drawArrowHead(GraphicsContext og,
                                double startX, double startY,
                                double endX,   double endY) {
        double dx  = endX - startX;
        double dy  = endY - startY;
        double len = Math.hypot(dx, dy);
        if (len == 0) return;

        double ux   = dx / len;
        double uy   = dy / len;
        double tipX = endX;
        double tipY = endY;
        double baseX = tipX - ux * 7;
        double baseY = tipY - uy * 7;
        double sideX = -uy * 4;
        double sideY =  ux * 4;

        og.fillPolygon(
            new double[]{tipX,  baseX + sideX, baseX - sideX},
            new double[]{tipY,  baseY + sideY, baseY - sideY},
            3
        );
    }

    /**
     * Draws a small dark pill with the route name near the midpoint of the path.
     */
    private void drawRouteName(GraphicsContext og, Route route, List<Tile> path, Color routeColor) {
        Tile mid    = path.get(path.size() / 2);
        double anchorX = tileCenterX(mid);
        double anchorY = tileCenterY(mid);
        String label = route.getName() == null || route.getName().isBlank()
            ? route.getOrigin().getName() + " -> " + route.getDestination().getName()
            : route.getName();
        double boxWidth = Math.max(92, label.length() * 6.1);
        double boxX = anchorX + 16;
        double boxY = anchorY - 30;

        og.setStroke(routeColor.deriveColor(0, 1, 1, 0.9));
        og.setLineWidth(1.4);
        og.strokeLine(anchorX + 4, anchorY - 4, boxX, boxY + 12);
        og.setFill(Color.web("#0f172a", 0.92));
        og.fillRoundRect(boxX, boxY, boxWidth, 22, 10, 10);
        og.setStroke(routeColor.deriveColor(0, 1, 1, 0.75));
        og.setLineWidth(1);
        og.strokeRoundRect(boxX, boxY, boxWidth, 22, 10, 10);
        og.setFill(routeColor);
        og.fillText(label, boxX + 9, boxY + 15);
    }

    /**
     * Draws rounded-rectangle fill, outline, and a type-specific marker for every hub
     * on the current map, and adds floating name labels to the label layer.
     */
    private void renderHubOverlay() {
        if (currentMap == null) return;

        GraphicsContext og = overlayCanvas.getGraphicsContext2D();
        for (Hub hub : currentMap.getHubs()) {
            double drawX = hub.getTile().getX() * TILE_SIZE;
            double drawY = hub.getTile().getY() * TILE_SIZE;
            double drawW = hub.getFootprintWidth()  * TILE_SIZE;
            double drawH = hub.getFootprintHeight() * TILE_SIZE;
            boolean isSelected = hub.equals(selectedHub);

            if (isSelected) {
                og.setFill(MapPalette.selectedHubGlow());
                og.fillRoundRect(drawX - 4, drawY - 4, drawW + 8, drawH + 8, 10, 10);
            }
            og.setFill(MapPalette.hubAccentColor(hub));
            og.fillRoundRect(drawX + 3, drawY + 3, drawW - 6, drawH - 6, 6, 6);
            og.setStroke(MapPalette.hubOutlineColor(hub));
            og.setLineWidth(isSelected ? 3 : 2);
            og.strokeRoundRect(drawX + 1, drawY + 1, drawW - 2, drawH - 2, 6, 6);

            drawHubMarker(og, hub, drawX, drawY, drawW, drawH, isSelected);
        }

        List<Hub> hubsForLabels = new ArrayList<>(currentMap.getHubs());
        hubsForLabels.sort(Comparator.comparingInt(this::hubLabelPriority).reversed());
        List<Bounds> occupied = new ArrayList<>();
        for (Hub hub : hubsForLabels) {
            if (!shouldShowLabel(hub)) {
                continue;
            }
            Label label = createHubLabel(
                hub,
                hub.getTile().getX() * TILE_SIZE,
                hub.getTile().getY() * TILE_SIZE,
                hub.getFootprintWidth() * TILE_SIZE,
                hub.getFootprintHeight() * TILE_SIZE,
                hub.equals(selectedHub)
            );
            label.applyCss();
            label.autosize();
            Bounds bounds = label.getBoundsInParent();
            if (hub.equals(selectedHub) || occupied.stream().noneMatch(bounds::intersects)) {
                occupied.add(bounds);
                labelLayer.getChildren().add(label);
            }
        }
    }

    /**
     * Draws the small icon in the centre of a hub's footprint that differentiates hub types.
     */
    private void drawHubMarker(GraphicsContext og, Hub hub,
                                double drawX, double drawY,
                                double drawW, double drawH,
                                boolean isSelected) {
        double cx = drawX + drawW / 2.0;
        double cy = drawY + drawH / 2.0;
        og.setFill(Color.web("#f5f7fa"));
        double scale = isSelected ? 1.18 : 1.0;

        if (hub instanceof Settlement) {
            double radius = 6 * scale;
            og.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
            return;
        }

        if (hub instanceof IndustrialFacility facility) {
            double half = 5 * scale;
            og.fillRect(cx - half, cy - half, half * 2, half * 2);
            og.setStroke(Color.web("#10161f"));
            og.setLineWidth(1.5);
            if (facility.getName().startsWith("Mine")) {
                og.strokeLine(cx - 3, cy + 4, cx + 3, cy - 4);
                og.strokeLine(cx,     cy + 4, cx + 4, cy + 1);
            } else if (facility.getName().startsWith("Farm")) {
                og.strokeLine(cx, cy - 4, cx, cy + 4);
                og.strokeLine(cx, cy - 1, cx + 3, cy - 4);
                og.strokeLine(cx, cy + 2, cx + 3, cy - 1);
            } else {
                og.strokeOval(cx - 3, cy - 3, 6, 6);
            }
            return;
        }

        if (hub instanceof Depot) {
            double size = 6 * scale;
            og.fillPolygon(
                new double[]{cx,      cx + size, cx,      cx - size},
                new double[]{cy - (size + 1),  cy,       cy + (size + 1),  cy},
                4
            );
        }
    }

    /**
     * Creates a floating name label positioned just below a hub's footprint.
     */
    private Label createHubLabel(Hub hub, double drawX, double drawY, double drawW, double drawH, boolean selected) {
        Label label = new Label(hubLabelText(hub));
        label.getStyleClass().add("hub-label");
        if (selected) {
            label.getStyleClass().add("hub-label-selected");
        }
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setLayoutX(labelXForHub(hub, drawX, drawW));
        label.setLayoutY(labelYForHub(hub, drawY, drawH));
        return label;
    }

    private double labelXForHub(Hub hub, double drawX, double drawW) {
        if (hub instanceof Depot) {
            return drawX + drawW + 6;
        }
        return drawX + 2;
    }

    private double labelYForHub(Hub hub, double drawY, double drawH) {
        if (hub instanceof Depot) {
            return drawY - 2;
        }
        return drawY + drawH + 4;
    }

    private int hubLabelPriority(Hub hub) {
        if (hub.equals(selectedHub)) {
            return 100;
        }
        if (hub instanceof Depot) {
            return 80;
        }
        if (hub instanceof Settlement && selectedHub != null) {
            return 70;
        }
        return 60;
    }

    private String hubLabelText(Hub hub) {
        if (hub instanceof Depot)                    return hub.getName();
        if (hub instanceof Settlement)               return hub.getName();
        if (hub instanceof IndustrialFacility f)     return f.getName();
        return hub.getName();
    }

    private boolean shouldShowLabel(Hub hub) {
        if (hub.equals(selectedHub)) {
            return true;
        }
        if (hub instanceof Depot) {
            return true;
        }
        return selectedHub == null && hub instanceof IndustrialFacility;
    }

    /**
     * Shifts the camera by the given delta and re-applies clamped offsets.
     */
    private void moveCameraBy(double dx, double dy) {
        offsetX += dx;
        offsetY += dy;
        applyCameraOffset();
    }

    private void applyCameraOffset() {
        offsetX = centeredOrClampedOffset(offsetX, viewportClip.getWidth(), canvas.getWidth());
        offsetY = centeredOrClampedOffset(offsetY, viewportClip.getHeight(), canvas.getHeight());
        mapLayer.setTranslateX(offsetX);
        mapLayer.setTranslateY(offsetY);
        renderMinimap();
    }

    /**
     * Draws a compact minimap including the current camera viewport rectangle.
     */
    private void renderMinimap() {
        GraphicsContext mg = minimapCanvas.getGraphicsContext2D();
        double mmw = minimapCanvas.getWidth();
        double mmh = minimapCanvas.getHeight();

        mg.clearRect(0, 0, mmw, mmh);
        mg.setFill(Color.web("#0b1119", 0.96));
        mg.fillRoundRect(0, 0, mmw, mmh, 10, 10);
        mg.setStroke(Color.web("#64748b"));
        mg.setLineWidth(1.1);
        mg.strokeRoundRect(0.5, 0.5, mmw - 1, mmh - 1, 10, 10);

        if (currentMap == null) {
            return;
        }

        double worldWidth = canvas.getWidth();
        double worldHeight = canvas.getHeight();
        if (worldWidth <= 0 || worldHeight <= 0) {
            return;
        }

        double scale = Math.min((mmw - 10) / worldWidth, (mmh - 10) / worldHeight);
        double mapW = worldWidth * scale;
        double mapH = worldHeight * scale;
        double originX = (mmw - mapW) / 2.0;
        double originY = (mmh - mapH) / 2.0;
        double miniTile = TILE_SIZE * scale;

        for (int x = 0; x < currentMap.getWidth(); x++) {
            for (int y = 0; y < currentMap.getHeight(); y++) {
                Tile tile = currentMap.getTileAt(x, y);
                mg.setFill(MapPalette.colorForTile(tile));
                mg.fillRect(originX + x * miniTile, originY + y * miniTile, miniTile, miniTile);
            }
        }

        for (int i = 0; i < visibleRoutes.size(); i++) {
            Route route = visibleRoutes.get(i);
            List<Tile> path = currentMap.findTunnelPath(route.getOrigin(), route.getDestination());
            if (path.isEmpty()) {
                continue;
            }
            mg.setStroke(MapPalette.routeColor(i, route.equals(selectedRoute)).deriveColor(0, 1, 1, 0.8));
            mg.setLineWidth(route.equals(selectedRoute) ? 1.6 : 1.0);
            for (int p = 0; p < path.size() - 1; p++) {
                mg.strokeLine(
                    originX + tileCenterX(path.get(p)) * scale,
                    originY + tileCenterY(path.get(p)) * scale,
                    originX + tileCenterX(path.get(p + 1)) * scale,
                    originY + tileCenterY(path.get(p + 1)) * scale
                );
            }
        }

        for (Hub hub : currentMap.getHubs()) {
            double hubX = originX + tileCenterX(hub.getTile()) * scale;
            double hubY = originY + tileCenterY(hub.getTile()) * scale;
            double radius = hub.equals(selectedHub) ? 3.4 : 2.2;
            mg.setFill(MapPalette.hubAccentColor(hub));
            mg.fillOval(hubX - radius, hubY - radius, radius * 2, radius * 2);
        }

        double viewWorldX = -offsetX;
        double viewWorldY = -offsetY;
        double viewWorldW = viewportClip.getWidth();
        double viewWorldH = viewportClip.getHeight();

        mg.setStroke(Color.web("#f5f7fa"));
        mg.setLineWidth(1.4);
        mg.strokeRect(
            originX + viewWorldX * scale,
            originY + viewWorldY * scale,
            Math.min(viewWorldW * scale, mapW),
            Math.min(viewWorldH * scale, mapH)
        );
    }

    private VBox createLegend() {
        VBox legend = new VBox(6);
        legend.getStyleClass().add("map-legend");
        Label title = new Label("Map Key");
        title.getStyleClass().add("map-legend-title");
        legend.getChildren().addAll(
            title,
            legendRow("Depot", "#d97706"),
            legendRow("Settlement", "#1d4ed8"),
            legendRow("Industry", "#15803d"),
            legendRow("Route", "#ff6b6b")
        );
        return legend;
    }

    private HBox legendRow(String text, String color) {
        Region swatch = new Region();
        swatch.getStyleClass().add("legend-swatch");
        switch (color) {
            case "#d97706" -> swatch.getStyleClass().add("legend-swatch-depot");
            case "#1d4ed8" -> swatch.getStyleClass().add("legend-swatch-settlement");
            case "#15803d" -> swatch.getStyleClass().add("legend-swatch-industry");
            case "#ff6b6b" -> swatch.getStyleClass().add("legend-swatch-route");
            default -> swatch.getStyleClass().add("legend-swatch-default");
        }

        Label label = new Label(text);
        label.getStyleClass().add("map-legend-item");

        HBox row = new HBox(8, swatch, label);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Returns the smallest (most negative) translation allowed so the content fills the viewport.
     */
    static double minAllowedOffset(double viewportSize, double contentSize) {
        return Math.min(0, viewportSize - contentSize);
    }

    /**
     * Clamps a requested offset into the valid range.
     */
    static double clampOffset(double requestedOffset, double minOffset) {
        return Math.max(minOffset, Math.min(0, requestedOffset));
    }

    private static double centeredOrClampedOffset(double requestedOffset, double viewportSize, double contentSize) {
        if (contentSize <= viewportSize) {
            return (viewportSize - contentSize) / 2.0;
        }
        return clampOffset(requestedOffset, minAllowedOffset(viewportSize, contentSize));
    }

    /**
     * Converts a mouse-release position to tile coordinates and forwards the event to the
     * registered.
     */
    private void emitTileClick(double localX, double localY) {
        if (currentMap == null || tileClickHandler == null) return;

        int tileX = (int) Math.floor((localX - mapLayer.getTranslateX()) / TILE_SIZE);
        int tileY = (int) Math.floor((localY - mapLayer.getTranslateY()) / TILE_SIZE);
        if (currentMap.isInBounds(tileX, tileY)) {
            tileClickHandler.accept(tileX, tileY);
        }
    }

    /**
     * Returns the x canvas coordinate of the centre of a tile.
     */
    private double tileCenterX(Tile tile) {
        return (tile.getX() * TILE_SIZE) + (TILE_SIZE / 2.0);
    }

    /**
     * Returns the y canvas coordinate of the centre of a tile.
     */
    private double tileCenterY(Tile tile) {
        return (tile.getY() * TILE_SIZE) + (TILE_SIZE / 2.0);
    }

    /**
     * Translates a minimap interaction (click or drag) into main camera movement.
     */
    private void jumpCameraToMinimapCoordinate(double localX, double localY) {
        if (currentMap == null) return;

        double worldWidth = canvas.getWidth();
        double worldHeight = canvas.getHeight();
        double mmw = minimapCanvas.getWidth();
        double mmh = minimapCanvas.getHeight();

        // Replicate scale and origin logic from renderMinimap()
        double scale = Math.min((mmw - 10) / worldWidth, (mmh - 10) / worldHeight);
        double mapW = worldWidth * scale;
        double mapH = worldHeight * scale;
        double originX = (mmw - mapW) / 2.0;
        double originY = (mmh - mapH) / 2.0;

        // Convert minimap coordinates to world coordinates
        double worldTargetX = (localX - originX) / scale;
        double worldTargetY = (localY - originY) / scale;

        // Center the camera on the target point
        offsetX = -(worldTargetX - (viewportClip.getWidth() / 2.0));
        offsetY = -(worldTargetY - (viewportClip.getHeight() / 2.0));

        // Clamp bounds and redraw
        applyCameraOffset();
    }
}
