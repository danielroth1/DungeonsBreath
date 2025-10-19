module com.game.dungeons_breath {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires java.desktop;
    requires transitive javafx.media;

    exports com.game.dungeons_breath.application;

    opens com.game.dungeons_breath.application to javafx.fxml;

    exports com.game.dungeons_breath.services;

    opens com.game.dungeons_breath.services;

    // IBehavior is used in public APIs (Unit etc.) - ensure implementations are
    // visible
    exports com.game.dungeons_breath.behaviors;

    opens com.game.dungeons_breath.behaviors to javafx.fxml;

    exports com.game.dungeons_breath;

    opens com.game.dungeons_breath;

    exports com.game.dungeons_breath.models;

    opens com.game.dungeons_breath.models to javafx.fxml;
}