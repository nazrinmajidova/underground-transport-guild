package team3.view;

import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import team3.model.Depot;
import team3.model.Route;

public class RouteManagementView extends VBox {
    private final VBox routeItems = new VBox(0);
     private Label detailFrom;
    private Label detailTo;
    private Label detailDistance;
    private Label detailVehicles;
    private Consumer<Route> routeSelectionHandler;
    private Route selectedRoute;
 
    private Button newRouteBtn;
    private VBox creationBox;
    private Label creationStatus;
    private Button confirmBtn;
    private Button cancelBtn;
    private TextField routeNameField;
    
    private Runnable onNewRouteRequested;
    private Runnable onCancelRoute;
    private Consumer<String> onConfirmRoute;
    private Consumer<Route> onAssignVehicleRequested;
    private Consumer<Route> onDeleteRouteRequested;
    private Consumer<Route> onRenameRouteRequested;
    private Runnable onBuyVehicleRequested;

    public RouteManagementView() {
        super(0);
        setPadding(new Insets(0));
        getStyleClass().add("panel-compact");
        setPrefWidth(400);
        setMaxWidth(Region.USE_PREF_SIZE);
 
        HBox topBar = buildTopBar();
        HBox body = new HBox(0);
 
        VBox routeList    = buildRouteList();
        VBox routeDetails = buildRouteDetails();
 
        Region divider = new Region();
        divider.setPrefWidth(1);
        divider.getStyleClass().add("divider-vertical");
 
        body.getChildren().addAll(routeList, divider, routeDetails);
        HBox.setHgrow(routeList,    Priority.SOMETIMES);
        HBox.setHgrow(routeDetails, Priority.ALWAYS);
 
        getChildren().addAll(topBar, body);
    }
 
 
    private HBox buildTopBar() {
        Label title = new Label("Routes");
        title.getStyleClass().add("panel-header");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button buyVehicleButton = new Button("Buy Vehicle");
        buyVehicleButton.getStyleClass().add("btn-secondary");
        buyVehicleButton.setOnAction(event -> {
            if (onBuyVehicleRequested != null) {
                onBuyVehicleRequested.run();
            }
        });

        HBox bar = new HBox(title, spacer, buyVehicleButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("row-divider");
        return bar;
    }
 
    private VBox buildRouteList() {
        Label header = new Label("Route list");
        header.getStyleClass().add("panel-header");
        header.setMaxWidth(Double.MAX_VALUE);
        routeItems.getChildren().setAll(routePlaceholder("No routes available"));
 
        Region hDivider = new Region();
        hDivider.setPrefHeight(1);
        hDivider.getStyleClass().add("divider-horizontal");
 
        newRouteBtn = new Button("+ New route");
        newRouteBtn.getStyleClass().add("btn-subtle");
        newRouteBtn.setMaxWidth(Double.MAX_VALUE);
        newRouteBtn.setOnAction(e -> {
            if (onNewRouteRequested != null) onNewRouteRequested.run();
        });

        creationStatus = new Label("Select depots (0)");
        creationStatus.getStyleClass().add("muted");

        routeNameField = new TextField();
        routeNameField.setPromptText("Route name");
        routeNameField.getStyleClass().add("form-control");

        confirmBtn = new Button("Confirm");
        confirmBtn.getStyleClass().add("btn-primary");
        confirmBtn.setDisable(true);   
        confirmBtn.setOnAction(e -> {
            if (onConfirmRoute != null) onConfirmRoute.accept(routeNameField.getText());
        });

        cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> {
            if (onCancelRoute != null) onCancelRoute.run();
        });

        HBox creationActions = new HBox(4, confirmBtn, cancelBtn);
        creationActions.setAlignment(Pos.CENTER);

        creationBox = new VBox(4, routeNameField, creationStatus, creationActions);
        creationBox.getStyleClass().add("panel");
        creationBox.setVisible(false);
        creationBox.setManaged(false);

        VBox list = new VBox(0, header, routeItems, hDivider, newRouteBtn, creationBox);
        list.setPrefWidth(145);
        list.getStyleClass().add("list-column");
        return list;
    }
 
    private VBox buildRouteDetails() {
        Label header = new Label("Route details");
        header.getStyleClass().add("panel-header");
        header.setMaxWidth(Double.MAX_VALUE);
 
        detailFrom     = detailLabel("From : —");
        detailTo       = detailLabel("To : —");
        detailDistance = detailLabel("Distance : —");
        detailVehicles = detailLabel("Vehicles : 0");

        Button renameBtn = new Button("Modify");
        Button deleteBtn = new Button("Delete");
        renameBtn.getStyleClass().add("btn-secondary");
        deleteBtn.getStyleClass().add("btn-secondary");
        renameBtn.setOnAction(event -> {
            if (selectedRoute != null && onRenameRouteRequested != null) {
                onRenameRouteRequested.accept(selectedRoute);
            }
        });
        deleteBtn.setOnAction(event -> {
            if (selectedRoute != null && onDeleteRouteRequested != null) {
                onDeleteRouteRequested.accept(selectedRoute);
            }
        });

        HBox actions = new HBox(8, renameBtn, deleteBtn);
        actions.setPadding(new Insets(4, 8, 4, 8));

        VBox details = new VBox(0, header,
                detailFrom, detailTo, detailDistance, detailVehicles, actions);
        details.setPadding(new Insets(0));
        details.setPrefWidth(240);
        return details;
    }
 
 
    private Button routeItem(String name) {
        Button btn = new Button(name);
        btn.getStyleClass().add("item-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setWrapText(true);
        return btn;
    }

    private Label routePlaceholder(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("empty-state");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
 
    private Label detailLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("table-row");
        lbl.getStyleClass().add("table-value");
        lbl.setWrapText(true);
        return lbl;
    }
 
    public void showRouteDetails(String from, String to, String distance, String vehicles) {
        detailFrom    .setText("From : " + from);
        detailTo      .setText("To : "   + to);
        detailDistance.setText("Distance : "  + distance);
        detailVehicles.setText("Vehicles : "  + vehicles);
    }

    public void setRouteSelectionHandler(Consumer<Route> handler) {
        this.routeSelectionHandler = handler;
    }

    public void setOnNewRouteRequested(Runnable handler) { this.onNewRouteRequested = handler; }
    public void setOnCancelRoute(Runnable handler) { this.onCancelRoute = handler; }
    public void setOnConfirmRoute(Consumer<String> handler) { this.onConfirmRoute = handler; }
    public void setOnAssignVehicleRequested(Consumer<Route> handler) { this.onAssignVehicleRequested = handler; }
    public void setOnDeleteRouteRequested(Consumer<Route> handler) { this.onDeleteRouteRequested = handler; }
    public void setOnRenameRouteRequested(Consumer<Route> handler) { this.onRenameRouteRequested = handler; }
    public void setOnBuyVehicleRequested(Runnable handler) { this.onBuyVehicleRequested = handler; }

    public void setRoutes(List<Route> routes) {
        routeItems.getChildren().clear();
        if (routes == null || routes.isEmpty()) {
            routeItems.getChildren().add(routePlaceholder("No routes available"));
            selectedRoute = null;
            showRouteDetails("-", "-", "none", "0");
            return;
        }

        for (Route route : routes) {
            Button routeButton = routeItem(route.getName());
            routeButton.setOnAction(event -> selectRoute(route));
            applyRouteSelectionStyle(routeButton, route.equals(selectedRoute));
            routeItems.getChildren().add(routeButton);
        }

        if (selectedRoute == null || !routes.contains(selectedRoute)) {
            selectRoute(routes.getFirst());
        } else {
            refreshRouteButtons();
        }
    }

    public void selectRoute(Route route) {
        if (route == null) return;

        selectedRoute = route;

        String from     = route.getOrigin()      == null ? "—" : route.getOrigin().getName();
        String to       = route.getDestination() == null ? "—" : route.getDestination().getName();
        String distance = Math.round(route.getDistance()) + " tiles";
        String vehicles = Integer.toString(route.getAssignedUnits().size());

        showRouteDetails(from, to, distance, vehicles);
        refreshRouteButtons();

        if (routeSelectionHandler != null) {
            routeSelectionHandler.accept(route);
        }
    }

    private void refreshRouteButtons() {
        for (var node : routeItems.getChildren()) {
            if (!(node instanceof Button btn)) continue;
            boolean isSelected = selectedRoute != null
                    && btn.getText().equals(selectedRoute.getName());
            applyRouteSelectionStyle(btn, isSelected);
        }
    }

    private void applyRouteSelectionStyle(Button button, boolean selected) {
        button.getStyleClass().remove("item-button-selected");
        if (selected) {
            button.getStyleClass().add("item-button-selected");
        }
    }

    /**
     * Toggles the panel into or out of Route Creation mode.
     */
    public void setCreationMode(boolean active, List<Depot> pending) {
        newRouteBtn.setVisible(!active);
        newRouteBtn.setManaged(!active);
        creationBox.setVisible(active);
        creationBox.setManaged(active);

        if (active) {
            routeNameField.clear();
            updatePendingDepots(pending);
        }
    }

    /**
     * Refreshes the display showing the user how many depots they've linked for a pending route.
     */
    public void updatePendingDepots(List<Depot> pending) {
        int count = (pending == null) ? 0 : pending.size();
        creationStatus.setText("Depots selected: " + count
                + (count < 2 ? " (need at least 2)" : " ✓"));
        confirmBtn.setDisable(count < 2);
    }
}
