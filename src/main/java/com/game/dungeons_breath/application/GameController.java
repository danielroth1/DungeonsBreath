package com.game.dungeons_breath.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.game.dungeons_breath.behaviors.IBehavior;
import com.game.dungeons_breath.behaviors.PlayerBehvaior;
import com.game.dungeons_breath.behaviors.RandomAIBehavior;
import com.game.dungeons_breath.models.*;
import com.game.dungeons_breath.services.ImageLoader;
import com.game.dungeons_breath.services.KeyMouseInputService;
import com.game.dungeons_breath.services.SoundService;

public class GameController {

    private Thread gameThread;
    private boolean running;

    private World world;
    private int timePassedMs;
    private List<GameControllerListener> listeners;
    private KeyMouseInputService keyMouseInputService;
    private SoundService soundService;

    private List<String> unitImagePaths;
    private int score;
    private int timeEndMs; // end time in ms
    private GameState state;

    private Unit player;

    // game parameters
    private int stepSize = 10;
    private String backgroundSound = "sounds/sunsai__menu-background-music.mp3";
    private String painSound = "sounds/unfa__medium-male-pain-grunts.mp3";

    public GameController() {
        unitImagePaths = ImageLoader.getUnitImagePaths();
        listeners = new ArrayList<>();
        keyMouseInputService = new KeyMouseInputService();
        soundService = new SoundService();
    }

    public void dispose() {
        running = false;
        gameThread = null;
        if (soundService != null)
            soundService.dispose();
        soundService = null;
    }

    public void startGame() {

        if (gameThread == null) {
            gameThread = new Thread() {
                @Override
                public void run() {
                    while (running) {
                        try {

                            updateGame();

                            for (GameControllerListener listener : listeners) {
                                listener.gameLoopCompleted();
                            }
                            Thread.sleep(stepSize);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    super.run();
                }
            };
            running = true;
            gameThread.start();
        }
        state = GameState.STARTING;
        soundService.playContinousSound(backgroundSound, true, true);
    }

    public void restartGame() {
        startGame();
    }

    /**
     * Update the game state. Is called in every game loop iteration.
     */
    private void updateGame() {
        // Game state machine. This is the heart of the game loop.
        handleInput();

        switch (state) {

            case STARTING:
                handleStartGame();
                break;
            case WON:
                break;
            case LOST:
                break;
            case PAUSED:
                break;
            case RUNNING:
                synchronized (world.getUnits()) {
                    handleUnitCollision();
                    handleWeaponUpdate(stepSize);
                    handleUnitBehaviors();
                    spawnUnitsRandomly();
                    if (world.getUnits().size() == 1 && world.getUnits().contains(player)) { // player is the only unit
                                                                                             // left
                        state = GameState.WON;
                        soundService.pauseContinousSound(painSound, true);
                        soundService.playSound("sounds/you_win.mp3");
                    } else if (timePassedMs >= timeEndMs || player.getHealth() <= 0.0) {
                        state = GameState.LOST;
                        soundService.pauseContinousSound(painSound, true);
                        soundService.playSound("sounds/you_lose.mp3");
                    }
                    timePassedMs += stepSize;
                }
                break;
        }
    }

    private void handleStartGame() {
        score = 0;
        timeEndMs = 60 * 1000; // 60 seconds
        state = GameState.RUNNING;
        timePassedMs = 0;

        world = new World(300, 300);
        synchronized (world.getUnits()) {
            // Add initially one unit of each type
            for (String imagePath : unitImagePaths) {
                Unit unit = new Unit(imagePath, Math.random() * world.getWidth(), Math.random() * world.getHeight());
                unit.setMaxHealth(100);
                unit.setHealth(100);
                unit.setBehavior(new RandomAIBehavior(unit, world));
                world.getUnits().add(unit);
            }

            // Add player
            player = new Unit("images/units/human.png", world.getWidth() / 2, world.getHeight() / 2);
            player.setBehavior(new PlayerBehvaior(player, world, keyMouseInputService));
            player.setMaxHealth(100);
            player.setHealth(100);
            Weapon weapon = new Weapon(6, 6, 1000);
            weapon.addListener(new IWeaponListener() {
                @Override
                public void OnWeaponStartReloading() {
                    soundService.playSound("sounds/pcmac__gun_reload.wav", 0.6);
                }
            });
            player.setWeapon(weapon);
            world.getUnits().add(player);

            state = GameState.RUNNING;
        }
    }

    private void handleInput() {
        handleMouseInput();
        handleKeyInput();
    }

    private void handleMouseInput() {
        if (state != GameState.RUNNING)
            return;
        boolean weaponShot = false;
        for (Vector2 v : keyMouseInputService.getMouseInput()) {
            weaponShot = shootWeapon();
            if (!weaponShot)
                continue;
            List<Unit> unitsHit = world.getUnitsInside(v.x, v.y);
            for (Unit unit : unitsHit) {
                // Don't kill the player (this is not the best way of implementing this)
                // Maybe it would be better to give Unit an attribute "canBeKilledByPlayer"
                if (unit.getBehavior() != null && unit.getBehavior() instanceof PlayerBehvaior)
                    continue;
                shootUnit(unit);
            }
            score += unitsHit.size();
        }
        if (weaponShot)
            soundService.playSound("sounds/d4xx__shoot.mp3");
        keyMouseInputService.getMouseInput().clear();
    }

    private boolean shootWeapon() {
        Weapon weapon = player.getWeapon();
        if (weapon == null)
            return false;
        if (weapon.getNumBulletsInMagazine() <= 0) {
            // sounds bad
            // soundService.playSound("sounds/sertonin__9mm-handgun-being-dry-fired.wav");
            return false;
        }
        weapon.setNumBulletsInMagazine(weapon.getNumBulletsInMagazine() - 1);
        return true;
    }

    private void shootUnit(Unit unit) {
        unit.setHealth(unit.getHealth() - 40);
        if (unit.getHealth() <= 0) {
            world.getUnits().remove(unit);
            soundService.playSound("sounds/demon-death-03.wav");
        } else {
            soundService.playSound("sounds/male_death.wav");
        }
    }

    private void handleKeyInput() {
        for (String charPressed : keyMouseInputService.getTextInput()) {
            if (Objects.equals(charPressed, "p")) {
                // pause / unpause by pressing "p"
                togglePause();
            }
        }
        keyMouseInputService.getTextInput().remove("p"); // only interested in click and not holding down.
    }

    private void togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
            soundService.pauseContinousSound(backgroundSound, true);
            soundService.playSound("sounds/timkahn__paused.wav");
        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
            soundService.playContinousSound(backgroundSound, false, true);
        }
    }

    private void spawnUnitsRandomly() {
        if (timePassedMs % 4000 == 0) { // every 4 seconds
            String randomUnitImageName = unitImagePaths.get((int) Math.floor(Math.random() * unitImagePaths.size()));
            Unit unit = new Unit(randomUnitImageName, Math.random() * world.getWidth(),
                    Math.random() * world.getHeight());
            unit.setMaxHealth(100);
            unit.setHealth(100);
            unit.setBehavior(new RandomAIBehavior(unit, world));
            world.getUnits().add(unit);
        }
    }

    private void handleWeaponUpdate(int timePassedDeltaMs) {
        for (Unit unit : world.getUnits()) {
            Weapon weapon = unit.getWeapon();
            if (weapon != null) {
                weapon.update(timePassedDeltaMs);
            }
        }
    }

    private void handleUnitBehaviors() {
        for (Unit unit : world.getUnits()) {
            IBehavior behavior = unit.getBehavior();
            if (behavior != null) {
                behavior.update(timePassedMs);
            }
        }
    }

    private void handleUnitCollision() {
        if (!world.getUnitsColliding(player).isEmpty()) {
            player.setHealth(player.getHealth() - 1.0);
            soundService.playContinousSound(painSound, false, true, 2.0);
        } else {
            soundService.pauseContinousSound(painSound, true);
        }
    }

    public World getWorld() {
        return world;
    }

    public GameState getState() {
        return state;
    }

    public int getScore() {
        return score;
    }

    public int getTimeEndMs() {
        return timeEndMs;
    }

    public int getTimeLeftMs() {
        return timeEndMs - timePassedMs;
    }

    public Unit getPlayer() {
        return player;
    }

    public void addListener(GameControllerListener listener) {
        listeners.add(listener);
    }

    public void onMousePressed(Vector2 v) {
        keyMouseInputService.onMousePressed(v);
    }

    public void onMouseReleased(Vector2 v) {
        keyMouseInputService.onMouseReleased(v);
    }

    public void onKeyPressed(javafx.scene.input.KeyEvent keyEvent) {
        keyMouseInputService.onKeyPressed(keyEvent);
    }

    public void onKeyReleased(javafx.scene.input.KeyEvent keyEvent) {
        keyMouseInputService.onKeyReleased(keyEvent);
    }
}
