package com.game.dungeons_breath.services;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

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
        Set<String> imageNames = new LinkedHashSet<>();

        // The resources are placed under package path com/game/dungeons_breath/
        String packagePath = "com/game/dungeons_breath/" + subfolder;

        try {
            // 1) Try classloader resource approach first
            URL dirURL = GameViewController.class.getClassLoader().getResource(packagePath);
            if (dirURL != null) {
                String protocol = dirURL.getProtocol();
                if ("file".equals(protocol)) {
                    System.out
                            .println("[FileIOService] getFilePaths: using classloader 'file' protocol for packagePath='"
                                    + packagePath + "' -> " + dirURL);
                    File folder = new File(dirURL.toURI());
                    File[] listOfFiles = folder.listFiles();
                    if (listOfFiles != null) {
                        for (File f : listOfFiles) {
                            if (f.isFile()) {
                                imageNames.add(subfolder + f.getName());
                                System.out.println("[FileIOService] getFilePaths: found file in resource dir: "
                                        + f.getAbsolutePath());
                            }
                        }
                    }
                } else if ("jar".equals(protocol)) {
                    System.out
                            .println("[FileIOService] getFilePaths: using classloader 'jar' protocol for packagePath='"
                                    + packagePath + "' -> " + dirURL);
                    String jarPath = dirURL.getPath();
                    int excl = jarPath.indexOf("!");
                    if (excl > 0) {
                        String jarFilePath = jarPath.substring(5, excl);
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
                                        System.out.println("[FileIOService] getFilePaths: found entry in jar '"
                                                + jarFilePath + "': " + entryName);
                                    }
                                }
                            }
                        }
                    }
                } else if ("jrt".equals(protocol)) {
                    System.out
                            .println("[FileIOService] getFilePaths: using classloader 'jrt' protocol for packagePath='"
                                    + packagePath + "' -> " + dirURL);
                    try {
                        try {
                            FileSystems.getFileSystem(URI.create("jrt:/"));
                        } catch (FileSystemNotFoundException e) {
                            FileSystems.newFileSystem(URI.create("jrt:/"), Collections.emptyMap());
                        }
                        // First try to interpret the URL path directly within the jrt filesystem
                        Path resourceRoot = FileSystems.getFileSystem(URI.create("jrt:/")).getPath(dirURL.getPath());
                        if (!Files.exists(resourceRoot)) {
                            // Fallback to the canonical /modules/<module>/<path> form
                            String moduleName = GameViewController.class.getModule() != null
                                    ? GameViewController.class.getModule().getName()
                                    : null;
                            if (moduleName != null) {
                                resourceRoot = FileSystems.getFileSystem(URI.create("jrt:/"))
                                        .getPath("/modules", moduleName, packagePath);
                            }
                        }
                        if (Files.exists(resourceRoot)) {
                            if (Files.isDirectory(resourceRoot)) {
                                try (Stream<Path> stream = Files.list(resourceRoot)) {
                                    stream.filter(Files::isRegularFile).forEach(path -> {
                                        imageNames.add(subfolder + path.getFileName().toString());
                                        System.out.println(
                                                "[FileIOService] getFilePaths: found file in jrt module: " + path);
                                    });
                                }
                            } else if (Files.isRegularFile(resourceRoot)) {
                                imageNames.add(subfolder + resourceRoot.getFileName().toString());
                                System.out.println("[FileIOService] getFilePaths: found single file in jrt module: "
                                        + resourceRoot);
                            }
                        } else {
                            System.out.println("[FileIOService] getFilePaths: jrt resource path not found: "
                                    + dirURL);
                        }
                    } catch (Exception err) {
                        System.out.println("[FileIOService] getFilePaths: failed to read jrt resources: " + err);
                    }
                }
            }

            // 2) If nothing found yet (common for jlink/jpackage runtime), scan classpath
            // entries
            if (imageNames.isEmpty()) {
                System.out.println("[FileIOService] getFilePaths: classloader approach found nothing for '"
                        + packagePath + "', scanning classpath entries...");
                String classpath = System.getProperty("java.class.path");
                if (classpath != null) {
                    for (String cpEntry : classpath.split(File.pathSeparator)) {
                        File cpFile = new File(cpEntry);
                        if (cpFile.isDirectory()) {
                            File folder = new File(cpFile, packagePath);
                            if (folder.exists() && folder.isDirectory()) {
                                File[] files = folder.listFiles();
                                if (files != null) {
                                    for (File f : files) {
                                        if (f.isFile()) {
                                            imageNames.add(subfolder + f.getName());
                                            System.out.println(
                                                    "[FileIOService] getFilePaths: found file in classpath dir: "
                                                            + f.getAbsolutePath());
                                        }
                                    }
                                }
                            }
                        } else if (cpFile.isFile() && cpFile.getName().toLowerCase().endsWith(".jar")) {
                            try (JarFile jar = new JarFile(cpFile)) {
                                Enumeration<JarEntry> entries = jar.entries();
                                while (entries.hasMoreElements()) {
                                    JarEntry entry = entries.nextElement();
                                    String name = entry.getName();
                                    if (name.startsWith(packagePath)) {
                                        String entryName = name.substring(packagePath.length());
                                        if (!entryName.isEmpty() && !entryName.endsWith("/")) {
                                            imageNames.add(subfolder + entryName);
                                            System.out.println("[FileIOService] getFilePaths: found entry in jar '"
                                                    + cpFile.getName() + "': " + entryName);
                                        }
                                    }
                                }
                            } catch (IOException ignored) {
                                // skip unreadable jars
                            }
                        }
                    }
                }

                // 3) Try the code source location for this class (jar or folder)
                try {
                    if (GameViewController.class.getProtectionDomain() != null
                            && GameViewController.class.getProtectionDomain().getCodeSource() != null) {
                        URL codeSource = GameViewController.class.getProtectionDomain().getCodeSource().getLocation();
                        System.out
                                .println("[FileIOService] getFilePaths: inspecting codeSource location: " + codeSource);
                        if (codeSource != null) {
                            String csProtocol = codeSource.getProtocol();
                            if ("file".equals(csProtocol)) {
                                File csFile = new File(codeSource.toURI());
                                if (csFile.isDirectory()) {
                                    File folder = new File(csFile, packagePath);
                                    if (folder.exists() && folder.isDirectory()) {
                                        File[] files = folder.listFiles();
                                        if (files != null) {
                                            for (File f : files) {
                                                if (f.isFile()) {
                                                    imageNames.add(subfolder + f.getName());
                                                    System.out.println(
                                                            "[FileIOService] getFilePaths: found file in codeSource dir: "
                                                                    + f.getAbsolutePath());
                                                }
                                            }
                                        }
                                    }
                                } else if (csFile.isFile() && csFile.getName().toLowerCase().endsWith(".jar")) {
                                    try (JarFile jar = new JarFile(csFile)) {
                                        Enumeration<JarEntry> entries = jar.entries();
                                        while (entries.hasMoreElements()) {
                                            JarEntry entry = entries.nextElement();
                                            String name = entry.getName();
                                            if (name.startsWith(packagePath)) {
                                                String entryName = name.substring(packagePath.length());
                                                if (!entryName.isEmpty() && !entryName.endsWith("/")) {
                                                    imageNames.add(subfolder + entryName);
                                                    System.out.println(
                                                            "[FileIOService] getFilePaths: found entry in codeSource jar: "
                                                                    + entryName);
                                                }
                                            }
                                        }
                                    } catch (IOException ignored) {
                                        // skip
                                    }
                                }
                            } else if ("jar".equals(csProtocol)) {
                                try {
                                    // codeSource points to a jar URL; open it
                                    String jarPath = codeSource.getPath();
                                    if (jarPath != null) {
                                        jarPath = URLDecoder.decode(jarPath, "UTF-8");
                                        try (JarFile jar = new JarFile(new File(new URI(jarPath)))) {
                                            Enumeration<JarEntry> entries = jar.entries();
                                            while (entries.hasMoreElements()) {
                                                JarEntry entry = entries.nextElement();
                                                String name = entry.getName();
                                                if (name.startsWith(packagePath)) {
                                                    String entryName = name.substring(packagePath.length());
                                                    if (!entryName.isEmpty() && !entryName.endsWith("/")) {
                                                        imageNames.add(subfolder + entryName);
                                                        System.out.println(
                                                                "[FileIOService] getFilePaths: found entry in codeSource jar URL: "
                                                                        + entryName);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {
                                    // skip
                                }
                            } else if ("jrt".equals(csProtocol)) {
                                try {
                                    try {
                                        FileSystems.getFileSystem(URI.create("jrt:/"));
                                    } catch (FileSystemNotFoundException e) {
                                        FileSystems.newFileSystem(URI.create("jrt:/"), Collections.emptyMap());
                                    }
                                    String moduleName = GameViewController.class.getModule() != null
                                            ? GameViewController.class.getModule().getName()
                                            : null;
                                    if (moduleName != null) {
                                        Path resourceRoot = FileSystems.getFileSystem(URI.create("jrt:/"))
                                                .getPath("/modules", moduleName, packagePath);
                                        if (Files.isDirectory(resourceRoot)) {
                                            try (Stream<Path> stream = Files.list(resourceRoot)) {
                                                stream.filter(Files::isRegularFile).forEach(path -> {
                                                    imageNames.add(subfolder + path.getFileName().toString());
                                                    System.out.println(
                                                            "[FileIOService] getFilePaths: found file in codeSource jrt module: "
                                                                    + path);
                                                });
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {
                                    // skip
                                }
                            } else {
                                System.out.println(
                                        "[FileIOService] getFilePaths: skipping codeSource protocol '" + csProtocol
                                                + "'");
                            }
                        }
                    }
                } catch (URISyntaxException ignored) {
                    // ignore
                }
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        System.out.println(
                "[FileIOService] getFilePaths: total resources found for '" + packagePath + "' = " + imageNames.size());
        return new ArrayList<>(imageNames);
    }
}
