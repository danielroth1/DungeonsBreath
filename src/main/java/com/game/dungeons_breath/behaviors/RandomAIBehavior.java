package com.game.dungeons_breath.behaviors;

import java.util.Arrays;
import java.util.List;

import com.game.dungeons_breath.models.Unit;
import com.game.dungeons_breath.models.Vector2;
import com.game.dungeons_breath.models.World;

public class RandomAIBehavior implements IBehavior {

    final List<Vector2> movements2d = Arrays.asList(
            new Vector2(1.0, 0.0), // right
            new Vector2(-1.0, 0.0), // left
            new Vector2(0.0, -1.0), // up
            new Vector2(0.0, 1.0), // down
            new Vector2(1.0, 1.0), // right-down
            new Vector2(1.0, -1.0), // right-up
            new Vector2(-1.0, 1.0), // left-down
            new Vector2(-1.0, -1.0) // left-up
    );

    private Unit unit;
    private World world;
    private int walkDirectionIndex = 0;

    public RandomAIBehavior(Unit unit, World world) {
        this.unit = unit;
        this.world = world;
    }

    @Override
    public void update(int timePassedMs) {
        if (timePassedMs % 500 == 0) { // change walk direction every 0.5 seconds
            walkDirectionIndex = (int) Math.floor(Math.random() * movements2d.size());
        }
        Vector2 movement = movements2d.get(walkDirectionIndex);
        world.moveUnit(unit, movement.x, movement.y);
    }
}
