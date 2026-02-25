package com.example.vwm;

import javafx.animation.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SceneManager {

    public static void switchSceneWithFadeTransition(Stage primaryStage, Parent newPage) {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), primaryStage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            primaryStage.getScene().setRoot(newPage);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), primaryStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    // در کلاس SceneManager اضافه کنید:
    public static void switchSceneWithSlideTransition(Stage stage, Parent newRoot) {
        Scene currentScene = stage.getScene();
        Parent oldRoot = currentScene.getRoot();

        StackPane container = new StackPane();
        container.getChildren().addAll(oldRoot, newRoot);

        newRoot.setTranslateX(stage.getWidth());

        currentScene.setRoot(container);

        Timeline slideOut = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(oldRoot.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(oldRoot.translateXProperty(), -stage.getWidth() / 3, Interpolator.EASE_BOTH))
        );

        Timeline slideIn = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(newRoot.translateXProperty(), stage.getWidth())),
                new KeyFrame(Duration.millis(300), new KeyValue(newRoot.translateXProperty(), 0, Interpolator.EASE_BOTH))
        );

        slideOut.play();
        slideIn.play();

        slideIn.setOnFinished(e -> currentScene.setRoot(newRoot));
    }

    public static void switchToPanel(Stage stage, Parent newPanel) {
        if (stage.getScene() != null) {
            Parent oldRoot = stage.getScene().getRoot();

            // انیمیشن محو شدن
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldRoot);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            fadeOut.setOnFinished(e -> {
                // تنظیم ریشه جدید
                stage.getScene().setRoot(newPanel);

                // انیمیشن ظاهر شدن
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newPanel);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });

            fadeOut.play();
        } else {
            Scene scene = new Scene(newPanel);
            stage.setScene(scene);
            stage.show();
        }
    }
}
