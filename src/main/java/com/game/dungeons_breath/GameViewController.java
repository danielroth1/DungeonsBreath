package com.game.dungeons_breath;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.game.dungeons_breath.application.*;
import com.game.dungeons_breath.models.*;
import com.game.dungeons_breath.services.ImageLoader;

public class GameViewController {

    @FXML
    public CanvasPane canvasPane;
    public VBox canvasParent;

    private ImageLoader imageLoader;
    private GameController gameController;

    private double currentScale;
    private List<Button> buttons;

    @FXML
    public void onBackButtonClick(ActionEvent actionEvent) throws IOException {
        gameController.dispose();
        MainApplication.changeScene("main-view.fxml");
    }

    public void onRestartButtonClick(ActionEvent actionEvent) {
        gameController.restartGame();
    }

    @FXML
    public void initialize() {

        buttons = new ArrayList<>();
        gameController = new GameController();
        gameController.addListener(new GameControllerListener() {
            @Override
            public void gameLoopCompleted() {
                paintCanvasLater();
            }
        });

        imageLoader = new ImageLoader();
        canvasPane.widthProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                        paintCanvasLater();
                    }
                });
        canvasPane.heightProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                        paintCanvasLater();
                    }
                });
        currentScale = 1;
        gameController.startGame();
    }

    public void paintCanvasLater() {
        Platform.runLater(() -> {
            paintCanvas();
        });
    }

    public void paintCanvas() {
        Canvas canvas = canvasPane.getCanvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.restore();
        gc.save();
        gc.setFill(Color.BLACK);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        World world = gameController.getWorld();
        double canvasWidth = 0;
        double canvasHeight = 0;
        // scale so that world fits window
        if (canvas.getHeight() != 0) {
            // apply new scale
            currentScale = canvas.getHeight() / world.getHeight();
            gc.scale(currentScale, currentScale);

            // center world
            canvasWidth = canvas.getWidth() / currentScale;
            canvasHeight = canvas.getHeight() / currentScale;
            gc.translate(canvasWidth / 2 - world.getWidth() / 2, 0);
        }

        // world background
        gc.setFill(Color.BLUE);
        gc.fillRect(0, 0, world.getWidth() / 2, world.getHeight());
        gc.setFill(Color.RED);
        gc.fillRect(world.getWidth() / 2, 0, world.getWidth() / 2, world.getHeight());

        // world background images
        Image backgroundImage = imageLoader.getImage("images/environment/undead3.png");
        for (double x = 0; x < world.getWidth(); x += backgroundImage.getWidth()) {
            for (double y = 0; y < world.getHeight(); y += backgroundImage.getHeight()) {
                gc.drawImage(backgroundImage, x, y);
            }
        }

        gc.setFill(Color.GRAY);
        double sideWidth = (canvasWidth - world.getWidth()) / 2;
        gc.fillRect(-sideWidth, 0, sideWidth, canvasHeight);
        gc.fillRect(world.getWidth(), 0, sideWidth, canvasHeight);

        // world border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0, 0, world.getWidth(), world.getHeight());

        // world units
        synchronized (gameController.getWorld().getUnits()) {
            for (Unit unit : gameController.getWorld().getUnits()) {
                gc.drawImage(imageLoader.getImage(unit.getImagePath()), unit.getX(), unit.getY());

                if (unit.getHealth() != unit.getMaxHealth()) {
                    double healthFactor = unit.getHealth() / unit.getMaxHealth();
                    Paint healthColor = healthColor = Color.GREEN;
                    if (healthFactor < 0.3)
                        healthColor = Color.RED;
                    else if (healthFactor < 0.7)
                        healthColor = Color.ORANGE;
                    gc.setFill(healthColor);
                    gc.fillRect(unit.getX(), unit.getY() - 2, healthFactor * unit.getWidth(), 2);
                }
            }
        }

        // score board
        Color whiteTransparent = new Color(1, 1, 1, 0.5);
        gc.setFill(whiteTransparent);
        gc.fillRect(world.getWidth() - 120, 0, 150, 50);
        // score
        gc.setFill(Color.RED);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("Score: " + Integer.toString(gameController.getScore()), world.getWidth() - 100, 15);
        // time end
        gc.fillText("Remaining time: " + Integer.toString(gameController.getTimeLeftMs() / 1000),
                world.getWidth() - 100, 30);
        // magazine
        Unit player = gameController.getPlayer();
        if (player != null) {
            Weapon weapon = player.getWeapon();
            if (weapon != null) {
                gc.fillText(Integer.toString(weapon.getNumBulletsInMagazine()) + " / "
                        + Integer.toString(weapon.getMagazinSize()), world.getWidth() - 100, 45);
            }
        }

        // display banners
        GameState state = gameController.getState();
        double bannerWidth = 100;
        double bannerHeight = 40;
        double bannerX = (world.getWidth() - bannerWidth) / 2;
        double bannerY = (world.getHeight() - bannerHeight) / 2;
        if (state == GameState.WON) { // won banner
            drawBanner(gc, world, "You Win!", Color.GREEN);
        } else if (state == GameState.LOST) { // lost banner
            drawBanner(gc, world, "You Lose!", Color.RED);
        } else if (state == GameState.PAUSED) { // paused banner
            drawBanner(gc, world, "Paused", Color.GRAY);
        }

        // buttons
        for (Button button : buttons) {
            gc.setFill(button.getColor());
            gc.fillRect(button.getX(), button.getY(), button.getWidth(), button.getHeight());
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);

            double baselineOffset = getBaselineOffset(button.getText(), gc.getFont());
            Bounds b = getTextBounds(button.getText(), gc.getFont());
            // + b.getHeight() / 2
            gc.fillText(button.getText(), button.getX() + button.getWidth() / 2,
                    button.getY() + button.getHeight() / 2 + baselineOffset / 2);
        }
    }

    // Helper method to get text bounds
    private Bounds getTextBounds(String text, Font font) {
        Text tempText = new Text(text);
        tempText.setFont(font);
        return tempText.getBoundsInLocal(); // Get the bounding box of the text
    }

    private double getBaselineOffset(String text, Font font) {
        Text tempText = new Text(text);
        tempText.setFont(font);
        return tempText.getBaselineOffset();
    }

    private void drawBanner(GraphicsContext gc, World world, String text, Paint backgroundColor) {
        double bannerWidth = 100;
        double bannerHeight = 40;
        double bannerX = (world.getWidth() - bannerWidth) / 2;
        double bannerY = (world.getHeight() - bannerHeight) / 2;
        gc.setFill(backgroundColor);
        gc.fillRect(bannerX, bannerY, bannerWidth, bannerHeight);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(bannerX, bannerY, bannerWidth, bannerHeight);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, bannerX + bannerWidth / 2, bannerY + bannerHeight / 2 + 3);
    }

    private Vector2 convertToWorldCoordiantes(double x, double y) {
        Canvas canvas = canvasPane.getCanvas();
        World world = gameController.getWorld();
        double vx = x / currentScale - (canvas.getWidth() / 2 / currentScale - world.getWidth() / 2);
        double vy = y / currentScale;
        return new Vector2(vx, vy);
    }

    public void onMousePressed(MouseEvent mouseEvent) {
        gameController.onMousePressed(convertToWorldCoordiantes(mouseEvent.getX(), mouseEvent.getY()));
    }

    public void onMouseReleased(MouseEvent mouseEvent) {
        Vector2 v = convertToWorldCoordiantes(mouseEvent.getX(), mouseEvent.getY());
        for (Button button : buttons) {
            if (button.getX() <= v.x && v.x <= button.getX() + button.getWidth() &&
                    button.getY() <= v.y && v.y <= button.getY() + button.getHeight()) {
                button.onClicked();
            }
        }
        gameController.onMouseReleased(convertToWorldCoordiantes(mouseEvent.getX(), mouseEvent.getY()));
    }

    public void onKeyPressed(KeyEvent keyEvent) {
        gameController.onKeyPressed(keyEvent);
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        gameController.onKeyReleased(keyEvent);
    }
}
