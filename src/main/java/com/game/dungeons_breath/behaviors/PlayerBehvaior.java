package com.game.dungeons_breath.behaviors;

import java.util.Objects;

import com.game.dungeons_breath.models.Unit;
import com.game.dungeons_breath.models.Vector2;
import com.game.dungeons_breath.models.World;
import com.game.dungeons_breath.services.KeyMouseInputService;

public class PlayerBehvaior implements IBehavior {

    private Unit unit;
    private World world;
    KeyMouseInputService keyMouseInputService;

    public PlayerBehvaior(Unit unit, World world, KeyMouseInputService keyMouseInputService) {
        this.unit = unit;
        this.world = world;
        this.keyMouseInputService = keyMouseInputService;
    }

    @Override
    public void update(int timePassedMs) {
        // player control with w, a, s, d
        Vector2 dv = new Vector2(0, 0);
        for (String charPressed : keyMouseInputService.getTextInput()) {
            if (Objects.equals(charPressed, "w") || Objects.equals(charPressed, "a") || Objects.equals(charPressed, "s")
                    || Objects.equals(charPressed, "d")) {
                switch (charPressed.toLowerCase()) {
                    case "w":
                        dv.y += -1;
                        break;
                    case "a":
                        dv.x += -1;
                        break;
                    case "s":
                        dv.y += 1;
                        break;
                    case "d":
                        dv.x += 1;
                        break;
                }
            }
        }
        world.moveUnit(unit, dv.x, dv.y);
    }
}
