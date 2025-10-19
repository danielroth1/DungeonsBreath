package com.game.dungeons_breath.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.game.dungeons_breath.GameViewController;

public class FileIOService {

    /**
     * Return the path of a resource if available (may return null when resource not
     * found).
     */
    public static String getFilePath(String filePath) {
        URL res = GameViewController.class.getResource(filePath);
        if (res == null) {
            return null;
        }
        return res.getPath();
    }

    /**
     * @param subfolder - subfolder in resources folder, e.g. "images/units/"
     * @return list of file names in subfolder (paths relative to resources under
     *         com/game/dungeons_breath/)
     */
    public static List<String> getFilePaths(String subfolder) {
        List<String> imageNames = new ArrayList<>();

        // The resources are placed under package path com/game/dungeons_breath/
        String packagePath = "com/game/dungeons_breath/" + subfolder;

        // Try to get the URL for the folder
        URL dirURL = GameViewController.class.getClassLoader().getResource(packagePath);

        try {
            if (dirURL != null && dirURL.getProtocol().equals("file")) {
                // Running from IDE / exploded classes: resources are files on disk
                File folder = new File(dirURL.toURI());
                File[] listOfFiles = folder.listFiles();
                if (listOfFiles != null) {
                    for (File f : listOfFiles) {
                        if (f.isFile()) {
                            imageNames.add(subfolder + f.getName());
                        }
                    }
                }
            } else if (dirURL != null && dirURL.getProtocol().equals("jar")) {
                // Running from JAR: need to read entries in the jar file
                String jarPath = dirURL.getPath();
                // jarPath example:
                // file:/path/to/jar.jar!/com/game/dungeons_breath/images/units/
                int excl = jarPath.indexOf("!");
                String jarFilePath = jarPath.substring(5, excl); // strip "file:"
                jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
                try (JarFile jar = new JarFile(jarFilePath)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(packagePath)) {
                            String entryName = name.substring(packagePath.length());
                            if (!entryName.isEmpty() && !entryName.endsWith("/")) {
                                imageNames.add(subfolder + entryName);
                            }
                        }
                    }
                }
            } else if (dirURL == null) {
                // Resource not found via ClassLoader; attempt with leading slash via
                // class.getResource
                URL alt = GameViewController.class.getResource("/" + packagePath);
                if (alt != null) {
                    // recursive call: convert URL to file or jar handling
                    return getFilePaths(subfolder); // alt should normally resolve the same; fall back to empty
                }
            }
        } catch (URISyntaxException | IOException e) {
            // If anything goes wrong, return the (possibly partial) list
            e.printStackTrace();
        }

        return imageNames;
    }
}
