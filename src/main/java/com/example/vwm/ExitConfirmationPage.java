package com.example.vwm;



import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.AnimationUtils;
import ui.LoginPage;

public class ExitConfirmationPage {
    private VBox root;
    private Label countdownLabel;
    private Timeline timeline;

    public ExitConfirmationPage(Stage primaryStage) {
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: rgba(0,0,0,0.7);"); // شیشه‌ای تاریک

        Label message = new Label("آیا مطمئن هستید که می‌خواهید خارج شوید؟");
        message.setStyle("-fx-text-fill: white; -fx-font-size: 28px;");

        countdownLabel = new Label();
        countdownLabel.setStyle("-fx-text-fill: white; -fx-font-size: 48px;");

        Button confirmButton = new Button("بله");
        confirmButton.setStyle("-fx-font-size: 26px;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: rgba(255,255,255,0.10);" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;" +
                "-fx-border-color: rgba(255,255,255,0.25);" +
                "-fx-border-width: 1.5;" +
                "-fx-padding: 15 20;" +
                "-fx-prompt-text-fill: rgba(255,255,255,0.45);");
        Button cancelButton = new Button("خیر");
        cancelButton.setStyle("-fx-font-size: 26px;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: rgba(255,255,255,0.10);" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;" +
                "-fx-border-color: rgba(255,255,255,0.25);" +
                "-fx-border-width: 1.5;" +
                "-fx-padding: 15 20;" +
                "-fx-prompt-text-fill: rgba(255,255,255,0.45);");

        confirmButton.setOnAction(e -> startCountdown(primaryStage));
        cancelButton.setOnAction(e -> {
            if (timeline != null) timeline.stop();
            SceneManager.switchSceneWithFadeTransition(primaryStage, Main.getInstance().getCurrentPage());
        });

        HBox buttons = new HBox(20, confirmButton, cancelButton);
        buttons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(message, countdownLabel, buttons);
    }

    public VBox getRoot() {
        return root;
    }

    private void startCountdown(Stage primaryStage) {
        timeline = new Timeline();
        for (int i = 10; i >= 0; i--) {
            int second = i;
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(10 - i), e -> {
                countdownLabel.setText("خروج در " + second + " ثانیه");
                countdownLabel.setStyle("-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #ff5555;");
                if (second == 0) {
                    Platform.exit();
                }
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.play();
    }
}

