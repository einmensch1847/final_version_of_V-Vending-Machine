package View;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class IdleMonitor {

    private Stage stage;
    private Scene scene;
    private Runnable onIdleCallback;
    private Timeline idleTimeline;
    private static final int IDLE_TIME_SECONDS = 60; // 1 دقیقه
    private boolean isScreenSaverMode = false;

    public IdleMonitor(Stage stage, Scene scene, Runnable onIdleCallback) {
        this.stage = stage;
        this.scene = scene;
        this.onIdleCallback = onIdleCallback;
        setupIdleMonitoring();
    }

    private void setupIdleMonitoring() {
        // تایمر برای تشخیص inactivity
        idleTimeline = new Timeline(new KeyFrame(Duration.seconds(IDLE_TIME_SECONDS), e -> {
            if (onIdleCallback != null && !isScreenSaverMode) {
                isScreenSaverMode = true;
                onIdleCallback.run();
            }
        }));
        idleTimeline.setCycleCount(Animation.INDEFINITE);

        // ریست تایمر با هر تعامل کاربر
        scene.addEventFilter(javafx.scene.input.MouseEvent.ANY, e -> resetTimer());
        scene.addEventFilter(javafx.scene.input.KeyEvent.ANY, e -> resetTimer());
        scene.addEventFilter(javafx.scene.input.TouchEvent.ANY, e -> resetTimer());

        resetTimer();
    }

    private void resetTimer() {
        idleTimeline.stop();
        idleTimeline.playFromStart();
    }

    public void setScreenSaverMode(boolean active) {
        this.isScreenSaverMode = active;
    }

    public void stop() {
        if (idleTimeline != null) {
            idleTimeline.stop();
        }
    }

    public void start() {
        isScreenSaverMode = false;
        resetTimer();
    }
}