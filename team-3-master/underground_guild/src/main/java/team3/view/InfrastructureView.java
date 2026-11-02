package team3.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Compact infrastructure panel for actions tied to the currently selected structure.
 */
public class InfrastructureView extends VBox {
    private final Label titleLabel = new Label("Infrastructure");
    private final Label line1Label = new Label("Select a depot or hub.");
    private final Label line2Label = new Label("");
    private final Label line3Label = new Label("");
    private final Button renameDepotButton = new Button("Rename Depot");

    public InfrastructureView() {
        super(0);
        setPadding(new Insets(0));
        getStyleClass().add("panel-compact");
        setPrefWidth(280);
        setMaxWidth(Region.USE_PREF_SIZE);

        titleLabel.getStyleClass().add("panel-header");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        line1Label.getStyleClass().add("table-row");
        line2Label.getStyleClass().add("table-row");
        line3Label.getStyleClass().add("table-row");
        line1Label.getStyleClass().add("table-value");
        line2Label.getStyleClass().add("table-value");
        line3Label.getStyleClass().add("table-value");
        line1Label.setWrapText(true);
        line2Label.setWrapText(true);
        line3Label.setWrapText(true);

        renameDepotButton.getStyleClass().add("btn-secondary");
        renameDepotButton.setDisable(true);

        HBox actions = new HBox(renameDepotButton);
        actions.setPadding(new Insets(8));

        getChildren().addAll(titleLabel, line1Label, line2Label, line3Label, actions);
        setEmptyState();
    }

    public void setOnRenameDepotRequested(Runnable callback) {
        renameDepotButton.setOnAction(event -> {
            if (callback != null) {
                callback.run();
            }
        });
    }

    public void setDepotSelected(String depotName, String hubName, int goodsCount, int waitingPassengers) {
        titleLabel.setText("Infrastructure");
        line1Label.getStyleClass().remove("empty-state");
        if (!line1Label.getStyleClass().contains("table-row")) line1Label.getStyleClass().add("table-row");
        line1Label.setText("Depot: " + depotName);
        line2Label.setText("Connected hub: " + hubName);
        line3Label.setText("Stored goods: " + goodsCount + " | Waiting passengers: " + waitingPassengers);
        renameDepotButton.setDisable(false);
    }

    public void setHubSelected(String hubType, String hubName, String summary, String detail) {
        titleLabel.setText("Infrastructure");
        line1Label.getStyleClass().remove("empty-state");
        if (!line1Label.getStyleClass().contains("table-row")) line1Label.getStyleClass().add("table-row");
        line1Label.setText(hubType + ": " + hubName);
        line2Label.setText(summary);
        line3Label.setText(detail);
        renameDepotButton.setDisable(true);
    }

    public void setEmptyState() {
        titleLabel.setText("Infrastructure");
        line1Label.getStyleClass().remove("table-row");
        if (!line1Label.getStyleClass().contains("empty-state")) line1Label.getStyleClass().add("empty-state");
        line1Label.setText("Select a depot or hub.");
        line2Label.setText("");
        line3Label.setText("");
        renameDepotButton.setDisable(true);
    }
}
