package team3.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import team3.controller.AppController;
import team3.model.Depot;
import team3.model.Game;
import team3.service.GameService;
import team3.model.GameMap;
import team3.model.Hub;
import team3.model.IndustrialFacility;
import team3.model.Route;
import team3.model.Settlement;
import team3.model.Tile;
import team3.model.TransportUnit;

/** 
 * The primary gameplay interface. Manages the map display and various management sub-panels. 
 * */
public class MainGameView {
    private final AppController controller;

    private final BuildView buildPanel    = new BuildView();
    private final EconomyView economyPanel  = new EconomyView();
    private final RouteManagementView routePanel    = new RouteManagementView();
    private final VehicleManagementView vehiclePanel  = new VehicleManagementView();
    private final TechnologyView technologyPanel = new TechnologyView();
    private final InfrastructureView infrastructurePanel = new InfrastructureView();
    private final GameMapView mapView = new GameMapView();
    private final Label statusMessage = new Label("Select a tile on the map.");
    
    private Label moneyLabel;
    private Label incomeLabel;
    private Label expensesLabel;
    private Label workersLabel;
    private Label vehiclesLabel;
    private Label speedValueLabel;
    private Label infoTitleLabel;
    private Label infoSubtitleLabel;
    private Label infoKey1Label;
    private Label infoValue1Label;
    private Label infoKey2Label;
    private Label infoValue2Label;
    private Label infoKey3Label;
    private Label infoValue3Label;
    private Hub selectedHub;
    private Timeline financialRefreshTimer;

    private Region activePanel = null;
    private final ScrollPane activePanelScroller = new ScrollPane();
    private final VBox dockColumn = new VBox(12);
    private final HBox footerRow = new HBox(10);
    private final VBox sidebarColumn = new VBox(12);

    private boolean isCreatingRoute = false;
    private BuildView.BuildMode currentBuildMode = BuildView.BuildMode.TUNNEL;
    private final List<Depot> pendingRouteDepots = new ArrayList<>();
    private StackPane mapWrapper;
    public MainGameView(AppController controller) {
        this.controller = controller;
        vehiclePanel.setController(controller);
        vehiclePanel.setMainGameView(this);
        activePanelScroller.setFitToWidth(false);
        activePanelScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        activePanelScroller.setMinWidth(Region.USE_PREF_SIZE);
        activePanelScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        activePanelScroller.setPannable(true);
        activePanelScroller.setPrefViewportWidth(420);
        activePanelScroller.setPrefViewportHeight(156);
        activePanelScroller.setMinViewportHeight(132);
        activePanelScroller.setMaxHeight(168);
        activePanelScroller.setMaxWidth(Region.USE_PREF_SIZE);
        activePanelScroller.getStyleClass().add("dock-panel-scroll");
        footerRow.getStyleClass().add("footer-row");
        footerRow.setAlignment(Pos.TOP_LEFT);
        footerRow.setFillHeight(false);
        footerRow.setMaxWidth(Region.USE_PREF_SIZE);
        dockColumn.getStyleClass().add("control-rail");
        dockColumn.setFillWidth(false);
        dockColumn.setPickOnBounds(false);
        dockColumn.setMaxWidth(Region.USE_PREF_SIZE);
        sidebarColumn.getStyleClass().add("map-sidebar");
        buildPanel.setOnModeChanged(mode -> currentBuildMode = mode);
        routePanel.setOnBuyVehicleRequested(() -> vehiclePanel.openPurchaseDialog());
        infrastructurePanel.setOnRenameDepotRequested(this::promptRenameSelectedDepot);

        setupRouteCreationHandlers();
    }

    public Parent build() {
        BorderPane root = new BorderPane();
        root.setTop(createStatusBar());
        root.setCenter(createGameCanvasShell());
        startFinancialRefreshTimer();
        root.setFocusTraversable(true);
        root.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DIGIT1 -> setSimulationSpeed("normal", "1x");
                case DIGIT2 -> setSimulationSpeed("fast", "2x");
                case DIGIT3 -> setSimulationSpeed("very_fast", "4x");
                case DIGIT4 -> setSimulationSpeed("pause", "paused");
                case P -> setSimulationSpeed("pause", "paused");
                default -> {}
            }
        });
        return root;
    }

    /**
    * Handles route creation lifecycle (start/cancel/confirm).
    * Ensures UI + map state remain consistent.
    */
    private void setupRouteCreationHandlers() {
        routePanel.setOnNewRouteRequested(() -> {
            isCreatingRoute = true;
            pendingRouteDepots.clear();
            mapView.setPendingRoute(pendingRouteDepots);
            routePanel.setCreationMode(true, pendingRouteDepots);
            statusMessage.setText("Select depots on the map to create a route.");
            mapView.refresh();
        });

        routePanel.setOnCancelRoute(() -> {
            isCreatingRoute = false;
            pendingRouteDepots.clear();
            mapView.setPendingRoute(null);
            routePanel.setCreationMode(false, null);
            statusMessage.setText("Route creation cancelled.");
            mapView.refresh();
        });

        routePanel.setOnConfirmRoute(routeName -> {
            if (routeName == null || routeName.trim().isEmpty()) {
                statusMessage.setText("Route name cannot be empty.");
                return;
            }

            String result = controller.getGameService()
                .createRoute(routeName.trim(), pendingRouteDepots);

            if (!"Success".equals(result)) {
                statusMessage.setText("Error: " + result);
                mapView.refresh();
                return;
            }
            
            isCreatingRoute = false;
            pendingRouteDepots.clear();

            Game game = controller.getGameService().getCurrentGame();

            mapView.setPendingRoute(null);
            routePanel.setCreationMode(false, null);

            if (game != null) {
                routePanel.setRoutes(game.getRoutes());
                mapView.setVisibleRoutes(game.getRoutes());
            }

            statusMessage.setText("Route successfully created!");
            mapView.refresh();
        });

        routePanel.setOnAssignVehicleRequested(this::openAssignVehicleDialog);
        routePanel.setOnDeleteRouteRequested(this::deleteRoute);
        routePanel.setOnRenameRouteRequested(this::renameRoute);
    }

    private StackPane createGameCanvasShell() {
        StackPane shell = new StackPane(createMapArea());
        shell.getStyleClass().add("game-shell");
        return shell;
    }

    private Parent createMapArea() {
        Game currentGame = controller.getGameService().getCurrentGame();
        if (currentGame != null) {
            mapView.setGameMap(currentGame.getGameMap());
            mapView.setVisibleRoutes(currentGame.getRoutes());
            mapView.setVisibleUnits(currentGame.getUnits());
            
            routePanel.setRouteSelectionHandler(route -> {
                if (!isCreatingRoute) {
                    mapView.setSelectedRoute(route);
                    selectedHub = null;
                    mapView.setSelectedHub(null);
                    statusMessage.setText("Selected route: " + route.getName());
                }
            });
            routePanel.setRoutes(currentGame.getRoutes());
            if (!currentGame.getRoutes().isEmpty()) {
                routePanel.selectRoute(currentGame.getRoutes().getFirst());
            }
            selectedHub = null;
        }
        mapView.setTileClickHandler((tileX, tileY) -> {
            Game game = controller.getGameService().getCurrentGame();
            if (game == null) return;
            
            if (isCreatingRoute) {
                GameMap map = game.getGameMap();
                Tile tile = map.getTileAt(tileX, tileY);
                if (tile != null) {
                    Hub hub = map.getHubAt(tile);
                    if (hub instanceof Depot depot) {
                        if (!pendingRouteDepots.isEmpty() && pendingRouteDepots.get(pendingRouteDepots.size() - 1).equals(depot)) {
                            statusMessage.setText("Depot is already the last selected stop.");
                        } else {
                            pendingRouteDepots.add(depot);
                            mapView.setPendingRoute(pendingRouteDepots);
                            routePanel.updatePendingDepots(pendingRouteDepots);
                            statusMessage.setText("Added " + depot.getName() + " to route sequence.");
                        }
                    } else {
                        statusMessage.setText("You must select a Depot.");
                    }
                }
            } else {
                if (currentBuildMode == BuildView.BuildMode.NONE) {
                    statusMessage.setText("Select a build mode first.");
                } else if (currentBuildMode == BuildView.BuildMode.DEPOT) {
                    GameService.DepotPlacementResult placement = controller.getGameService().placeDepotAt(tileX, tileY);
                    statusMessage.setText(placement.getMessage());
                    if (placement.isSuccess()) {
                        refreshRouteAndMapData();
                        vehiclePanel.refreshList();
                    }
                } else if (currentBuildMode == BuildView.BuildMode.REMOVE) {
                    statusMessage.setText(controller.getGameService().removeStructureAt(tileX, tileY));
                    refreshRouteAndMapData();
                    vehiclePanel.refreshList();
                } else {
                    statusMessage.setText(controller.handleBuildTunnel(tileX, tileY));
                }
            }
            updateSelectionFromTile(tileX, tileY);
            mapView.refresh();
        });
        mapWrapper = new StackPane(mapView);
        mapWrapper.getStyleClass().add("map-wrapper");
        sidebarColumn.getChildren().setAll(mapView.getMapToolsPanel(), createInfoPanel());
        showDockContent();
        BorderPane content = new BorderPane();
        content.getStyleClass().add("game-content");
        content.setCenter(mapWrapper);
        content.setRight(sidebarColumn);
        content.setBottom(dockColumn);
        BorderPane.setMargin(mapWrapper, new Insets(0, 16, 8, 0));
        return content;
    }

    public void showGameOver(Stage stage) {
        StackPane overlay = GameOverView.build(controller, stage);
        mapWrapper.getChildren().add(overlay); // sits on top of everything
    }

    public void refreshMap() {
        Game game = controller.getGameService().getCurrentGame();
        if (game != null) {
            mapView.setVisibleUnits(game.getUnits());
            mapView.refresh();
        }
    }

    private void openAssignVehicleDialog(Route route) {
        Game game = controller.getGameService().getCurrentGame();
        if (route == null || game == null || game.getUnits().isEmpty()) {
            statusMessage.setText("No route or available vehicles to assign.");
            return;
        }

        java.util.LinkedHashMap<String, TransportUnit> labelsToUnits = new java.util.LinkedHashMap<>();
        for (TransportUnit unit : game.getUnits()) {
            String assigned = unit.getAssignedRoute() == null ? "unassigned" : unit.getAssignedRoute().getName();
            String label = unit.getName() + " (" + assigned + ")";
            labelsToUnits.put(label, unit);
        }
        if (labelsToUnits.isEmpty()) {
            statusMessage.setText("No route or available vehicles to assign.");
            return;
        }

        String firstLabel = labelsToUnits.keySet().iterator().next();
        ChoiceDialog<String> dialog = new ChoiceDialog<>(firstLabel, labelsToUnits.keySet());
        dialog.setTitle("Assign Vehicle");
        dialog.setHeaderText("Assign a vehicle to route: " + route.getName());
        dialog.setContentText("Vehicle:");

        dialog.showAndWait().ifPresent(selectedLabel -> {
            TransportUnit selectedUnit = labelsToUnits.get(selectedLabel);
            if (selectedUnit == null) {
                statusMessage.setText("Selected vehicle is unavailable.");
                return;
            }

            String result = controller.getGameService().assignUnitToRoute(selectedUnit.getId(), route.getId());
            statusMessage.setText(result);
            routePanel.setRoutes(game.getRoutes());
            vehiclePanel.refreshList();
            refreshMap();
            updateStatusBar();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(result);
            alert.showAndWait();
        });
    }

    private void deleteRoute(Route route) {
        if (route == null) {
            statusMessage.setText("Select a route first.");
            return;
        }

        String result = controller.getGameService().deleteRoute(route.getId());
        statusMessage.setText(result);
        refreshRouteAndMapData();
        vehiclePanel.refreshList();
        updateStatusBar();
    }

    private void renameRoute(Route route) {
        if (route == null) {
            statusMessage.setText("Select a route first.");
            return;
        }

        ChoiceDialog<String> actionDialog = new ChoiceDialog<>(
            "Rename route",
            List.of("Rename route", "Assign vehicle", "Unassign vehicle", "Modify route stops")
        );
        actionDialog.setTitle("Route Actions");
        actionDialog.setHeaderText("Manage route: " + route.getName());
        actionDialog.setContentText("Choose action:");
        actionDialog.showAndWait().ifPresent(choice -> {
            switch (choice) {
                case "Rename route" -> promptRenameRoute(route);
                case "Assign vehicle" -> openAssignVehicleDialog(route);
                case "Unassign vehicle" -> {
                    String result = controller.getGameService().unassignVehicleFromRoute(route.getId());
                    statusMessage.setText(result);
                    refreshRouteAndMapData();
                    vehiclePanel.refreshList();
                }
                case "Modify route stops" -> promptModifyRouteStops(route);
                default -> {
                }
            }
        });
    }

    private void promptRenameRoute(Route route) {
        TextInputDialog dialog = new TextInputDialog(route.getName());
        dialog.setTitle("Rename Route");
        dialog.setHeaderText("Rename route");
        dialog.setContentText("New route name:");
        dialog.showAndWait().ifPresent(newName -> {
            String result = controller.getGameService().renameRoute(route.getId(), newName);
            statusMessage.setText(result);
            refreshRouteAndMapData();
        });
    }

    private void promptModifyRouteStops(Route route) {
        List<Depot> availableDepots = controller.getGameService().getAvailableDepots();
        if (availableDepots.size() < 2) {
            statusMessage.setText("Need at least 2 depots to modify route.");
            return;
        }

        List<Depot> sequence = new ArrayList<>(route.getDepots());
        boolean editing = true;
        while (editing) {
            String current = sequence.stream().map(Depot::getName).reduce((a, b) -> a + " -> " + b).orElse("(none)");
            ChoiceDialog<String> actionDialog = new ChoiceDialog<>(
                "Done",
                List.of("Add stop", "Remove last stop", "Done")
            );
            actionDialog.setTitle("Modify Route Stops");
            actionDialog.setHeaderText("Current: " + current);
            actionDialog.setContentText("Action:");
            String action = actionDialog.showAndWait().orElse("Done");

            switch (action) {
                case "Add stop" -> {
                    List<String> depotNames = availableDepots.stream().map(Depot::getName).toList();
                    ChoiceDialog<String> depotChoice = new ChoiceDialog<>(depotNames.getFirst(), depotNames);
                    depotChoice.setTitle("Add Route Stop");
                    depotChoice.setHeaderText("Select a depot to append");
                    depotChoice.setContentText("Depot:");
                    depotChoice.showAndWait().ifPresent(selectedName -> {
                        Depot selected = availableDepots.stream()
                            .filter(depot -> depot.getName().equals(selectedName))
                            .findFirst()
                            .orElse(null);
                        if (selected != null) {
                            sequence.add(selected);
                        }
                    });
                }
                case "Remove last stop" -> {
                    if (sequence.size() <= 2) {
                        statusMessage.setText("Route must keep at least 2 stops.");
                    } else {
                        sequence.removeLast();
                    }
                }
                default -> editing = false;
            }
        }

        if (sequence.size() < 2) {
            statusMessage.setText("Route must have at least 2 stops.");
            return;
        }

        String result = controller.getGameService().updateRouteStops(route.getId(), sequence);
        statusMessage.setText(result);
        refreshRouteAndMapData();
        vehiclePanel.refreshList();
    }

    private void togglePanel(Region panel) {
        if (activePanel == routePanel && panel != routePanel && isCreatingRoute) {
            isCreatingRoute = false;
            pendingRouteDepots.clear();
            mapView.setPendingRoute(null);
            routePanel.setCreationMode(false, null);
            setupRouteCreationHandlers();
            statusMessage.setText("Route creation cancelled.");
            mapView.refresh();
        }

        if (panel == null || panel == activePanel) {
            activePanel = null;
        } else {
            activePanel = panel;
        }
        showDockContent();
    }

    private void showDockContent() {
        footerRow.getChildren().clear();
        footerRow.getChildren().add(createModeDock());
        if (activePanel != null) {
            activePanelScroller.setContent(activePanel);
            footerRow.getChildren().add(activePanelScroller);
        }
        dockColumn.getChildren().setAll(footerRow);
    }

    private VBox createModeDock() {
        Button tunnelsBtn = menuButton("Tunnels");
        Button economyBtn = menuButton("Economy");
        Button buildBtn = menuButton("Build");
        Button vehiclesBtn = menuButton("Vehicles");
        Button infrastructureBtn = menuButton("Infrastructure");

        tunnelsBtn.setOnAction(e -> togglePanel(routePanel));
        economyBtn.setOnAction(e -> togglePanel(economyPanel));
        buildBtn.setOnAction(e -> togglePanel(buildPanel));
        vehiclesBtn.setOnAction(e -> {
            vehiclePanel.refreshList();
            togglePanel(vehiclePanel);
        });
        infrastructureBtn.setOnAction(e -> togglePanel(infrastructurePanel));

        VBox firstGroup = createDockSection("View", tunnelsBtn, economyBtn);
        VBox secondGroup = createDockSection("Build", buildBtn, vehiclesBtn, infrastructureBtn);
        HBox groups = new HBox(14, firstGroup, secondGroup);
        groups.setAlignment(Pos.CENTER_LEFT);
        VBox dock = new VBox(groups);
        dock.getStyleClass().add("mode-dock");
        return dock;
    }

    private VBox createDockSection(String title, Button... buttons) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dock-section-label");
        HBox row = new HBox(10, buttons);
        row.getStyleClass().add("dock-section-buttons");
        row.setAlignment(Pos.CENTER_LEFT);
        VBox section = new VBox(8, titleLabel, row);
        section.getStyleClass().add("dock-section");
        return section;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(12);
        statusBar.setAlignment(Pos.CENTER_LEFT); 
        statusBar.getStyleClass().add("top-status-bar");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        moneyLabel = new Label();
        incomeLabel = new Label();
        expensesLabel = new Label();
        workersLabel = new Label();
        vehiclesLabel = new Label();

        updateStatusBar();

        speedValueLabel = new Label("1x");
        speedValueLabel.getStyleClass().add("segmented-value");
        Button normalSpeedBtn = new Button("1x");
        Button fastSpeedBtn = new Button("2x");
        Button veryFastSpeedBtn = new Button("4x");
        Button pauseBtn = new Button("Pause");
        Button saveBtn = new Button("Save");
        for (Button button : List.of(normalSpeedBtn, fastSpeedBtn, veryFastSpeedBtn, pauseBtn)) {
            button.getStyleClass().add("btn-segment");
        }
        saveBtn.getStyleClass().add("btn-toolbar");

        normalSpeedBtn.setOnAction(e -> setSimulationSpeed("normal", "1x"));
        fastSpeedBtn.setOnAction(e -> setSimulationSpeed("fast", "2x"));
        veryFastSpeedBtn.setOnAction(e -> setSimulationSpeed("very_fast", "4x"));
        pauseBtn.setOnAction(e -> setSimulationSpeed("pause", "paused"));
        saveBtn.setOnAction(e -> openSaveSlotDialog());
        statusMessage.getStyleClass().add("status-info");

        moneyLabel.getStyleClass().add("hud-stat-value");
        incomeLabel.getStyleClass().add("hud-stat-sub");
        expensesLabel.getStyleClass().add("hud-stat-sub");
        workersLabel.getStyleClass().add("hud-stat-value");
        vehiclesLabel.getStyleClass().add("hud-stat-value");

        Label wLbl = new Label("workers :");
        wLbl.getStyleClass().add("hud-stat-label");
        Label vLbl = new Label("vehicles :");
        vLbl.getStyleClass().add("hud-stat-label");

        HBox leftStats = new HBox(
            24,
            moneyLabel,
            new HBox(8, incomeLabel, expensesLabel),
            new HBox(6, wLbl, workersLabel),
            new HBox(6, vLbl, vehiclesLabel)
        );
        leftStats.setAlignment(Pos.CENTER_LEFT);

        HBox centerStatus = new HBox(16, statusMessage);
        centerStatus.setAlignment(Pos.CENTER_LEFT);

        HBox speedCluster = new HBox(4, normalSpeedBtn, fastSpeedBtn, veryFastSpeedBtn, pauseBtn);
        speedCluster.getStyleClass().add("segmented-control");
        speedCluster.setAlignment(Pos.CENTER_LEFT);

        Label sLbl = new Label("speed :");
        sLbl.getStyleClass().add("hud-stat-label");
        HBox speedPanel = new HBox(8, sLbl, speedCluster);
        speedPanel.setAlignment(Pos.CENTER_LEFT);

        HBox gameButtons = new HBox(6, saveBtn);
        gameButtons.setAlignment(Pos.CENTER_LEFT);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        statusBar.getChildren().addAll(
            leftStats,
            spacer, 
            centerStatus,
            spacer2,
            speedPanel,
            new Region() { { setMinWidth(16); } },
            gameButtons
        );
        return statusBar;
    }

    public void updateStatusBar() {
        GameService.FinancialSnapshot snapshot = controller.getGameService().getFinancialSnapshot();

        if (moneyLabel == null || incomeLabel == null || expensesLabel == null) {
            return;
        }

        moneyLabel.setText(formatMoney(snapshot.balance()));
        incomeLabel.setText("+" + formatMoney(snapshot.totalIncome()) + "/min");
        expensesLabel.setText("-" + formatMoney(snapshot.totalExpenses()) + "/min");

        moneyLabel.getStyleClass().removeAll("money-good", "money-bad");
        moneyLabel.getStyleClass().add(snapshot.lowBalanceWarning() ? "money-bad" : "money-good");

        Game game = controller.getGameService().getCurrentGame();
        if (speedValueLabel != null && game != null) {
            workersLabel.setText(Integer.toString(estimateWorkers(game)));
            vehiclesLabel.setText(Integer.toString(game.getUnits().size()));
            speedValueLabel.setText(switch (game.getClock().getSpeed()) {
                case "fast" -> "2x";
                case "very_fast" -> "4x";
                case "pause" -> "paused";
                default -> "1x";
            });
        }

        economyPanel.updateFinancialSnapshot(snapshot);
    }

    private VBox createInfoPanel() {
        VBox infoPanel = new VBox(10);
        infoPanel.setPrefWidth(300);
        infoPanel.setMaxWidth(300);
        infoPanel.getStyleClass().add("info-panel");

        infoTitleLabel = new Label();
        infoTitleLabel.getStyleClass().add("info-panel-title");
        infoTitleLabel.setWrapText(true);
        infoSubtitleLabel = new Label();
        infoSubtitleLabel.getStyleClass().add("info-panel-subtitle");
        infoSubtitleLabel.setWrapText(true);

        HBox row1 = createInfoRow();
        infoKey1Label = (Label) row1.getChildren().get(0);
        infoValue1Label = (Label) row1.getChildren().get(2);
        HBox row2 = createInfoRow();
        infoKey2Label = (Label) row2.getChildren().get(0);
        infoValue2Label = (Label) row2.getChildren().get(2);
        HBox row3 = createInfoRow();
        infoKey3Label = (Label) row3.getChildren().get(0);
        infoValue3Label = (Label) row3.getChildren().get(2);

        infoPanel.getChildren().addAll(infoTitleLabel, infoSubtitleLabel, row1, row2, row3);
        updateInfoPanel();
        return infoPanel;
    }

    private HBox createInfoRow() {
        Label key = new Label();
        key.getStyleClass().add("info-panel-key");
        Label value = new Label();
        value.getStyleClass().add("info-panel-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(8, key, spacer, value);
        row.getStyleClass().add("info-panel-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void setInfoRow(Label keyLabel, Label valueLabel, String key, String value) {
        keyLabel.setText(key);
        valueLabel.setText(value);
    }

    private VBox createHudStat(String title, Label valueLabel) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("hud-stat-label");
        valueLabel.getStyleClass().add("hud-stat-value");
        VBox stat = new VBox(6, titleLabel, valueLabel);
        stat.getStyleClass().add("hud-stat");
        return stat;
    }

    private Label pillCaption(String text) {
        Label caption = new Label(text);
        caption.getStyleClass().add("pill-caption");
        return caption;
    }

    private HBox statusPill(Label label) {
        HBox pill = new HBox(label);
        pill.getStyleClass().add("hud-status-pill");
        pill.setAlignment(Pos.CENTER_LEFT);
        return pill;
    }

    private Button menuButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("btn-menu");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private int estimateWorkers(Game game) {
        int workers = 0;
        for (Hub hub : game.getGameMap().getHubs()) {
            if (hub instanceof Settlement settlement) {
                workers += settlement.getPopulation();
            }
        }
        return workers;
    }

    private void startFinancialRefreshTimer() {
        if (financialRefreshTimer != null) {
            return;
        }

        financialRefreshTimer = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            controller.getGameService().advanceSimulationFrame(0.1f);
            mapView.setMovementInterpolationAlpha(controller.getGameService().getMovementInterpolationAlpha());
            refreshMap();
            updateStatusBar();
            updateInfoPanel();
        }));
        financialRefreshTimer.setCycleCount(Timeline.INDEFINITE);
        financialRefreshTimer.play();
    }

    private String formatMoney(float amount) {
        return String.format("$%.2f", amount);
    }

    private void promptRenameSelectedDepot() {
        if (!(selectedHub instanceof Depot depot)) {
            statusMessage.setText("Select a depot on the map first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(depot.getName());
        dialog.setTitle("Rename Depot");
        dialog.setHeaderText("Rename depot");
        dialog.setContentText("New depot name:");
        dialog.showAndWait().ifPresent(newName -> {
            String result = controller.getGameService().renameDepot(depot.getId(), newName);
            statusMessage.setText(result);
            refreshRouteAndMapData();
            updateInfoPanel();
        });
    }

    private void refreshRouteAndMapData() {
        Game game = controller.getGameService().getCurrentGame();
        if (game == null) {
            return;
        }
        routePanel.setRoutes(game.getRoutes());
        mapView.setVisibleRoutes(game.getRoutes());
        mapView.setVisibleUnits(game.getUnits());
        mapView.refresh();
    }

    private void setSimulationSpeed(String speedKey, String label) {
        Game game = controller.getGameService().getCurrentGame();
        if (game == null) {
            return;
        }
        game.getClock().setSpeed(speedKey);
        statusMessage.setText("Simulation speed set to " + label + ".");
        updateStatusBar();
    }

    private void openSaveSlotDialog() {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(1, List.of(1, 2, 3, 4, 5));
        dialog.setTitle("Save Game");
        dialog.setHeaderText("Select a save slot");
        dialog.setContentText("Slot:");
        dialog.showAndWait().ifPresent(slot -> {
            String result = controller.getGameService().saveGameToSlot(slot);
            statusMessage.setText(result);
        });
    }

    private void updateSelectionFromTile(int tileX, int tileY) {
        Game game = controller.getGameService().getCurrentGame();
        if (game == null) {
            return;
        }
        Tile tile = game.getGameMap().getTileAt(tileX, tileY);
        if (tile == null) {
            return;
        }
        selectedHub = game.getGameMap().getHubAt(tile);
        mapView.setSelectedHub(selectedHub);
        updateInfoPanel();
    }

    private void updateInfoPanel() {
        if (infoTitleLabel == null || infoKey1Label == null || infoValue1Label == null) {
            return;
        }

        Game game = controller.getGameService().getCurrentGame();
        if (game == null) {
            infoTitleLabel.setText("No game loaded");
            infoSubtitleLabel.setText("Start or load a guild to see your network summary.");
            setInfoRow(infoKey1Label, infoValue1Label, "", "");
            setInfoRow(infoKey2Label, infoValue2Label, "", "");
            setInfoRow(infoKey3Label, infoValue3Label, "", "");
            return;
        }

        if (selectedHub instanceof Depot depot) {
            infoTitleLabel.setText("Depot: " + depot.getName());
            infoSubtitleLabel.setText("Distribution node and passenger stop.");
            setInfoRow(infoKey1Label, infoValue1Label, "Connected hub", depot.getHub().getName());
            setInfoRow(infoKey2Label, infoValue2Label, "Stored goods", Integer.toString(depot.getStoredGoods().size()));
            setInfoRow(infoKey3Label, infoValue3Label, "Waiting passengers", Integer.toString(depot.getWaitingPassengers()));
            infrastructurePanel.setDepotSelected(
                depot.getName(),
                depot.getHub().getName(),
                depot.getStoredGoods().size(),
                depot.getWaitingPassengers()
            );
            return;
        }

        if (selectedHub instanceof Settlement settlement) {
            infoTitleLabel.setText("Settlement: " + settlement.getName());
            infoSubtitleLabel.setText("Population center feeding your transport demand.");
            setInfoRow(infoKey1Label, infoValue1Label, "Population", Integer.toString(settlement.getPopulation()));
            setInfoRow(infoKey2Label, infoValue2Label, "Passenger demand", Long.toString(Math.round(settlement.getPassengerDemand())));
            setInfoRow(infoKey3Label, infoValue3Label, "Connected depots", Integer.toString(settlement.getDepots().size()));
            infrastructurePanel.setHubSelected(
                "Settlement",
                settlement.getName(),
                "Population: " + settlement.getPopulation(),
                "Connected depots: " + settlement.getDepots().size()
            );
            return;
        }

        if (selectedHub instanceof IndustrialFacility facility) {
            infoTitleLabel.setText(facility.getName());
            infoSubtitleLabel.setText("Industrial production feeding the guild economy.");
            setInfoRow(infoKey1Label, infoValue1Label, "Produces", facility.getProducedGood());
            setInfoRow(infoKey2Label, infoValue2Label, "Production rate", Float.toString(facility.getProductionRate()));
            setInfoRow(infoKey3Label, infoValue3Label, "Stored goods", Integer.toString(facility.getStoredGoods().size()));
            infrastructurePanel.setHubSelected(
                "Facility",
                facility.getName(),
                "Produces: " + facility.getProducedGood(),
                "Stored goods: " + facility.getStoredGoods().size()
            );
            return;
        }

        infoTitleLabel.setText("Network Overview");
        infoSubtitleLabel.setText("A quick snapshot of the underground transport guild.");
        setInfoRow(infoKey1Label, infoValue1Label, "Hubs", Integer.toString(game.getGameMap().getHubs().size()));
        setInfoRow(infoKey2Label, infoValue2Label, "Routes", Integer.toString(game.getRoutes().size()));
        setInfoRow(infoKey3Label, infoValue3Label, "Units", Integer.toString(game.getUnits().size()));
        infrastructurePanel.setEmptyState();
    }
}
