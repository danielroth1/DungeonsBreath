package com.game.dungeons_breath;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

public class CanvasPane extends Pane {

    public final Canvas canvas;

    public CanvasPane() {
        canvas = new Canvas(0, 0);
        getChildren().add(canvas);

        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());
    }

    public Canvas getCanvas() {
        return canvas;
    }
}