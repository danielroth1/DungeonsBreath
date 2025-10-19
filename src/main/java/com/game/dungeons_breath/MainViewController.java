package com.game.dungeons_breath;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainViewController {

    @FXML
    private StackPane root;

    @FXML
    private ImageView bgImageView;

    @FXML
    protected void onStartButtonClick() throws IOException {
        MainApplication.changeScene("game-view.fxml");
    }

    @FXML
    protected void onExitButtonClick() {
        Platform.exit();
    }

    @FXML
    public void initialize() {
        // bind the imageview to fill the root pane
        bgImageView.fitWidthProperty().bind(root.widthProperty());
        bgImageView.fitHeightProperty().bind(root.heightProperty());
    }

    public MainViewController() {

    }
}