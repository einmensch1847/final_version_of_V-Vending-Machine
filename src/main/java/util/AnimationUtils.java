package util;

import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AnimationUtils {

    public static void switchSceneWithFadeTransition(Stage stage, Parent newPage) {
        Scene scene = stage.getScene();

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), scene.getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            scene.setRoot(newPage);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), newPage);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    public static void fadeIn(Scene scene) {
        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), scene.getRoot());
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
}
