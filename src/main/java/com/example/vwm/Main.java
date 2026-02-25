package com.example.vwm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ui.LoginPage;
import com.example.vwm.SceneManager;

public class Main extends Application {

    private static Main instance;
    private Stage primaryStage;
    private Pane currentPage;

    public Main() {
        instance = this;
    }

    public static Main getInstance() {
        return instance;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Pane getCurrentPage() {
        return currentPage;
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        LoginPage loginPage = new LoginPage();
        currentPage = (Pane) loginPage.getPage();
        Scene scene = new Scene(currentPage);

        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(null);

        stage.setOnCloseRequest(event -> {
            event.consume();
            ExitConfirmationPage exitPage = new ExitConfirmationPage(stage);
            SceneManager.switchSceneWithFadeTransition(stage, exitPage.getRoot());
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
