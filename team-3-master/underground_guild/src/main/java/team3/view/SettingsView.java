package team3.view;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import team3.controller.AppController;

public class SettingsView {
    private final AppController controller;

    public SettingsView(AppController controller) {
        this.controller = controller;
    }

    public Parent build() {
        Label title = new Label("Settings");
        title.getStyleClass().add("title");

        VBox settingsPanel = new VBox(16,
            buildSliderRow("Master volume", 75),
            buildSliderRow("Music",         50),
            buildSliderRow("SFX",           80)
        );
        settingsPanel.getStyleClass().add("panel");

        VBox root = new VBox(16,
            title,
            settingsPanel,
            buildBackButton()
        );
        root.setAlignment(Pos.CENTER);
        root.setFillWidth(false);
        root.getStyleClass().add("screen");
        return root;
    }

    private HBox buildSliderRow(String labelText, double defaultValue) {
        Label lbl = new Label(labelText);
        lbl.setPrefWidth(140);

        Slider slider = new Slider(0, 100, defaultValue);
        slider.setPrefWidth(220);
        slider.setShowTickMarks(false);

        Label valueLabel = new Label((int) defaultValue + "%");
        valueLabel.setPrefWidth(40);

        slider.valueProperty().addListener((obs, oldVal, newVal) ->
            valueLabel.setText(newVal.intValue() + "%"));

        HBox row = new HBox(14, lbl, slider, valueLabel);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    private Button buildBackButton() {
        Button back = new Button("Back");
        back.getStyleClass().add("btn-secondary");
        back.setPrefWidth(220);
        back.setOnAction(e -> {
            Stage stage = (Stage) back.getScene().getWindow();
            controller.showWelcomeView(stage);
        });
        VBox.setVgrow(back, Priority.NEVER);
        return back;
    }
}
