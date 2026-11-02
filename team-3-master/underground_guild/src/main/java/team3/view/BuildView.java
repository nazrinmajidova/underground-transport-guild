package team3.view;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class BuildView extends VBox{
    public enum BuildMode {
        NONE,
        TUNNEL,
        DEPOT,
        REMOVE
    }

    private final Label selectedModeLabel;
    private BuildMode selectedMode = BuildMode.NONE;
    private Consumer<BuildMode> modeChangedListener;

    public BuildView() {
        super(0);
        setPadding(new Insets(0));
        getStyleClass().add("panel-compact");
        setPrefWidth(250);
        setMaxWidth(Region.USE_PREF_SIZE);
 
        Label title = new Label("Build mode");
        title.setMaxWidth(Double.MAX_VALUE);
        title.getStyleClass().add("panel-header");
 
        Button selectBtn = actionButton("Select");
        Button tunnelBtn = actionButton("Place tunnel");
        Button depotBtn  = actionButton("Place depot");
        Button removeBtn = actionButton("Remove");

        HBox selectRow  = buildActionRow(selectBtn, "mode");
        HBox tunnelRow  = buildActionRow(tunnelBtn, "cost : 50");
        HBox depotRow   = buildActionRow(depotBtn,  "cost : 500");
        HBox removeRow  = buildActionRow(removeBtn, "refund : ??");
 
        selectedModeLabel = new Label("Selected mode: None");
        selectedModeLabel.getStyleClass().add("selected-mode");
        selectedModeLabel.setMaxWidth(Double.MAX_VALUE);
 
        getChildren().addAll(title, selectRow, tunnelRow, depotRow, removeRow, selectedModeLabel);
 
        selectBtn.setOnAction(e -> setSelectedMode(BuildMode.NONE));
        tunnelBtn.setOnAction(e -> setSelectedMode(BuildMode.TUNNEL));
        depotBtn .setOnAction(e -> setSelectedMode(BuildMode.DEPOT));
        removeBtn.setOnAction(e -> setSelectedMode(BuildMode.REMOVE));

        setSelectedMode(BuildMode.NONE);
    }
 
    private Button actionButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("btn-subtle");
        btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS);
        return btn;
    }

    private HBox buildActionRow(Button actionBtn, String costText) {
        Label costLabel = new Label(costText);
        costLabel.getStyleClass().add("muted");
 
        HBox row = new HBox(actionBtn, costLabel);
        row.getStyleClass().add("row-divider");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
 
    public void setSelectedMode(BuildMode mode) {
        if (mode == null) {
            return;
        }

        selectedMode = mode;
        String label = switch (mode) {
            case NONE -> "Select";
            case TUNNEL -> "Place tunnel";
            case DEPOT -> "Place depot";
            case REMOVE -> "Remove";
        };

        selectedModeLabel.setText("Selected mode: " + label);
        if (modeChangedListener != null) {
            modeChangedListener.accept(mode);
        }
    }

    public BuildMode getSelectedMode() {
        return selectedMode;
    }

    public void setOnModeChanged(Consumer<BuildMode> listener) {
        this.modeChangedListener = listener;
    }
}
 
