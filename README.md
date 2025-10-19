# Game (JavaFX) - Gradle

This project has been migrated to use Gradle. Maven files and outputs were removed.

## Build and run (Windows cmd)

```
gradle clean build
gradle run
```

- Requires JDK 23 (Gradle toolchain will download if configured).
- JavaFX modules used: javafx-controls, javafx-fxml, javafx-media (version 17.0.12).
- Main class: `com.game.dungeons_breath.MainApplication`
- Module name: `com.game.dungeons_breath`

## Notes

- Resources are under `src/main/resources` and packaged by Gradle.
- If you encounter JavaFX runtime errors, ensure the installed JDK matches the toolchain (23) and that graphics drivers are up to date.
