package com.game.dungeons_breath.services;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.List;

import com.game.dungeons_breath.GameViewController;

public class ImageLoader {

    private HashMap<String, Image> imageMap;

    public ImageLoader() {
        imageMap = new HashMap<String, Image>();
    }

    /**
     * Gets image from images folder. Loads images that are not loaded yet (only
     * once).
     * 
     * @param path - path of the image file in images folder with file ending, e.g.
     *             hill_giant.png -> images/{path}
     * @return loaded image
     */
    public Image getImage(String path) {
        Image image = imageMap.getOrDefault(path, null);
        if (image == null) {
            image = new Image(GameViewController.class.getResourceAsStream(path));
            imageMap.put(path, image);
        }
        return image;
    }

    public static List<String> getUnitImagePaths() {
        return FileIOService.getFilePaths("images/units/");
    }
}
