package team3;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import team3.controller.AppController;
import team3.service.GameService;
import team3.util.AppMetadata;
import team3.view.WelcomeView;

/**
 * Application entrypoint for the Maven JavaFX setup.
 */
public class Main extends Application {

    private static final double WINDOW_WIDTH = 1000;
    private static final double WINDOW_HEIGHT = 700;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        GameService gameService = new GameService();
        AppController controller = new AppController(gameService);
        WelcomeView welcomeView = new WelcomeView(controller);

        Scene scene = new Scene(welcomeView.build(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setTitle(AppMetadata.APPLICATION_TITLE);
        stage.setScene(scene);
        stage.show();
    }
}
