package View;

import api.ApiClient;
import com.example.vwm.Main;
import com.example.vwm.SceneManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ProductActionPanel {

    private StackPane root;
    private UserPanel.User currentUser;
    private UserPanel.ButtonModel product;
    private ApiClient api = new ApiClient();
    private Runnable onBackCallback;
    private Stage primaryStage;

    // UI Components
    private BorderPane mainContainer;
    private Label timerLabel;
    private Timeline autoReturnTimeline;
    private int selectedSweetness;
    private int selectedCaffeine;
    private int selectedTemperature;
    private ImageView backgroundImageView;

    // iOS 16 Colors
    private boolean isDarkMode;
    private Color currentBackgroundColor;
    private Color currentLabelColor;
    private Color currentSecondaryLabelColor;
    private static final Color IOS_SYSTEM_BLUE = Color.web("#0A84FF");
    private static final Color IOS_SYSTEM_GREEN = Color.web("#30D158");
    private static final Color IOS_SYSTEM_ORANGE = Color.web("#FF9F0A");
    private static final Color IOS_SYSTEM_RED = Color.web("#FF453A");

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Random random = new Random();
    private boolean isReturning = false;

    public ProductActionPanel(UserPanel.User user, UserPanel.ButtonModel product,
                              boolean isDarkMode, Color bgColor, Color labelColor,
                              Color secondaryColor, Runnable onBackCallback) {
        this.currentUser = user;
        this.product = product;
        this.isDarkMode = isDarkMode;
        this.currentBackgroundColor = bgColor;
        this.currentLabelColor = labelColor;
        this.currentSecondaryLabelColor = secondaryColor;
        this.onBackCallback = onBackCallback;
        this.primaryStage = Main.getInstance().getPrimaryStage();

        this.selectedSweetness = product.getSweetness();
        this.selectedCaffeine = product.getCaffeine();
        this.selectedTemperature = product.getTemperature();

        createUI();
        loadUserBackground();
        startAutoReturnTimer();
    }

    private void loadUserBackground() {
        try {
            String bgUrl = "https://menschwoodworks.ir/API/uploads/backgrounds/" +
                    (currentUser.getBackgroundImage() != null ? currentUser.getBackgroundImage() : "default_bg.jpg");
            Image bgImage = new Image(bgUrl, true);
            backgroundImageView = new ImageView(bgImage);
            backgroundImageView.fitWidthProperty().bind(root.widthProperty());
            backgroundImageView.fitHeightProperty().bind(root.heightProperty());
            backgroundImageView.setPreserveRatio(false);
            backgroundImageView.setOpacity(0.5);
            backgroundImageView.setEffect(new GaussianBlur(4));

            // اضافه کردن مستقیم به root (پایین‌ترین لایه)
            root.getChildren().add(0, backgroundImageView);

            // تنظیم پس‌زمینه Pane به شفاف
            if (root.getChildren().size() > 1 && root.getChildren().get(1) instanceof Pane) {
                ((Pane) root.getChildren().get(1)).setStyle("-fx-background-color: transparent;");
            }
        } catch (Exception e) {
            System.err.println("Error loading background: " + e.getMessage());
        }
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        // پس‌زمینه ذرات (با پس‌زمینه شفاف)
        Pane backgroundLayer = createBackgroundLayer();
        backgroundLayer.setStyle("-fx-background-color: transparent;");

        // محتوای اصلی
        mainContainer = createMainContainer();

        root.getChildren().addAll(backgroundLayer, mainContainer);

        // انیمیشن ورود
        root.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private Pane createBackgroundLayer() {
        Pane bg = new Pane();
        bg.setStyle("-fx-background-color: transparent;"); // شفاف

        // ذرات معلق با opacity کمتر
        for (int i = 0; i < 15; i++) {
            Circle particle = new Circle(random.nextDouble() * 4 + 1);
            particle.setCenterX(random.nextDouble() * 1400);
            particle.setCenterY(random.nextDouble() * 900);
            particle.setFill(Color.web(isDarkMode ? "rgba(255,255,255,0.05)" : "rgba(0,0,0,0.03)"));

            TranslateTransition floatAnim = new TranslateTransition(
                    Duration.seconds(15 + random.nextDouble() * 10), particle);
            floatAnim.setFromY(particle.getCenterY());
            floatAnim.setToY(particle.getCenterY() - 40 - random.nextDouble() * 30);
            floatAnim.setCycleCount(Animation.INDEFINITE);
            floatAnim.setAutoReverse(true);
            floatAnim.play();

            bg.getChildren().add(particle);
        }

        return bg;
    }

    private BorderPane createMainContainer() {
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(30));
        container.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(44, 44, 46, 0.9);" :  // کمی شفاف‌تر
                        "rgba(255, 255, 255, 0.9);") +
                        "-fx-background-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 10);" +
                        "-fx-border-color: " + toRgbString(IOS_SYSTEM_BLUE) + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 30;"
        );
        container.setMaxWidth(1200);
        container.setMaxHeight(700);
        container.setPrefWidth(1100);
        container.setPrefHeight(650);

        // ===== HEADER - تایمر =====
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

//        Label timerIcon = new Label("⏱️");
//        timerIcon.setFont(Font.font("SF Pro Display", 22));

        timerLabel = new Label("20");
        timerLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 24));
        timerLabel.setTextFill(IOS_SYSTEM_ORANGE);

        Label timerText = new Label("ثانیه تا بازگشت خودکار");
        timerText.setFont(Font.font("SF Pro Text", 14));
        timerText.setTextFill(currentSecondaryLabelColor);
        timerText.setPadding(new Insets(0, 0, 0, 5));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(timerText,timerLabel, spacer);

        container.setTop(headerBox);

        // ===== CENTER - محتوای اصلی (افقی) =====
        HBox centerBox = new HBox(40);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(10, 0, 20, 0));

        // بخش راست - تصویر محصول (40% عرض)
        VBox rightBox = new VBox(25);
        rightBox.setAlignment(Pos.CENTER);
        rightBox.setPrefWidth(350);
        rightBox.setPadding(new Insets(0, 20, 0, 0));

        // تصویر محصول
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(200, 200);
        imageContainer.setMaxSize(200, 200);
        imageContainer.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(255,255,255,0.1);" :
                        "rgba(0,0,0,0.03);") +
                        "-fx-background-radius: 100;" +
                        "-fx-effect: dropshadow(gaussian, " + toRgbaString(IOS_SYSTEM_BLUE, 0.3) + ", 20, 0, 0, 5);" +
                        "-fx-border-color: " + toRgbString(IOS_SYSTEM_BLUE) + ";" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 100;"
        );

        try {
            String imageUrl = "https://menschwoodworks.ir/API/uploads/buttons/" + product.getImage();
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                Image productImage = new Image(imageUrl, 200, 200, true, true, true);
                ImageView imageView = new ImageView(productImage);
                imageView.setFitWidth(180);
                imageView.setFitHeight(180);
                imageView.setPreserveRatio(true);

                Circle clip = new Circle(90);
                clip.setCenterX(90);
                clip.setCenterY(90);
                imageView.setClip(clip);
                imageContainer.getChildren().add(imageView);
            } else {
                Label emoji = new Label(getEmojiForProduct(product.getTitle()));
                emoji.setFont(Font.font("SF Pro Display", 100));
                imageContainer.getChildren().add(emoji);
            }
        } catch (Exception e) {
            Label emoji = new Label(getEmojiForProduct(product.getTitle()));
            emoji.setFont(Font.font("SF Pro Display", 100));
            imageContainer.getChildren().add(emoji);
        }

        // عنوان محصول
        Label titleLabel = new Label(product.getTitle());
        titleLabel.setFont(Font.font("SF Pro Display", FontWeight.SEMI_BOLD, 24));
        titleLabel.setTextFill(currentLabelColor);
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setMaxWidth(300);

        // قیمت
        Label priceLabel = new Label(product.getPriceFormatted());
        priceLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 28));
        priceLabel.setTextFill(IOS_SYSTEM_ORANGE);
        priceLabel.setEffect(new DropShadow(8, IOS_SYSTEM_ORANGE.darker()));

        rightBox.getChildren().addAll(imageContainer, titleLabel, priceLabel);

        // بخش چپ - گزینه‌های سفارشی‌سازی (60% عرض)
        VBox leftBox = new VBox(20);
        leftBox.setAlignment(Pos.TOP_CENTER);
        leftBox.setPrefWidth(550);
        leftBox.setPadding(new Insets(0, 0, 0, 20));
        leftBox.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(255,255,255,0.05);" :
                        "rgba(0,0,0,0.02);") +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 25;"
        );

        Label optionsTitle = new Label("تنظیمات سفارشی");
        optionsTitle.setFont(Font.font("SF Pro Display", FontWeight.SEMI_BOLD, 22));
        optionsTitle.setTextFill(currentLabelColor);
        optionsTitle.setPadding(new Insets(0, 0, 15, 0));

        leftBox.getChildren().add(optionsTitle);

        if (product.hasSweetness()) {
            VBox sweetnessBox = createOptionControl(
                    "🍬 میزان شیرینی",
                    product.getSweetness(),
                    value -> selectedSweetness = value,
                    IOS_SYSTEM_ORANGE
            );
            leftBox.getChildren().add(sweetnessBox);
        }

        if (product.hasCaffeine()) {
            VBox caffeineBox = createOptionControl(
                    "☕ میزان کافئین",
                    product.getCaffeine(),
                    value -> selectedCaffeine = value,
                    IOS_SYSTEM_GREEN
            );
            leftBox.getChildren().add(caffeineBox);
        }

        if (product.hasTemperature()) {
            VBox tempBox = createOptionControl(
                    "🌡️ دمای سرو",
                    product.getTemperature(),
                    value -> selectedTemperature = value,
                    IOS_SYSTEM_BLUE,
                    0, 100, "°C"
            );
            leftBox.getChildren().add(tempBox);
        }

        if (!product.hasSweetness() && !product.hasCaffeine() && !product.hasTemperature()) {
            Label noOptionsLabel = new Label("این محصول فاقد گزینه‌های سفارشی است");
            noOptionsLabel.setFont(Font.font("SF Pro Text", 15));
            noOptionsLabel.setTextFill(currentSecondaryLabelColor);
            noOptionsLabel.setPadding(new Insets(30, 0, 30, 0));
            leftBox.getChildren().add(noOptionsLabel);
        }

        centerBox.getChildren().addAll(rightBox, leftBox);
        container.setCenter(centerBox);

        // ===== FOOTER - دکمه‌ها =====
        HBox footerBox = new HBox(20);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(20, 0, 0, 0));

        Button confirmBtn = createActionButton("✓ ثبت سفارش", IOS_SYSTEM_GREEN);
        Button backBtn = createActionButton("✕ بازگشت", IOS_SYSTEM_RED);

        confirmBtn.setOnAction(e -> confirmOrder());
        backBtn.setOnAction(e -> returnToMain());

        footerBox.getChildren().addAll(confirmBtn, backBtn);
        container.setBottom(footerBox);

        return container;
    }

    private VBox createOptionControl(String label, int defaultValue,
                                     java.util.function.IntConsumer valueConsumer, Color color) {
        return createOptionControl(label, defaultValue, valueConsumer, color, 0, 10, "");
    }

    private VBox createOptionControl(String label, int defaultValue,
                                     java.util.function.IntConsumer valueConsumer,
                                     Color color, int min, int max, String unit) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10, 5, 10, 5));
        box.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(255,255,255,0.03);" :
                        "rgba(0,0,0,0.01);") +
                        "-fx-background-radius: 12;"
        );

        // عنوان
        Label labelText = new Label(label);
        labelText.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 15));
        labelText.setTextFill(currentSecondaryLabelColor);

        // نمایش مقدار فعلی
        Label valueLabel = new Label(defaultValue + unit);
        valueLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        valueLabel.setTextFill(color);
        valueLabel.setPadding(new Insets(0, 0, 0, 10));

        HBox valueBox = new HBox(15);
        valueBox.setAlignment(Pos.CENTER_LEFT);

        // اسلایدر
        Slider slider = new Slider(min, max, defaultValue);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit((max - min) / 5);
        slider.setMinorTickCount(1);
        slider.setSnapToTicks(true);
        slider.setPrefWidth(380);
        slider.setStyle(
                "-fx-control-inner-background: " + (isDarkMode ?
                        "rgba(255,255,255,0.15);" :
                        "rgba(0,0,0,0.08);") +
                        "-fx-accent: " + toRgbString(color) + ";"
        );

        // ریست تایمر با تغییر اسلایدر
        slider.setOnMousePressed(e -> resetTimer());
        slider.setOnMouseDragged(e -> resetTimer());

        // آپدیت مقدار
        slider.valueProperty().addListener((obs, old, newVal) -> {
            int intVal = newVal.intValue();
            valueLabel.setText(intVal + unit);
            valueConsumer.accept(intVal);
        });

        valueBox.getChildren().addAll(valueLabel, slider);
        box.getChildren().addAll(labelText, valueBox);

        return box;
    }

    private Button createActionButton(String text, Color color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 16));
        btn.setTextFill(Color.WHITE);
        btn.setPadding(new Insets(12, 35, 12, 35));
        btn.setStyle(
                "-fx-background-color: " + toRgbString(color) + ";" +
                        "-fx-background-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, " + toRgbaString(color, 0.4) + ", 15, 0, 0, 3);" +
                        "-fx-cursor: hand;"
        );
        btn.setPrefWidth(160);

        btn.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), btn);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
            btn.setEffect(new DropShadow(20, color));
        });

        btn.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), btn);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
            btn.setEffect(new DropShadow(12, color.darker()));
        });

        btn.setOnMousePressed(e -> {
            ScaleTransition press = new ScaleTransition(Duration.millis(50), btn);
            press.setToX(0.95);
            press.setToY(0.95);
            press.play();
            resetTimer();
        });

        btn.setOnMouseReleased(e -> {
            ScaleTransition release = new ScaleTransition(Duration.millis(50), btn);
            release.setToX(1.0);
            release.setToY(1.0);
            release.play();
        });

        return btn;
    }

    private void startAutoReturnTimer() {
        autoReturnTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    int time = Integer.parseInt(timerLabel.getText()) - 1;
                    timerLabel.setText(String.valueOf(time));

                    if (time <= 5) {
                        timerLabel.setTextFill(IOS_SYSTEM_RED);
                        ScaleTransition pulse = new ScaleTransition(Duration.millis(300), timerLabel);
                        pulse.setToX(1.2);
                        pulse.setToY(1.2);
                        pulse.setAutoReverse(true);
                        pulse.setCycleCount(2);
                        pulse.play();
                    }

                    if (time <= 0 && !isReturning) {
                        autoReturnTimeline.stop();
                        returnToMain();
                    }
                })
        );
        autoReturnTimeline.setCycleCount(20);
        autoReturnTimeline.play();
    }

    private void resetTimer() {
        if (autoReturnTimeline != null && !isReturning) {
            autoReturnTimeline.stop();
            timerLabel.setText("20");
            timerLabel.setTextFill(IOS_SYSTEM_ORANGE);
            startAutoReturnTimer();
        }
    }

    private void confirmOrder() {
        if (isReturning) return;
        isReturning = true;

        if (autoReturnTimeline != null) {
            autoReturnTimeline.stop();
        }

        System.out.println("=== ثبت سفارش ===");
        System.out.println("User ID: " + currentUser.getId());
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("Product ID: " + product.getId());
        System.out.println("Product Title: " + product.getTitle());
        System.out.println("Price: " + product.getPrice());
        System.out.println("Sweetness: " + selectedSweetness);
        System.out.println("Caffeine: " + selectedCaffeine);
        System.out.println("Temperature: " + selectedTemperature);

        // ثبت فروش در دیتابیس
        int saleId = api.addSale(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getFullname(),
                product.getId(),
                product.getTitle(),
                product.getImage(),
                1, // quantity
                product.getPrice(),
                selectedSweetness,
                selectedCaffeine,
                selectedTemperature,
                "CARD"
        );

        if (saleId > 0) {
            System.out.println("✅ Sale recorded with ID: " + saleId);
        } else {
            System.out.println("❌ Sale recording FAILED!");
        }

        // نمایش پیام موفقیت
        VBox successBox = new VBox(25);
        successBox.setAlignment(Pos.CENTER);
        successBox.setPadding(new Insets(40));
        successBox.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(44, 44, 46, 0.98);" :
                        "rgba(255, 255, 255, 0.98);") +
                        "-fx-background-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 40, 0, 0, 10);"
        );
        successBox.setMaxWidth(450);

        Label successIcon = new Label("✅");
        successIcon.setFont(Font.font("SF Pro Display", 70));

        Label successTitle = new Label("سفارش شما ثبت شد!");
        successTitle.setFont(Font.font("SF Pro Display", FontWeight.SEMI_BOLD, 26));
        successTitle.setTextFill(currentLabelColor);

        Label productLabel = new Label(product.getTitle());
        productLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 22));
        productLabel.setTextFill(IOS_SYSTEM_ORANGE);

        Label priceLabel = new Label(product.getPriceFormatted());
        priceLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 24));
        priceLabel.setTextFill(IOS_SYSTEM_ORANGE);

        // دکمه بازگشت به صفحه اصلی
        Button okBtn = createActionButton("باشه", IOS_SYSTEM_GREEN);
        okBtn.setOnAction(e -> {
            // انیمیشن محو شدن
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setInterpolator(Interpolator.EASE_BOTH);
            fadeOut.setOnFinished(ev -> {
                if (onBackCallback != null) {
                    onBackCallback.run(); // بازگشت به پنل اصلی
                }
            });
            fadeOut.play();
        });

        successBox.getChildren().addAll(successIcon, successTitle, productLabel, priceLabel, okBtn);

        // جایگزینی محتوای اصلی با صفحه موفقیت
        mainContainer.setCenter(successBox);
        mainContainer.setTop(null);
        mainContainer.setBottom(null);
    }

    private void returnToMain() {
        if (isReturning) return;
        isReturning = true;

        if (autoReturnTimeline != null) {
            autoReturnTimeline.stop();
        }

        // انیمیشن محو شدن
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(Interpolator.EASE_BOTH);

        fadeOut.setOnFinished(e -> {
            if (onBackCallback != null) {
                // اجرای کال‌بک بازگشت - SceneManager کار انیمیشن را انجام می‌دهد
                onBackCallback.run();
            }
        });

        fadeOut.play();
    }

    private String getEmojiForProduct(String title) {
        String lowerTitle = title.toLowerCase();
        if (lowerTitle.contains("قهوه") || lowerTitle.contains("اسپرسو") ||
                lowerTitle.contains("کاپوچینو") || lowerTitle.contains("لاته")) {
            return "☕";
        } else if (lowerTitle.contains("چای")) {
            return "🍵";
        } else if (lowerTitle.contains("نوشابه") || lowerTitle.contains("کوکا") ||
                lowerTitle.contains("پپسی")) {
            return "🥤";
        } else if (lowerTitle.contains("آبمیوه") || lowerTitle.contains("آب") ||
                lowerTitle.contains("نوشیدنی")) {
            return "🧃";
        } else if (lowerTitle.contains("شکلات")) {
            return "🍫";
        } else if (lowerTitle.contains("کیک") || lowerTitle.contains("کاپ کیک")) {
            return "🍰";
        } else if (lowerTitle.contains("شیر") || lowerTitle.contains("کاکائو")) {
            return "🥛";
        } else if (lowerTitle.contains("بستنی")) {
            return "🍦";
        }
        return "🧃";
    }

    private String toRgbString(Color color) {
        // استفاده از روش darker()
        Color darkerColor = color.darker(); // یا color.darker().darker() برای تیره‌تر بیشتر

        return String.format("#%02X%02X%02X",
                (int) (darkerColor.getRed() * 255),
                (int) (darkerColor.getGreen() * 255),
                (int) (darkerColor.getBlue() * 255));
    }

    private String toRgbaString(Color color, double alpha) {
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                alpha);
    }

    public Parent getRoot() {
        return root;
    }

    public void cleanup() {
        if (autoReturnTimeline != null) {
            autoReturnTimeline.stop();
        }
        scheduler.shutdown();
    }
}