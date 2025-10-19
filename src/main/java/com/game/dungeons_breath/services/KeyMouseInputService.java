package com.game.dungeons_breath.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.game.dungeons_breath.models.Vector2;

public class KeyMouseInputService {

    private List<Vector2> mouseInput;
    private HashSet<String> textInput; // Use HashSet here because it allows a key to only appear once.

    public KeyMouseInputService() {
        mouseInput = new ArrayList<Vector2>();
        textInput = new HashSet<String>();
    }

    public void onMousePressed(Vector2 v) {
        mouseInput.add(v);
    }

    public void onMouseReleased(Vector2 v) {
        mouseInput.clear();
    }

    public void onKeyPressed(javafx.scene.input.KeyEvent keyEvent) {
        textInput.add(keyEvent.getText());
    }

    public void onKeyReleased(javafx.scene.input.KeyEvent keyEvent) {
        textInput.remove(keyEvent.getText()); // removes all instance of the text (in case there are more than one)
    }

    public List<Vector2> getMouseInput() {
        return mouseInput;
    }

    public HashSet<String> getTextInput() {
        return textInput;
    }

}
