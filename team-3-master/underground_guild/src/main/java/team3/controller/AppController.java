package team3.controller;

import java.util.Objects;
import team3.service.GameService;
import team3.view.WelcomeView;
import team3.view.LoadSaveView;
import team3.view.MainGameView;
import team3.view.SettingsView;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
/**
 * Coordinates scene navigation and delegates game actions to the service layer.
 */
public class AppController {
    private static final String APP_STYLESHEET =
        Objects.requireNonNull(AppController.class.getResource("/styles.css"))
            .toExternalForm();

    private final GameService gameService;

    public AppController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Called when the "New Game" button is pressed.
     */
    public void handleStartNewGame(Stage stage) {
        gameService.startNewGame();
        showMainGame(stage);
    }

    public String loadSave(int slot, Stage stage) {
        String result = gameService.loadGameFromSlot(slot);
        if (result.startsWith("Loaded")) {
            showMainGame(stage);
        }
        return result;
    }    

    public void showMainGame(Stage stage) {
        MainGameView view = new MainGameView(this);
        
        gameService.setOnGameOver(() -> 
        Platform.runLater(() -> view.showGameOver(stage))
    );
        
        Scene scene = new Scene(view.build(), 1280, 720);
        scene.getStylesheets().add(APP_STYLESHEET);
        stage.setScene(scene);
        stage.show();
    }

    public void showWelcomeView(Stage stage) {
        WelcomeView view = new WelcomeView(this);
        stage.getScene().setRoot(view.build());
    }

    public void showSettingsView(Stage stage) {
        SettingsView view = new SettingsView(this);
        stage.getScene().setRoot(view.build());
    }

    public void showLoadSaveView(Stage stage) {
        LoadSaveView view = new LoadSaveView(this);
        stage.getScene().setRoot(view.build());
    }

    public String handleBuildTunnel(int x, int y) {
        return gameService.buildTunnelAt(x, y);
    }
    
    // Getter for the view to access game data (money, clock, etc.)
    public GameService getGameService() {
        return gameService;
    }
}
