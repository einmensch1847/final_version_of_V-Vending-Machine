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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScreenSaver {

    private StackPane root;
    private Stage primaryStage;
    private Parent previousPanel;
    private Runnable onExitCallback;
    private ApiClient api = new ApiClient();

    // UI Components
    private StackPane imageContainer;
    private ImageView currentImageView;
    private Label timerLabel;
    private Label captionLabel;
    private Label counterLabel;
    private ProgressIndicator loadingIndicator;
    private HBox indicatorDots;
    private List<Circle> dotIndicators = new ArrayList<>();

    // Animation
    private Timeline slideshowTimeline;
    private Timeline countdownTimeline;
    private int currentImageIndex = 0;
    private List<String> imageUrls = new ArrayList<>();
    private List<String> imageCaptions = new ArrayList<>(); // برای زیرنویس اختصاصی هر تصویر
    private int countdown = 5;

    // Colors
    private static final Color BG_PRIMARY = Color.web("#0a0c14");
    private static final Color BG_SECONDARY = Color.web("#141824");
    private static final Color ACCENT_COLOR = Color.web("#10b981");
    private static final Color ACCENT_GLOW = Color.web("rgba(16, 185, 129, 0.3)");
    private static final Color TEXT_PRIMARY = Color.web("#f8fafc");
    private static final Color TEXT_SECONDARY = Color.web("#cbd5e1");
    private static final Color GLASS_BG = Color.web("rgba(20, 24, 36, 0.6)");
    private static final Color GLASS_BORDER = Color.web("rgba(255, 255, 255, 0.1)");

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean isLoading = false;
    private boolean isPaused = false;
    private List<Circle> floatingParticles = new ArrayList<>();

    public ScreenSaver(Stage stage, Parent previousPanel, Runnable onExitCallback) {
        this.primaryStage = stage;
        this.previousPanel = previousPanel;
        this.onExitCallback = onExitCallback;
        createUI();
        loadAdImages();
        startFloatingParticles();
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #0a0c14;");

        // ===== لایه‌های پس‌زمینه =====
        Pane backgroundLayer = createBackgroundLayer();

        // ===== محتوای اصلی با افکت شیشه =====
        VBox mainContainer = createMainContainer();

        root.getChildren().addAll(backgroundLayer, mainContainer);
        root.setOpacity(0);

        // رویدادهای بازگشت
        root.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> exitScreenSaver());
        root.addEventFilter(KeyEvent.KEY_PRESSED, e -> exitScreenSaver());

        // انیمیشن ورود
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);
        fadeIn.play();

        // انیمیشن مقیاس ورود
        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(1.2), root);
        scaleIn.setFromX(1.05);
        scaleIn.setFromY(1.05);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);
        scaleIn.play();
    }

    private Pane createBackgroundLayer() {
        Pane bg = new Pane();
        bg.setStyle("-fx-background-color: #0a0c14;");

        // گرادیان عمیق
        Rectangle gradientBg = new Rectangle();
        gradientBg.widthProperty().bind(root.widthProperty());
        gradientBg.heightProperty().bind(root.heightProperty());

        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0c14")),
                new Stop(0.5, Color.web("#141824")),
                new Stop(1, Color.web("#1e1a2c"))
        );
        gradientBg.setFill(gradient);
        bg.getChildren().add(gradientBg);

        // ذرات معلق (برای عمق)
        for (int i = 0; i < 30; i++) {
            double size = Math.random() * 4 + 1;
            Circle particle = new Circle(size);
            particle.setCenterX(Math.random() * 1600);
            particle.setCenterY(Math.random() * 900);
            particle.setFill(Color.web("rgba(255,255,255,0.03)"));
            particle.setEffect(new GaussianBlur(2));

            // انیمیشن حرکت آرام
            TranslateTransition floatAnim = new TranslateTransition(
                    Duration.seconds(40 + Math.random() * 30), particle);
            floatAnim.setByX((Math.random() - 0.5) * 200);
            floatAnim.setByY((Math.random() - 0.5) * 200);
            floatAnim.setCycleCount(Animation.INDEFINITE);
            floatAnim.setAutoReverse(true);
            floatAnim.play();

            bg.getChildren().add(particle);
            floatingParticles.add(particle);
        }

        return bg;
    }

    private VBox createMainContainer() {
        VBox mainContainer = new VBox(25);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setMaxWidth(1200);
        mainContainer.setMaxHeight(800);

        // ===== هدر =====
        HBox headerBox = createHeader();

        // ===== کانتینر تصویر =====
        createImageContainer();

        // ===== کنترل‌ها و وضعیت =====
        HBox controlsBox = createControls();

        // ===== فوتر با تایمر =====
        HBox footerBox = createFooter();

        mainContainer.getChildren().addAll(headerBox, imageContainer, controlsBox, footerBox);

        // افکت شیشه برای کل کانتینر
        mainContainer.setStyle(
                "-fx-background-color: rgba(10, 12, 20, 0.4);" +
                        "-fx-background-radius: 40;" +
                        "-fx-border-color: rgba(255,255,255,0.05);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 40;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 40, 0, 0, 10);"
        );

        return mainContainer;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 20, 10, 20));

        // لوگو
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Circle logoGlow = new Circle(18);
        logoGlow.setFill(ACCENT_GLOW);
        logoGlow.setEffect(new GaussianBlur(10));

        Label logoIcon = new Label("✨");
        logoIcon.setFont(Font.font("SF Pro Display", 28));
        logoIcon.setTextFill(ACCENT_COLOR);

        StackPane logoContainer = new StackPane();
        logoContainer.getChildren().addAll(logoGlow, logoIcon);

        Label logoText = new Label("VVM Gallery");
        logoText.setFont(Font.font("SF Pro Display", FontWeight.SEMI_BOLD, 20));
        logoText.setTextFill(TEXT_PRIMARY);

        logoBox.getChildren().addAll(logoContainer, logoText);

        // شمارنده تصاویر
        counterLabel = new Label("0 / 0");
        counterLabel.setFont(Font.font("SF Pro Mono", FontWeight.MEDIUM, 16));
        counterLabel.setTextFill(TEXT_SECONDARY);
        counterLabel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 6 18;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(logoBox, spacer, counterLabel);
        return header;
    }

    private void createImageContainer() {
        imageContainer = new StackPane();
        imageContainer.setPrefSize(1000, 550);
        imageContainer.setMaxSize(1000, 550);
        imageContainer.setStyle(
                "-fx-background-color: rgba(0,0,0,0.3);" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: " + toRgbString(ACCENT_COLOR) + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, " + toRgbaString(ACCENT_COLOR, 0.2) + ", 30, 0, 0, 5);"
        );

        // تصویر فعلی
        currentImageView = new ImageView();
        currentImageView.setPreserveRatio(true);
        currentImageView.setFitWidth(950);
        currentImageView.setFitHeight(500);
        currentImageView.setOpacity(0);
        currentImageView.setVisible(false);

        // لودینگ
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: " + toRgbString(ACCENT_COLOR) + ";");
        loadingIndicator.setMaxSize(60, 60);
        loadingIndicator.setVisible(true);

        imageContainer.getChildren().addAll(loadingIndicator, currentImageView);
    }

    private HBox createControls() {
        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(15, 20, 15, 20));

        // زیرنویس تصویر
        captionLabel = new Label("در حال بارگذاری گالری...");
        captionLabel.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 15));
        captionLabel.setTextFill(TEXT_PRIMARY);
        captionLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.4);" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 8 25;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 25;"
        );

        // نقاط نشانگر صفحه
        indicatorDots = new HBox(10);
        indicatorDots.setAlignment(Pos.CENTER);
        indicatorDots.setPadding(new Insets(0, 10, 0, 10));

        controls.getChildren().addAll(captionLabel, indicatorDots);
        return controls;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15, 20, 10, 20));

        // تایمر بازگشت با طراحی مدرن
        HBox timerBox = new HBox(15);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.setStyle(
                "-fx-background-color: rgba(0,0,0,0.3);" +
                        "-fx-background-radius: 40;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 40;" +
                        "-fx-padding: 12 30;"
        );

        // انیمیشن چرخش برای آیکون تایمر
        Label timerIcon = new Label("⏱️");
        timerIcon.setFont(Font.font("SF Pro Display", 24));

        RotateTransition rotateIcon = new RotateTransition(Duration.seconds(3), timerIcon);
        rotateIcon.setByAngle(10);
        rotateIcon.setAutoReverse(true);
        rotateIcon.setCycleCount(Animation.INDEFINITE);
        rotateIcon.play();

        timerLabel = new Label("5");
        timerLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 36));
        timerLabel.setTextFill(ACCENT_COLOR);

        // افکت درخشش برای تایمر
        Bloom bloom = new Bloom(0.3);
        timerLabel.setEffect(bloom);

        Label timerText = new Label("ثانیه تا بازگشت");
        timerText.setFont(Font.font("SF Pro Text", 14));
        timerText.setTextFill(TEXT_SECONDARY);

        timerBox.getChildren().addAll(timerIcon, timerLabel, timerText);

        // راهنما
        Label hintLabel = new Label("برای بازگشت کلیک کنید یا کلیدی را فشار دهید");
        hintLabel.setFont(Font.font("SF Pro Text", 12));
        hintLabel.setTextFill(Color.web("rgba(255,255,255,0.4)"));

        VBox footerContent = new VBox(10);
        footerContent.setAlignment(Pos.CENTER);
        footerContent.getChildren().addAll(timerBox, hintLabel);

        footer.getChildren().add(footerContent);
        return footer;
    }

    private void updateIndicators() {
        indicatorDots.getChildren().clear();
        dotIndicators.clear();

        for (int i = 0; i < imageUrls.size(); i++) {
            Circle dot = new Circle(5);
            if (i == currentImageIndex) {
                dot.setFill(ACCENT_COLOR);
                dot.setEffect(new DropShadow(10, ACCENT_COLOR));

                // انیمیشن پالس برای نقطه فعال
                ScaleTransition pulse = new ScaleTransition(Duration.seconds(1), dot);
                pulse.setToX(1.3);
                pulse.setToY(1.3);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.play();
            } else {
                dot.setFill(Color.web("rgba(255,255,255,0.2)"));
            }
            dot.setStroke(Color.TRANSPARENT);
            dotIndicators.add(dot);
            indicatorDots.getChildren().add(dot);
        }
    }

    private void loadAdImages() {
        loadingIndicator.setVisible(true);
        loadingIndicator.setProgress(-1);

        scheduler.submit(() -> {
            List<String> ads = api.getAdImages();
            Platform.runLater(() -> {
                if (ads != null && !ads.isEmpty()) {
                    imageUrls.clear();
                    imageCaptions.clear();

                    for (int i = 0; i < ads.size(); i++) {
                        String adName = ads.get(i);
                        if (adName != null && !adName.trim().isEmpty() && !adName.equals("NO_ADS")) {
                            String encodedName = adName.replace(" ", "%20")
                                    .replace("(", "%28")
                                    .replace(")", "%29");
                            String url = "https://menschwoodworks.ir/API/uploads/ads/" + encodedName;
                            imageUrls.add(url);

                            // ایجاد زیرنویس بر اساس نام فایل
                            String caption = createCaptionFromFilename(adName);
                            imageCaptions.add(caption);

                            System.out.println("Ad URL: " + url);
                        }
                    }

                    counterLabel.setText(imageUrls.size() + " تصویر");
                    updateIndicators();

                    if (!imageUrls.isEmpty()) {
                        loadingIndicator.setVisible(false);
                        startSlideshow();
                    } else {
                        showEmptyState();
                    }
                } else {
                    showEmptyState();
                }
            });
        });
    }

    private String createCaptionFromFilename(String filename) {
        // حذف پسوند و تبدیل به متن خوانا
        String name = filename.replaceAll("\\.[^.]*$", ""); // حذف پسوند
        name = name.replace("-", " ")
                .replace("_", " ")
                .replace("  ", " ");

        // اگر نام انگلیسی بود، همینطور نشون بده، وگرنه پیش‌فرض
        if (name.matches(".*[a-zA-Z].*")) {
            return name;
        }
        return "✨ تبلیغ ویژه";
    }

    private void showEmptyState() {
        loadingIndicator.setVisible(false);
        captionLabel.setText("گالری در حال به‌روزرسانی");

        Label noImageLabel = new Label("🎨");
        noImageLabel.setFont(Font.font("SF Pro Display", 100));
        noImageLabel.setTextFill(ACCENT_COLOR);

        // انیمیشن نوسان برای ایموجی
        RotateTransition rotate = new RotateTransition(Duration.seconds(3), noImageLabel);
        rotate.setByAngle(5);
        rotate.setAutoReverse(true);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.play();

        imageContainer.getChildren().add(noImageLabel);
    }

    private void startSlideshow() {
        if (imageUrls.isEmpty()) return;

        // نمایش اولین تصویر
        loadImageAtIndex(0);
        currentImageIndex = 0;

        // تایمر بازگشت
        startCountdownTimer();

        // اسلایدشو
        slideshowTimeline = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> {
                    if (!isPaused) {
                        nextImage();
                    }
                })
        );
        slideshowTimeline.setCycleCount(Timeline.INDEFINITE);
        slideshowTimeline.play();
    }

    private void nextImage() {
        if (imageUrls.isEmpty() || isLoading || isPaused) return;

        currentImageIndex = (currentImageIndex + 1) % imageUrls.size();
        loadImageAtIndex(currentImageIndex);
        updateIndicators();
    }

    private void loadImageAtIndex(int index) {
        if (isLoading) return;
        isLoading = true;

        String url = imageUrls.get(index);
        String caption = (index < imageCaptions.size()) ? imageCaptions.get(index) : "تبلیغ ویژه";

        // انیمیشن محو شدن
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), currentImageView);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(Interpolator.EASE_IN);

        fadeOut.setOnFinished(e -> {
            Image newImage = new Image(url, true);

            newImage.progressProperty().addListener((obs, old, progress) -> {
                if (progress.doubleValue() == 1.0) {
                    Platform.runLater(() -> {
                        currentImageView.setImage(newImage);
                        currentImageView.setVisible(true);
                        loadingIndicator.setVisible(false);

                        // انیمیشن ظاهر شدن
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), currentImageView);
                        fadeIn.setToValue(1);
                        fadeIn.setInterpolator(Interpolator.EASE_OUT);
                        fadeIn.play();

                        // انیمیشن مقیاس
                        ScaleTransition scale = new ScaleTransition(Duration.millis(600), currentImageView);
                        scale.setFromX(0.95);
                        scale.setFromY(0.95);
                        scale.setToX(1);
                        scale.setToY(1);
                        scale.setInterpolator(Interpolator.EASE_OUT);
                        scale.play();

                        // زیرنویس
                        captionLabel.setText(caption);
                        counterLabel.setText((index + 1) + " / " + imageUrls.size());

                        // انیمیشن زیرنویس
                        FadeTransition captionFade = new FadeTransition(Duration.millis(300), captionLabel);
                        captionFade.setToValue(1);
                        captionFade.play();

                        isLoading = false;
                        resetTimer();
                    });
                }
            });

            newImage.errorProperty().addListener((obs, oldErr, newErr) -> {
                if (newErr) {
                    System.err.println("Error loading image: " + url);
                    Platform.runLater(() -> {
                        currentImageView.setVisible(false);
                        loadingIndicator.setVisible(false);

                        // نمایش ایموجی خطا
                        Label errorLabel = new Label("🖼️");
                        errorLabel.setFont(Font.font("SF Pro Display", 80));
                        errorLabel.setTextFill(ACCENT_COLOR);
                        imageContainer.getChildren().add(errorLabel);

                        captionLabel.setText("خطا در بارگذاری تصویر");

                        // بعد از ۲ ثانیه به تصویر بعدی برو
                        Timeline retry = new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
                            imageContainer.getChildren().remove(errorLabel);
                            loadingIndicator.setVisible(true);
                            isLoading = false;
                            nextImage();
                        }));
                        retry.setCycleCount(1);
                        retry.play();
                    });
                }
            });
        });

        fadeOut.play();
    }

    private void startCountdownTimer() {
        countdown = 10;
        timerLabel.setText(String.valueOf(countdown));
        timerLabel.setTextFill(ACCENT_COLOR);

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    countdown--;
                    timerLabel.setText(String.valueOf(countdown));

                    // انیمیشن نبض برای ثانیه‌های آخر
                    if (countdown <= 2) {
                        ScaleTransition pulse = new ScaleTransition(Duration.millis(300), timerLabel);
                        pulse.setToX(1.4);
                        pulse.setToY(1.4);
                        pulse.setAutoReverse(true);
                        pulse.setCycleCount(2);
                        pulse.play();

                        timerLabel.setTextFill(Color.web("#ef4444"));
                    }

                    if (countdown <= 0) {
                        resetTimer();
                    }
                })
        );
        countdownTimeline.setCycleCount(10);
        countdownTimeline.play();
    }

    private void resetTimer() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        startCountdownTimer();
        timerLabel.setTextFill(ACCENT_COLOR);
    }

    private void startFloatingParticles() {
        Timeline particleTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.1), e -> {
                    for (Circle particle : floatingParticles) {
                        if (Math.random() < 0.02) {
                            // تغییر opacity تصادفی برای اثر چشمک زدن
                            FadeTransition flicker = new FadeTransition(Duration.millis(500), particle);
                            flicker.setFromValue(0.3);
                            flicker.setToValue(0.8);
                            flicker.setAutoReverse(true);
                            flicker.setCycleCount(2);
                            flicker.play();
                        }
                    }
                })
        );
        particleTimeline.setCycleCount(Timeline.INDEFINITE);
        particleTimeline.play();
    }

    private void exitScreenSaver() {
        if (slideshowTimeline != null) {
            slideshowTimeline.stop();
        }
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        // انیمیشن خروج
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), root);
        fadeOut.setToValue(0);

        ScaleTransition scaleOut = new ScaleTransition(Duration.seconds(0.5), root);
        scaleOut.setToX(1.05);
        scaleOut.setToY(1.05);

        ParallelTransition exitAnim = new ParallelTransition(fadeOut, scaleOut);
        exitAnim.setInterpolator(Interpolator.EASE_IN);

        exitAnim.setOnFinished(e -> {
            if (onExitCallback != null) {
                onExitCallback.run();
            }
        });

        exitAnim.play();
    }

    private String toRgbString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
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
        if (slideshowTimeline != null) {
            slideshowTimeline.stop();
        }
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        scheduler.shutdown();
    }
}