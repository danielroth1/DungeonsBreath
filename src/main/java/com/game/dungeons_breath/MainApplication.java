package com.game.dungeons_breath;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {

    private static Stage stage;

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlUrl = getClass().getResource("/com/game/dungeons_breath/main-view.fxml");
        if (fxmlUrl == null) {
            System.err.println("ERROR: main-view.fxml not found on classpath at /com/game/dungeons_breath/main-view.fxml");
            return;
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        MainApplication.stage = stage;
        Scene scene = new Scene(root, 1080, 720);
        stage.setTitle("Game");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Easy way of changing scene from everywhere in the software.
     * 
     * @param fxml address to the scene file (e.g. "main-view.fxml")
     * @throws IOException
     */
    public static void changeScene(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(MainApplication.class.getResource(fxml));
        stage.getScene().setRoot(pane);
    }

    public static void main(String[] args) {
        launch(MainApplication.class, args);
    }
}