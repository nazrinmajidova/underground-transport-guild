package team3.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import team3.controller.AppController;
import team3.model.Game;
import team3.service.GameService;

public class GameOverView {

    public static StackPane build(AppController controller, Stage stage) {
        GameService service = controller.getGameService();
        Game game = service.getCurrentGame();

        Label title = new Label("GAME OVER");
        title.getStyleClass().add("game-over-title");

        Label subtitle = new Label("Your guild has gone bankrupt!");
        subtitle.getStyleClass().add("game-over-subtitle");

        VBox statsBox = new VBox(6);
        statsBox.getStyleClass().add("panel");
        statsBox.setPadding(new Insets(12, 20, 12, 20));
        statsBox.setMaxWidth(320);

        if (game != null) {
            statsBox.getChildren().addAll(
                statRow("Final Balance", String.format("$%.2f", game.getEconomy().getBalance())),
                statRow("Total Income", String.format("$%.2f", game.getEconomy().getTotalIncome())),
                statRow("Total Expenses", String.format("$%.2f", game.getEconomy().getTotalExpenses())),
                statRow("Days Survived", String.format("%.0f", game.getClock().getElapsedTime())),
                statRow("Vehicles Owned", String.format("%d", game.getUnits().size())),
                statRow("Routes Active", String.format("%d", game.getRoutes().size()))
            );
        }

        Button homeBtn = new Button("Return to Main Menu");
        homeBtn.getStyleClass().add("btn-primary");
        homeBtn.setOnAction(e -> controller.showWelcomeView(stage));

        Button loadBtn = new Button("Load Save");
        loadBtn.getStyleClass().add("btn-secondary");
        loadBtn.setOnAction(e -> controller.showLoadSaveView(stage));

        VBox buttonRow = new VBox(8, homeBtn, loadBtn);
        buttonRow.setAlignment(Pos.CENTER);

        VBox box = new VBox(16, title, subtitle, statsBox, buttonRow);
        box.setAlignment(Pos.CENTER);

        StackPane overlay = new StackPane(box);
        overlay.getStyleClass().add("game-over-overlay");
        return overlay;
    }

    private static Label statRow(String key, String value) {
        Label label = new Label(key + ":  " + value);
        label.getStyleClass().add("table-row");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
}
