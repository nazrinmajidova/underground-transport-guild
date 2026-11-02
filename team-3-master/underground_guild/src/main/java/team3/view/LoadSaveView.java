package team3.view;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import team3.controller.AppController;

public class LoadSaveView {
    private final AppController controller;
    private static final int SLOT_COUNT = 5;

    public LoadSaveView(AppController controller) {
        this.controller = controller;
    }

    public Parent build() {
        Label title = new Label("Load Save");
        title.getStyleClass().add("title");

        VBox slots = new VBox(8);
        slots.setAlignment(Pos.CENTER);
        for (int i = 1; i <= SLOT_COUNT; i++) {
            slots.getChildren().add(buildSlotRow(i));
        }

        Button back = new Button("Back");
        back.getStyleClass().add("btn-secondary");
        back.setPrefWidth(220);
        back.setOnAction(e -> {
            Stage stage = (Stage) back.getScene().getWindow();
            controller.showWelcomeView(stage);
        });

        VBox root = new VBox(14, title, slots, back);
        root.setAlignment(Pos.CENTER);
        root.setFillWidth(false);
        root.getStyleClass().add("screen");
        return root;
    }

    private HBox buildSlotRow(int slotNumber) {
        Label slotLabel = new Label("Save " + slotNumber);
        slotLabel.setPrefWidth(80);

        String metadata = controller.getGameService().getSaveSlotMetadata(slotNumber);
        boolean hasSave = controller.getGameService().hasSaveSlot(slotNumber);

        Label metaLabel = new Label(metadata);
        metaLabel.getStyleClass().add("muted");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button loadBtn = new Button("Load");
        loadBtn.getStyleClass().add("btn-primary");
        loadBtn.setDisable(!hasSave);
        loadBtn.setOnAction(e -> {
            Stage stage = (Stage) loadBtn.getScene().getWindow();
            String result = controller.loadSave(slotNumber, stage);
            if (!result.startsWith("Loaded")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText(result);
                alert.showAndWait();
            }
        });

        HBox row = new HBox(12, slotLabel, metaLabel, spacer, loadBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("save-slot");
        row.setPrefWidth(400);
        return row;
    }
}
