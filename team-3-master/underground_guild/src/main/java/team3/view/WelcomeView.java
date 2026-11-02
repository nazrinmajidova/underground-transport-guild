package team3.view;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import team3.controller.AppController;
import team3.util.AppMetadata;

/**
 * Startup menu for creating games, loading save slots, and opening settings.
 */
public class WelcomeView {

    private final AppController controller;

    public WelcomeView(AppController controller) {
        this.controller = controller;
    }

    public Parent build() {
        Label title = new Label(AppMetadata.APPLICATION_TITLE);
        title.getStyleClass().add("title");
        title.setWrapText(true);

        Label credits = new Label(AppMetadata.CREDITS_TEXT);
        credits.getStyleClass().add("muted");

        Button newGameButton = createActionButton("New Game", true);
        newGameButton.setOnAction(event -> {
            Stage stage = (Stage) newGameButton.getScene().getWindow();
            controller.handleStartNewGame(stage);
        });

        Button loadSaveButton = createActionButton("Load Save", false);
        loadSaveButton.setOnAction(event -> {
            Stage stage = (Stage) loadSaveButton.getScene().getWindow();
            controller.showLoadSaveView(stage);
        });
        
        Button settingsButton = createActionButton("Settings", false);
        settingsButton.setOnAction(event -> {
            Stage stage = (Stage) settingsButton.getScene().getWindow();
            controller.showSettingsView(stage);
        });

        Button exitButton = createActionButton("Exit", false);
        exitButton.setOnAction(event -> {
            Stage stage = (Stage) exitButton.getScene().getWindow();
            stage.close();
        });

        VBox center = new VBox(18, title, newGameButton, loadSaveButton, settingsButton, exitButton);
        center.setAlignment(Pos.CENTER);
        center.setFillWidth(false);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("screen");
        root.setCenter(center);
        BorderPane.setAlignment(center, Pos.CENTER);

        Region topSpacer = new Region();
        topSpacer.setMinHeight(40);
        root.setTop(topSpacer);

        BorderPane.setAlignment(credits, Pos.CENTER_LEFT);
        root.setBottom(credits);
        return root;
    }

    private Button createActionButton(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "btn-primary" : "btn-secondary");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefWidth(280);
        VBox.setVgrow(button, Priority.NEVER);
        return button;
    }
}
