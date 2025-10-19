package com.game.dungeons_breath.models;

import com.game.dungeons_breath.behaviors.IBehavior;
import com.game.dungeons_breath.services.ImageLoader;

public class Unit {

    private String imagePath;
    private double x;
    private double y;
    private double width;
    private double height;
    private double health;
    private double maxHealth;
    private Weapon weapon;

    private IBehavior behavior;

    public Unit(String imagePath, double x, double y) {
        this(imagePath, x, y, null);
    }

    public Unit(String imagePath, double x, double y, IBehavior behavior) {
        this(imagePath, x, y, 0, 0, behavior);
        ImageLoader imageLoader = new ImageLoader();
        javafx.scene.image.Image image = imageLoader.getImage(imagePath);
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public Unit(String imagePath, double x, double y, double width, double height, IBehavior behavior) {
        this.imagePath = imagePath;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        health = 0;
        maxHealth = 0;
        this.behavior = behavior;
    }

    public String getImagePath() {
        return imagePath;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getHealth() {
        return health;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public IBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(IBehavior behavior) {
        this.behavior = behavior;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }
}
