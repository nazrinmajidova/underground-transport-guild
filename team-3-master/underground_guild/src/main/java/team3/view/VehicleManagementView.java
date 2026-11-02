package team3.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import team3.controller.AppController;
import team3.model.CargoTransport;
import team3.model.Depot;
import team3.model.TransportUnit;
import java.util.List;
import java.util.Optional;

public class VehicleManagementView extends VBox{
    private Label detailId;
    private Label detailLevel;
    private Label detailRoute;
    private Label detailMaintenance;
    private MainGameView mainGameView;
    private VBox vehicleListContainer;
    private AppController controller;
    private TransportUnit selectedVehicle;

    public VehicleManagementView() {
        super(0);
        setPadding(new Insets(0));
        getStyleClass().add("panel-compact");
        setPrefWidth(400);
        setMaxWidth(Region.USE_PREF_SIZE);
 
        HBox topBar = buildTopBar();
        HBox body = new HBox(0);
 
        VBox vehicleList   = buildVehicleListWrapper();
        VBox vehicleDetail = buildVehicleDetail();
 
        Region divider = new Region();
        divider.setPrefWidth(1);
        divider.getStyleClass().add("divider-vertical");
 
        body.getChildren().addAll(vehicleList, divider, vehicleDetail);
        HBox.setHgrow(vehicleList,   Priority.SOMETIMES);
        HBox.setHgrow(vehicleDetail, Priority.ALWAYS);
 
        getChildren().addAll(topBar, body);
    }
 
    /**
     * Binds the application controller to the view, enabling interaction with the model.
     */
    public void setController(AppController controller) {
        this.controller = controller;
    }
 
    private HBox buildTopBar() {
        Label title = new Label("Vehicles");
        title.getStyleClass().add("panel-header");
 
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
 
        Button buyBtn = new Button("Buy new vehicle");
        buyBtn.getStyleClass().add("btn-secondary");
        HBox.setMargin(buyBtn, new Insets(3, 6, 3, 0));
 
        buyBtn.setOnAction(e -> openPurchaseDialog());

        HBox bar = new HBox(title, spacer, buyBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("row-divider");
        return bar;
    }
 
    /**
     * Spawns a dialog window permitting the user to purchase a new transport unit.
     */
    public void openPurchaseDialog() {
        if (controller == null || controller.getGameService().getCurrentGame() == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Purchase Transport Unit");
        dialog.setHeaderText("Select a vehicle to purchase:");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
            "Cargo - Standard ($1000, Cap: 40, Spd: 8, Maint: 100)", 
            "Cargo - Fast ($1300, Cap: 20, Spd: 16, Maint: 180)",
            "Passenger - Standard ($600, Cap: 50, Spd: 10, Maint: 100)", 
            "Passenger - Fast ($1000, Cap: 30, Spd: 20, Maint: 150)"
        );
        typeCombo.getSelectionModel().selectFirst();

        ComboBox<Depot> depotCombo = new ComboBox<>();
        List<Depot> depots = controller.getGameService().getAvailableDepots();
        depotCombo.getItems().addAll(depots);
        
        depotCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Depot item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        depotCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Depot item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        
        if (!depots.isEmpty()) depotCombo.getSelectionModel().selectFirst();

        TextField nameField = new TextField("New Transport");

        VBox content = new VBox(10, 
            new Label("Vehicle Type & Variant:"), typeCombo,
            new Label("Spawn Depot (Optional):"), depotCombo,
            new Label("Vehicle Name:"), nameField
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String selection = typeCombo.getValue();
            String category = selection.startsWith("Cargo") ? "Cargo" : "Passenger";
            String variant = selection.contains("Fast") ? "FAST" : "STANDARD";
            
            String msg = controller.getGameService().purchaseTransportUnit(
                category, variant, nameField.getText(), depotCombo.getValue()
            );
            
            Alert alert = new Alert(msg.contains("successful") ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            alert.setTitle("Purchase Result");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();

            refreshList();

            if (mainGameView != null) {
                mainGameView.updateStatusBar();
                mainGameView.refreshMap();
            }
        }
    }



    private VBox buildVehicleListWrapper() {
        Label header = new Label("Vehicle list");
        header.getStyleClass().add("panel-header");
        header.setMaxWidth(Double.MAX_VALUE);
 
        vehicleListContainer = new VBox(0);
        
        VBox list = new VBox(0, header, vehicleListContainer);
        list.setPrefWidth(150);
        list.getStyleClass().add("list-column");
        return list;
    }

    /**
     * Re-pulls the transport units from the game state to accurately display them.
     */
    public void refreshList() {
        vehicleListContainer.getChildren().clear();
        if (controller == null || controller.getGameService().getCurrentGame() == null) return;

        List<TransportUnit> units = controller.getGameService().getCurrentGame().getUnits();
        if (units.isEmpty()) {
            Label emptyLabel = new Label("No vehicles owned");
            emptyLabel.getStyleClass().add("empty-state");
            vehicleListContainer.getChildren().add(emptyLabel);
            showVehicleDetails("-", "-", "-", "-");
            return;
        }

        for (TransportUnit unit : units) {
            String typeStr = (unit instanceof CargoTransport) ? "Cargo" : "Passenger";
            Button btn = vehicleItem(String.format("%s\nType: %s", unit.getName(), typeStr));
            btn.setOnAction(e -> {
                selectedVehicle = unit;
                refreshVehicleSelectionStyles(btn);
                showVehicleDetails(
                    unit.getId().length() > 8 ? unit.getId().substring(0,8) : unit.getId(),
                    typeStr + " (L" + unit.getUpgradeLevel() + ")",
                    unit.getAssignedRoute() != null ? unit.getAssignedRoute().getName() : "None",
                    "$" + unit.getMaintenanceCost()
                );
            });
            vehicleListContainer.getChildren().add(btn);
        }
    }
 
    private VBox buildVehicleDetail() {
        Label header = new Label("Selected vehicle");
        header.getStyleClass().add("panel-header");
        header.setMaxWidth(Double.MAX_VALUE);
 
        detailId          = detailLabel("ID : 1");
        detailLevel       = detailLabel("level : x");
        detailRoute       = detailLabel("assigned route : xx");
        detailMaintenance = detailLabel("maintenance : $xx");
 
        Button upgradeBtn = new Button("Upgrade");
        Button sellBtn    = new Button("Sell");
        upgradeBtn.getStyleClass().add("btn-secondary");
        sellBtn.getStyleClass().add("btn-secondary");
        upgradeBtn.setOnAction(e -> upgradeSelectedVehicle());
        sellBtn.setOnAction(e -> sellSelectedVehicle());
 
        HBox actions = new HBox(8, upgradeBtn, sellBtn);
        actions.setPadding(new Insets(4, 8, 4, 8));
 
        VBox detail = new VBox(0, header,
                detailId, detailLevel, detailRoute, detailMaintenance, actions);
        return detail;
    }
 
 
    private Button vehicleItem(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("item-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setWrapText(true);
        return btn;
    }
 
    private Label detailLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("table-row");
        return lbl;
    }

    private void refreshVehicleSelectionStyles(Button selectedButton) {
        for (var node : vehicleListContainer.getChildren()) {
            if (node instanceof Button button) {
                button.getStyleClass().remove("item-button-selected");
            }
        }
        selectedButton.getStyleClass().add("item-button-selected");
    }
 
    public void showVehicleDetails(String id, String level,
                                   String route, String maintenance) {
        detailId         .setText("ID : "                + id);
        detailLevel      .setText("level : "             + level);
        detailRoute      .setText("assigned route : "    + route);
        detailMaintenance.setText("maintenance : "       + maintenance);
    }

    private void sellSelectedVehicle() {
        if (selectedVehicle == null || controller == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Select a vehicle first.");
            alert.showAndWait();
            return;
        }

        String result = controller.getGameService().sellTransportUnit(selectedVehicle.getId());
        Alert alert = new Alert(result.startsWith("Sold") ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(result);
        alert.showAndWait();

        if (result.startsWith("Sold")) {
            selectedVehicle = null;
            refreshList();
            showVehicleDetails("-", "-", "-", "-");
            if (mainGameView != null) {
                mainGameView.updateStatusBar();
                mainGameView.refreshMap();
            }
        }
    }

    private void upgradeSelectedVehicle() {
        if (selectedVehicle == null || controller == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Select a vehicle first.");
            alert.showAndWait();
            return;
        }

        String result = controller.getGameService().upgradeTransportUnit(selectedVehicle.getId());
        Alert alert = new Alert(result.startsWith("Upgraded") ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(result);
        alert.showAndWait();

        if (result.startsWith("Upgraded")) {
            refreshList();
            showVehicleDetails(
                selectedVehicle.getId().length() > 8 ? selectedVehicle.getId().substring(0, 8) : selectedVehicle.getId(),
                (selectedVehicle instanceof CargoTransport ? "Cargo" : "Passenger") + " (L" + selectedVehicle.getUpgradeLevel() + ")",
                selectedVehicle.getAssignedRoute() != null ? selectedVehicle.getAssignedRoute().getName() : "None",
                "$" + selectedVehicle.getMaintenanceCost()
            );
            if (mainGameView != null) {
                mainGameView.updateStatusBar();
                mainGameView.refreshMap();
            }
        }
    }

    public void setMainGameView(MainGameView mainGameView){
        this.mainGameView = mainGameView;
    }
}
