package com.game.dungeons_breath.models;

public class Vector2 {

    public double x, y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 add(Vector2 vector) {
        x += vector.x;
        y += vector.y;
        return this;
    }

}
