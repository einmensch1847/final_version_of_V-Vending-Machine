package ui;

import View.AdminPanel;
import View.UserPanel;
import api.ApiClient;
import api.Logger;
import com.example.vwm.Main;
import com.example.vwm.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LoginPage {

    private final ApiClient api = new ApiClient();
    private TextField userField;
    private PasswordField passField;
    private Label errorMessage;
    private CheckBox adminCheckBox;
    private VirtualKeyboard usernameKeyboard;
    private VirtualKeyboard passwordKeyboard;

    private static final Color PRIMARY_COLOR = Color.web("#4e9cff");
    private static final Color ACCENT_COLOR = Color.web("#00ffaa");
    private static final Color ERROR_COLOR = Color.web("#ff6b6b");
    private static final Color SUCCESS_COLOR = Color.web("#90ee90");

    public Parent getPage() {
        // Background container
        StackPane backgroundPane = new StackPane();

        // Gradient background
        Rectangle bgRect = new Rectangle();
        bgRect.widthProperty().bind(backgroundPane.widthProperty());
        bgRect.heightProperty().bind(backgroundPane.heightProperty());

        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0f0c29")),
                new Stop(0.5, Color.web("#302b63")),
                new Stop(1, Color.web("#24243e"))
        );
        bgRect.setFill(gradient);

        backgroundPane.getChildren().add(bgRect);

        // Main container - استفاده از BorderPane برای چیدمان بهتر
        BorderPane mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: transparent;");
        mainContainer.setPadding(new Insets(20));

        // Header Section (TOP)
//        HBox headerBox = new HBox(10);
//        headerBox.setAlignment(Pos.CENTER);

        Label icon = new Label("🔐");
        icon.setStyle("-fx-font-size: 50px;");

//        Label title = new Label("ورود به سیستم VWM");
//        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));
//        title.setTextFill(Color.WHITE);
//        title.setEffect(new DropShadow(10, PRIMARY_COLOR));
//
//        headerBox.getChildren().addAll(title);

//        mainContainer.setTop(headerBox);
//        BorderPane.setAlignment(headerBox, Pos.CENTER);
//        BorderPane.setMargin(headerBox, new Insets(20, 0, 40, 0));

        // Login Form Container (CENTER)
        VBox formContainer = new VBox(4);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setStyle("-fx-background-color: rgba(22, 33, 62, 0.8); " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: rgba(78, 156, 255, 0.3); " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 20;");
        formContainer.setEffect(new DropShadow(30, Color.BLACK));
        formContainer.setPadding(new Insets(40, 60, 40, 60));
        formContainer.setMaxWidth(550);

        // Username field
        VBox usernameBox = createLabeledField("👤 نام کاربری", userField = new TextField());

        // Password field
        VBox passwordBox = createLabeledField("🔒 رمز عبور", passField = new PasswordField());

        // Admin checkbox
        HBox checkboxBox = new HBox();
        checkboxBox.setAlignment(Pos.CENTER_RIGHT);

        adminCheckBox = new CheckBox("ورود به عنوان ادمین");
        adminCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 16px; " +
                "-fx-font-weight: bold; -fx-cursor: hand;");
        adminCheckBox.setSelected(false);

        checkboxBox.getChildren().add(adminCheckBox);

        // Login button
        Button loginBtn = new Button("🚪 ورود به سیستم");
        styleLoginButton(loginBtn);
        loginBtn.setOnAction(e -> checkLogin());

        // Error message
        errorMessage = new Label("");
        errorMessage.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        errorMessage.setAlignment(Pos.CENTER);
        errorMessage.setWrapText(true);
        errorMessage.setMaxWidth(420);

        // Quick action buttons
        HBox quickActionBox = createQuickActionButtons();

        // Add all components to form
        formContainer.getChildren().addAll(
                usernameBox, passwordBox, checkboxBox,
                loginBtn, errorMessage, quickActionBox
        );

        mainContainer.setCenter(formContainer);
        BorderPane.setAlignment(formContainer, Pos.CENTER);

        // Virtual Keyboards (BOTTOM)
        HBox keyboardContainer = new HBox(20);
        keyboardContainer.setAlignment(Pos.CENTER);
        keyboardContainer.setPadding(new Insets(20, 0, 0, 0));

        // Create virtual keyboards
        usernameKeyboard = new VirtualKeyboard(userField);
        passwordKeyboard = new VirtualKeyboard(passField);

        // Style keyboards to match theme
        styleVirtualKeyboard(usernameKeyboard);
        styleVirtualKeyboard(passwordKeyboard);

        keyboardContainer.getChildren().addAll(usernameKeyboard, passwordKeyboard);

        mainContainer.setBottom(keyboardContainer);
        BorderPane.setAlignment(keyboardContainer, Pos.CENTER);
        BorderPane.setMargin(keyboardContainer, new Insets(30, 0, 0, 0));

        // Final assembly with background
        StackPane finalContainer = new StackPane();
        finalContainer.getChildren().addAll(backgroundPane, mainContainer);

        return finalContainer;
    }

    private void styleVirtualKeyboard(VirtualKeyboard keyboard) {
        // استایل کلاس VirtualKeyboard با متدهای getChildren قابل تغییر است
        // اما بهتر است در خود کلاس VirtualKeyboard استایل را تغییر دهیم
        // فعلاً یک border اضافه می‌کنیم
        keyboard.setStyle(keyboard.getStyle() +
                " -fx-border-color: rgba(78, 156, 255, 0.3);" +
                " -fx-border-width: 2;" +
                " -fx-border-radius: 15;" +
                " -fx-background-color: rgba(40, 40, 40, 0.9);");
    }

    private VBox createLabeledField(String labelText, TextField field) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        field.setPromptText(labelText.substring(2)); // Remove emoji
        styleLoginField(field);

        container.getChildren().addAll(label, field);
        return container;
    }

    private void styleLoginField(TextField field) {
        String baseStyle = "-fx-font-size: 18px; " +
                "-fx-background-color: rgba(255, 255, 255, 0.08); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: rgba(78, 156, 255, 0.3); " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 15; " +
                "-fx-padding: 15 25; " +
                "-fx-text-fill: white; " +
                "-fx-prompt-text-fill: rgba(255, 255, 255, 0.4); " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0.3, 0, 2);";

        field.setStyle(baseStyle);
        field.setPrefWidth(420);
        field.setPrefHeight(55);

        // Hover effect
        field.setOnMouseEntered(e -> {
            field.setStyle(baseStyle +
                    "-fx-border-color: rgba(78, 156, 255, 0.6); " +
                    "-fx-effect: dropshadow(gaussian, rgba(78,156,255,0.2), 8, 0.4, 0, 3);");
        });

        field.setOnMouseExited(e -> {
            field.setStyle(baseStyle);
        });
    }

    private void styleLoginButton(Button btn) {
        String baseStyle = "-fx-font-size: 20px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 15; " +
                "-fx-background-color: " + toRgbString(PRIMARY_COLOR) + "; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 15 40; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, " + toRgbaString(PRIMARY_COLOR, 0.5) + ", 10, 0.5, 0, 3);";

        btn.setStyle(baseStyle);
        btn.setPrefWidth(420);
        btn.setPrefHeight(60);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(baseStyle +
                    " -fx-scale-x: 1.05; -fx-scale-y: 1.05; " +
                    "-fx-effect: dropshadow(gaussian, " + toRgbaString(PRIMARY_COLOR, 0.7) + ", 15, 0.6, 0, 5);");
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
        });
    }

    private HBox createQuickActionButtons() {
        HBox quickActionBox = new HBox(13);
        quickActionBox.setAlignment(Pos.CENTER);

        // Setup Guide Button
        Button setupBtn = new Button("📖 راهنمای راه‌اندازی");
        styleQuickActionButton(setupBtn, Color.web("#6c5ce7"));
        setupBtn.setOnAction(e -> showSetupGuide());

        // VWM Messenger Button
        Button messengerBtn = new Button("💬 ورود به پیام‌رسان VWM");
        styleQuickActionButton(messengerBtn, Color.web("#00b894"));
        messengerBtn.setOnAction(e -> openVWMMessenger());

        quickActionBox.getChildren().addAll(setupBtn, messengerBtn);
        return quickActionBox;
    }

    private void styleQuickActionButton(Button btn, Color color) {
        String baseStyle = "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 10; " +
                "-fx-background-color: " + toRgbString(color) + "; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10 20; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, " + toRgbaString(color, 0.5) + ", 5, 0.3, 0, 2);";

        btn.setStyle(baseStyle);
        btn.setPrefHeight(45);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(baseStyle +
                    " -fx-scale-x: 1.05; -fx-scale-y: 1.05; " +
                    "-fx-effect: dropshadow(gaussian, " + toRgbaString(color, 0.7) + ", 8, 0.4, 0, 3);");
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
        });
    }

    private void showSetupGuide() {
        // نمایش راهنمای راه‌اندازی
        Stage guideStage = new Stage();
        guideStage.initModality(Modality.APPLICATION_MODAL);
        guideStage.initStyle(StageStyle.UNDECORATED);
        guideStage.setResizable(false);

        VBox guideContent = new VBox(20);
        guideContent.setPadding(new Insets(30));
        guideContent.setAlignment(Pos.CENTER);
        guideContent.setStyle("-fx-background-color: rgba(22, 33, 62, 0.95); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: rgba(108, 92, 231, 0.8); " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 15;");
        guideContent.setEffect(new DropShadow(20, Color.BLACK));

        Label title = new Label("📖 راهنمای راه‌اندازی دستگاه");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        TextArea guideText = new TextArea(
                "مراحل راه‌اندازی دستگاه VWM:\n\n" +
                        "۱. اتصال برق دستگاه\n" +
                        "۲. اتصال به اینترنت\n" +
                        "۳. تنظیمات شبکه\n" +
                        "۴. کالیبراسیون اولیه\n" +
                        "۵. تست دستگاه\n" +
                        "۶. تنظیم محصولات\n\n" +
                        "برای راهنمای کامل با پشتیبانی تماس بگیرید."
        );
        guideText.setEditable(false);
        guideText.setWrapText(true);
        guideText.setPrefHeight(250);
        guideText.setStyle("-fx-font-size: 14px; -fx-text-fill: white; " +
                "-fx-background-color: rgba(255,255,255,0.05); " +
                "-fx-border-color: rgba(255,255,255,0.1); " +
                "-fx-background-radius: 8; -fx-border-radius: 8;");

        Button closeBtn = new Button("بستن");
        closeBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-background-color: " + toRgbString(Color.web("#6c5ce7")) + "; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 8 25; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, " + toRgbaString(Color.web("#6c5ce7"), 0.5) + ", 5, 0.3, 0, 2);");
        closeBtn.setOnAction(e -> guideStage.close());

        guideContent.getChildren().addAll(title, guideText, closeBtn);

        Scene scene = new Scene(guideContent, 500, 400);
        scene.setFill(Color.TRANSPARENT);
        guideStage.setScene(scene);
        guideStage.showAndWait();
    }

    private void openVWMMessenger() {
        Stage messengerStage = new Stage();
        messengerStage.setTitle("VWM Secure Messenger");
        messengerStage.setWidth(1000);
        messengerStage.setHeight(700);
        messengerStage.setResizable(true);

        AutoLoginMessenger messengerLogin = new AutoLoginMessenger(messengerStage);

        Scene scene = new Scene(messengerLogin.getRoot());

        // اضافه کردن استایل‌های CSS (اختیاری)
//        scene.getStylesheets().add(getClass().getResource("/styles/messenger.css").toExternalForm());

        messengerStage.setScene(scene);

        // مرکز‌سازی پنجره
        messengerStage.centerOnScreen();

        messengerStage.show();
    }

    private void checkLogin() {
        String username = userField.getText().trim();
        String password = passField.getText().trim();
        boolean isAdmin = adminCheckBox.isSelected();

        System.out.println("=== Login Attempt ===");
        System.out.println("Username: " + username);
        System.out.println("Password length: " + password.length());
        System.out.println("Is Admin: " + isAdmin);

        if (username.isEmpty() || password.isEmpty()) {
            showErrorMessage("❌ تمام فیلدها باید پر شوند.", ERROR_COLOR);
            return;
        }

        showErrorMessage("⏳ در حال بررسی اطلاعات...", Color.web("#ffcc00"));

        new Thread(() -> {
            try {
                if (isAdmin) {
                    // ✅ لاگین ادمین
                    String response = api.login(username, password, true);
                    System.out.println("Admin login response: " + response);

                    Platform.runLater(() -> {
                        if ("CONNECTION_FAILED".equals(response)) {
                            showErrorMessage("❌ اتصال به سرور برقرار نشد.", ERROR_COLOR);
                        } else if (response != null && response.startsWith("OK")) {
                            try {
                                String[] parts = response.split(";");
                                String fullname = parts[2].split("=")[1];
                                String level = parts[5].split("=")[1];

                                String targetMessage = "ادمین وارد شد: " + fullname + " سطح: " + level;
                                Parent targetPanel = new AdminPanel(parts[1].split("=")[1], fullname,
                                        parts[3].split("=")[1], parts[4].split("=")[1],
                                        level).getRoot();

                                // لاگ گرفتن ورود ادمین
                                String logDescription = "ورود ادمین: " + username + " (" + fullname + ") سطح: " + level;
                                Logger.log(username, "ADMIN_LOGIN", logDescription, username, "ADMIN", "INFO");

                                showErrorMessage("✅ " + targetMessage + " (۲ ثانیه تا ورود...)", SUCCESS_COLOR);

                                Timeline countdown = new Timeline(
                                        new KeyFrame(Duration.seconds(2), e -> {
                                            SceneManager.switchSceneWithFadeTransition(
                                                    Main.getInstance().getPrimaryStage(), targetPanel);
                                        })
                                );
                                countdown.setCycleCount(1);
                                countdown.play();

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showErrorMessage("❌ خطا در پردازش اطلاعات", ERROR_COLOR);
                            }
                        } else {
                            showErrorMessage("❌ نام کاربری یا رمز عبور اشتباه است.", ERROR_COLOR);
                        }
                    });

                } // در LoginPage.java، بخش مربوط به لاگین کاربر عادی را اصلاح کنید:

                else {
                    // ✅ لاگین کاربر عادی
                    System.out.println("Attempting user login with api.loginUser()...");
                    UserPanel.User user = api.loginUser(username, password);
                    System.out.println("User login result: " + (user != null ? "SUCCESS" : "FAILED"));

                    Platform.runLater(() -> {
                        if (user != null) {
                            String targetMessage = "کاربر وارد شد: " + user.getFullname();
                            UserPanel userPanel = new UserPanel(user);
                            Parent targetPanel = userPanel.getRoot();

                            // لاگ گرفتن ورود کاربر
                            String logDescription = "ورود کاربر: " + username + " (" + user.getFullname() + ")";
                            Logger.log("SYSTEM", "USER_LOGIN", logDescription, username, "USER", "INFO");

                            showErrorMessage("✅ " + targetMessage + " (۲ ثانیه تا ورود...)", SUCCESS_COLOR);

                            Timeline countdown = new Timeline(
                                    new KeyFrame(Duration.seconds(2), e -> {
                                        SceneManager.switchSceneWithFadeTransition(
                                                Main.getInstance().getPrimaryStage(), targetPanel);

                                        // ✅ بعد از انتقال به صفحه کاربر، مانیتورینگ را فعال کن
                                        Scene scene = Main.getInstance().getPrimaryStage().getScene();
                                        userPanel.startIdleMonitoring(scene);
                                    })
                            );
                            countdown.setCycleCount(1);
                            countdown.play();

                        } else {
                            showErrorMessage("❌ نام کاربری یا رمز عبور اشتباه است.", ERROR_COLOR);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showErrorMessage("❌ خطا در ارتباط با سرور", ERROR_COLOR);
                });
            }
        }).start();
    }
    private void showErrorMessage(String message, Color color) {
        errorMessage.setText(message);
        errorMessage.setTextFill(color);

        // Add styling based on message type
        if (color.equals(SUCCESS_COLOR)) {
            errorMessage.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-background-color: rgba(144, 238, 144, 0.1); " +
                    "-fx-background-radius: 8; -fx-padding: 10; " +
                    "-fx-border-color: rgba(144, 238, 144, 0.3); " +
                    "-fx-border-width: 1; -fx-border-radius: 8;");
        } else if (color.equals(ERROR_COLOR)) {
            errorMessage.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-background-color: rgba(255, 107, 107, 0.1); " +
                    "-fx-background-radius: 8; -fx-padding: 10; " +
                    "-fx-border-color: rgba(255, 107, 107, 0.3); " +
                    "-fx-border-width: 1; -fx-border-radius: 8;");
        } else {
            errorMessage.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-background-color: rgba(255, 204, 0, 0.1); " +
                    "-fx-background-radius: 8; -fx-padding: 10; " +
                    "-fx-border-color: rgba(255, 204, 0, 0.3); " +
                    "-fx-border-width: 1; -fx-border-radius: 8;");
        }
    }

    private String toRgbString(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    private String toRgbaString(Color color, double alpha) {
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255),
                alpha);
    }
}