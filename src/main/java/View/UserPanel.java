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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import ui.LoginPage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// ایمپورت‌های جدید
import View.IdleMonitor;
import View.ScreenSaver;

public class UserPanel {

    private StackPane root;
    private User currentUser;
    private ApiClient api = new ApiClient();
    private Stage primaryStage;

    // UI Components
    private FlowPane productsFlowPane;
    private Label welcomeLabel;
    private Label dateTimeLabel;
    private HBox paginationBox;
    private Button prevPageBtn;
    private Button nextPageBtn;
    private Label pageInfoLabel;
    private BorderPane mainContent;
    private ImageView logoImageView;
    private VBox centerContainer;

    // Data
    private List<ButtonModel> userButtons = new ArrayList<>();
    private int currentPage = 0;
    private int itemsPerPage = 10;
    private int totalPages = 0;

    // iOS 16 Real Glassmorphism Colors
    private static final Color IOS_SYSTEM_BLUE = Color.web("#0A84FF");
    private static final Color IOS_SYSTEM_GREEN = Color.web("#30D158");
    private static final Color IOS_SYSTEM_ORANGE = Color.web("#FF9F0A");
    private static final Color IOS_SYSTEM_RED = Color.web("#FF453A");
    private static final Color IOS_SYSTEM_PURPLE = Color.web("#BF5AF2");
    private static final Color IOS_SYSTEM_GRAY5_DARK = Color.web("rgba(44, 44, 46, 0.8)");
    private static final Color IOS_SYSTEM_GRAY5_LIGHT = Color.web("rgba(230, 230, 235, 0.8)");
    private static final Color IOS_SYSTEM_BACKGROUND_DARK = Color.web("#000000");
    private static final Color IOS_SYSTEM_BACKGROUND_LIGHT = Color.web("#F2F2F7");

    // iOS 16 Text Colors
    private static final Color IOS_LABEL_DARK = Color.web("#FFFFFF");
    private static final Color IOS_LABEL_LIGHT = Color.web("#1C1C1E");
    private static final Color IOS_SECONDARY_LABEL_DARK = Color.web("rgba(235, 235, 245, 0.6)");
    private static final Color IOS_SECONDARY_LABEL_LIGHT = Color.web("rgba(60, 60, 67, 0.6)");

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private List<Circle> bubbleParticles = new ArrayList<>();
    private Random random = new Random();

    // iOS 16 Dynamic Properties
    private boolean isDarkMode;
    private Color currentBackgroundColor;
    private Color currentLabelColor;
    private Color currentSecondaryLabelColor;

    // ✅ فیلدهای جدید برای ScreenSaver
    private IdleMonitor idleMonitor;
    private boolean isScreenSaverActive = false;

    // مدل دکمه برای کاربر
    public static class ButtonModel {
        private int id;
        private String title;
        private String caption;
        private String image;
        private double price;
        private int sweetness;
        private int caffeine;
        private int temperature;
        private int stock;

        public ButtonModel(int id, String title, String caption, String image, double price,
                           int sweetness, int caffeine, int temperature, int stock) {
            this.id = id;
            this.title = title;
            this.caption = caption;
            this.image = image;
            this.price = price;
            this.sweetness = sweetness;
            this.caffeine = caffeine;
            this.temperature = temperature;
            this.stock = stock;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getCaption() { return caption; }
        public String getImage() { return image; }
        public double getPrice() { return price; }
        public int getSweetness() { return sweetness; }
        public int getCaffeine() { return caffeine; }
        public int getTemperature() { return temperature; }
        public int getStock() { return stock; }

        public String getPriceFormatted() {
            return String.format("%,d", (int) price) + " تومان";
        }

        public boolean hasSweetness() { return sweetness > 0; }
        public boolean hasCaffeine() { return caffeine > 0; }
        public boolean hasTemperature() { return temperature > 0; }
    }

    // مدل کاربر
    public static class User {
        private int id;
        private String username;
        private String fullname;
        private String backgroundImage;
        private String deviceLocation;

        public User(int id, String username, String fullname, String backgroundImage, String deviceLocation) {
            this.id = id;
            this.username = username;
            this.fullname = fullname;
            this.backgroundImage = backgroundImage;
            this.deviceLocation = deviceLocation;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getFullname() { return fullname; }
        public String getBackgroundImage() { return backgroundImage; }
        public String getDeviceLocation() { return deviceLocation; }
    }

    public UserPanel(User user) {
        this.currentUser = user;
        this.primaryStage = Main.getInstance().getPrimaryStage();

        int hour = LocalDateTime.now().getHour();
        this.isDarkMode = hour >= 19 || hour < 7;

        updateIOSColors();
        createUI();
        loadLogo();
        loadUserButtons();
        startDateTimeUpdater();
        startBubbleAnimation();
    }

    private void updateIOSColors() {
        if (isDarkMode) {
            currentBackgroundColor = IOS_SYSTEM_BACKGROUND_DARK;
            currentLabelColor = IOS_LABEL_DARK;
            currentSecondaryLabelColor = IOS_SECONDARY_LABEL_DARK;
        } else {
            currentBackgroundColor = IOS_SYSTEM_BACKGROUND_LIGHT;
            currentLabelColor = IOS_LABEL_LIGHT;
            currentSecondaryLabelColor = IOS_SECONDARY_LABEL_LIGHT;
        }
    }

    private void loadLogo() {
        try {
            String logoUrl = "https://menschwoodworks.ir/API/uploads/logo/logo.jpg";
            Image logoImage = new Image(logoUrl, 40, 40, true, true, true);

            logoImage.progressProperty().addListener((obs, old, progress) -> {
                if (progress.doubleValue() == 1.0) {
                    Platform.runLater(() -> {
                        logoImageView = new ImageView(logoImage);
                        logoImageView.setFitWidth(40);
                        logoImageView.setFitHeight(40);
                        logoImageView.setPreserveRatio(true);
                        updateLogo();
                    });
                }
            });
        } catch (Exception e) {
            logoImageView = null;
        }
    }

    private void updateLogo() {
        if (mainContent != null && mainContent.getTop() != null) {
            VBox headerBox = (VBox) mainContent.getTop();
            if (!headerBox.getChildren().isEmpty()) {
                HBox topRow = (HBox) headerBox.getChildren().get(0);
                if (topRow != null && !topRow.getChildren().isEmpty()) {
                    HBox brandBox = (HBox) topRow.getChildren().get(0);
                    if (brandBox != null && !brandBox.getChildren().isEmpty()) {
                        StackPane logoContainer = (StackPane) brandBox.getChildren().get(0);
                        logoContainer.getChildren().clear();

                        if (logoImageView != null) {
                            Circle clip = new Circle(20);
                            clip.setCenterX(20);
                            clip.setCenterY(20);
                            logoImageView.setClip(clip);
                            logoContainer.getChildren().add(logoImageView);
                        } else {
                            Label logo = new Label("🧃");
                            logo.setStyle("-fx-font-size: 28px; -fx-font-family: 'SF Pro Display';");
                            logoContainer.getChildren().add(logo);
                        }
                    }
                }
            }
        }
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        StackPane layers = new StackPane();
        Pane backgroundLayer = createBackgroundLayer();
        mainContent = createMainContent();

        layers.getChildren().addAll(backgroundLayer, mainContent);
        root.getChildren().add(layers);

        root.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.4), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);
        fadeIn.play();
    }

    private Pane createBackgroundLayer() {
        Pane bg = new Pane();
        bg.setStyle("-fx-background-color: " + toRgbString(currentBackgroundColor) + ";");

        try {
            String bgUrl = "https://menschwoodworks.ir/API/uploads/backgrounds/" +
                    (currentUser.getBackgroundImage() != null ? currentUser.getBackgroundImage() : "default_bg.jpg");
            Image bgImage = new Image(bgUrl, true);
            ImageView bgView = new ImageView(bgImage);
            bgView.fitWidthProperty().bind(root.widthProperty());
            bgView.fitHeightProperty().bind(root.heightProperty());
            bgView.setPreserveRatio(false);
            bgView.setOpacity(0.5);
            bgView.setEffect(new GaussianBlur(4));
            bg.getChildren().add(bgView);
        } catch (Exception e) {
            // پس‌زمینه ساده
        }

        createIOSBubbles(bg);
        return bg;
    }

    private void createIOSBubbles(Pane bg) {
        bubbleParticles.clear();

        for (int i = 0; i < 25; i++) {
            double size = random.nextDouble() * 80 + 40;
            Circle bubble = new Circle(size);

            bubble.setCenterX(random.nextDouble() * 1600);
            bubble.setCenterY(random.nextDouble() * 1000);

            Color bubbleColor = isDarkMode ?
                    Color.web("rgba(255, 255, 255, 0.02)") :
                    Color.web("rgba(0, 0, 0, 0.01)");

            RadialGradient gradient = new RadialGradient(
                    0, 0, 0.3, 0.3, 0.7, true, CycleMethod.NO_CYCLE,
                    new Stop(0, bubbleColor),
                    new Stop(1, Color.TRANSPARENT)
            );

            bubble.setFill(gradient);
            bubble.setStroke(null);
            bubble.setEffect(new GaussianBlur(5));

            bg.getChildren().add(bubble);
            bubbleParticles.add(bubble);
        }
    }

    private void startBubbleAnimation() {
        for (Circle bubble : bubbleParticles) {
            double startX = bubble.getCenterX();
            double startY = bubble.getCenterY();

            Timeline bubbleAnim = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(bubble.centerXProperty(), startX, Interpolator.EASE_BOTH),
                            new KeyValue(bubble.centerYProperty(), startY, Interpolator.EASE_BOTH)
                    ),
                    new KeyFrame(Duration.seconds(60),
                            new KeyValue(bubble.centerXProperty(), startX + (random.nextDouble() - 0.5) * 200, Interpolator.EASE_BOTH),
                            new KeyValue(bubble.centerYProperty(), startY + (random.nextDouble() - 0.5) * 200, Interpolator.EASE_BOTH)
                    ),
                    new KeyFrame(Duration.seconds(120),
                            new KeyValue(bubble.centerXProperty(), startX, Interpolator.EASE_BOTH),
                            new KeyValue(bubble.centerYProperty(), startY, Interpolator.EASE_BOTH)
                    )
            );
            bubbleAnim.setCycleCount(Timeline.INDEFINITE);
            bubbleAnim.play();
        }
    }

    private BorderPane createMainContent() {
        BorderPane mainContent = new BorderPane();
        mainContent.setPadding(new Insets(20, 30, 20, 30));

        // ========== HEADER ==========
        VBox headerBox = new VBox(16);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(5, 0, 15, 0));

        // ردیف بالا
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setSpacing(20);

        HBox brandBox = createIOSBrandLogo();

        dateTimeLabel = new Label();
        dateTimeLabel.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 14));
        dateTimeLabel.setTextFill(currentSecondaryLabelColor);
        dateTimeLabel.setPadding(new Insets(8, 18, 8, 18));
        dateTimeLabel.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(44, 44, 46, 0.5);" :
                        "rgba(255, 255, 255, 0.5);") +
                        "-fx-background-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topRow.getChildren().addAll(brandBox, spacer, dateTimeLabel);

        // ردیف خوش‌آمدگویی
        HBox welcomeRow = new HBox();
        welcomeRow.setAlignment(Pos.CENTER_LEFT);
        welcomeRow.setSpacing(20);

        VBox welcomeBox = new VBox(4);
        String greeting = getGreeting();
        welcomeLabel = new Label(greeting + "، " + currentUser.getFullname());
        welcomeLabel.setFont(Font.font("SF Pro Display", FontWeight.SEMI_BOLD, 28));
        welcomeLabel.setTextFill(currentLabelColor);

        Button logoutBtn = createIOSButton("خروج", IOS_SYSTEM_RED);
        logoutBtn.setOnAction(e -> showLogoutConfirmation());

        HBox actionBox = new HBox(logoutBtn);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(actionBox, Priority.ALWAYS);

        welcomeRow.getChildren().addAll(welcomeBox, actionBox);
        headerBox.getChildren().addAll(topRow, welcomeRow);

        mainContent.setTop(headerBox);

        // ========== CENTER ==========
        centerContainer = new VBox(20);
        centerContainer.setAlignment(Pos.TOP_CENTER);
        centerContainer.setFillWidth(true);

        productsFlowPane = new FlowPane();
        productsFlowPane.setHgap(20);
        productsFlowPane.setVgap(20);
        productsFlowPane.setAlignment(Pos.CENTER);
        productsFlowPane.setPadding(new Insets(10, 0, 20, 0));
        productsFlowPane.setPrefWrapLength(1200);

        productsFlowPane.prefWrapLengthProperty().bind(
                mainContent.widthProperty().subtract(100)
        );

        centerContainer.getChildren().add(productsFlowPane);

        HBox paginationContainer = createIOSPagination();
        centerContainer.getChildren().add(paginationContainer);

        mainContent.setCenter(centerContainer);

        return mainContent;
    }

    private String getGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour >= 5 && hour < 12) return "صبح بخیر";
        if (hour >= 12 && hour < 17) return "ظهر بخیر";
        if (hour >= 17 && hour < 22) return "عصر بخیر";
        return "شب بخیر";
    }

    private HBox createIOSBrandLogo() {
        HBox brandBox = new HBox(12);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        StackPane logoContainer = new StackPane();
        logoContainer.setPrefSize(44, 44);
        logoContainer.setMaxSize(44, 44);
        logoContainer.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(44, 44, 46, 0.7);" :
                        "rgba(255, 255, 255, 0.7);") +
                        "-fx-background-radius: 22;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );

        if (logoImageView != null) {
            logoContainer.getChildren().add(logoImageView);
        } else {
            Label logo = new Label("🧃");
            logo.setFont(Font.font("SF Pro Display", 26));
            logoContainer.getChildren().add(logo);
        }

        VBox titleBox = new VBox(0);
        Label brandName = new Label("VVM");
        brandName.setFont(Font.font("SF Pro Display", FontWeight.SEMI_BOLD, 24));
        brandName.setTextFill(currentLabelColor);

        Label brandSub = new Label("vital vending machine");
        brandSub.setFont(Font.font("SF Pro Text", FontWeight.LIGHT, 10));
        brandSub.setTextFill(currentSecondaryLabelColor);

        titleBox.getChildren().addAll(brandName, brandSub);
        brandBox.getChildren().addAll(logoContainer, titleBox);

        return brandBox;
    }

    private HBox createIOSPagination() {
        paginationBox = new HBox(16);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setPadding(new Insets(15, 0, 5, 0));
        paginationBox.setVisible(false);
        paginationBox.setManaged(false);

        prevPageBtn = createIOSButton("قبلی", IOS_SYSTEM_BLUE);
        prevPageBtn.setOnAction(e -> navigateToPage(currentPage - 1));

        nextPageBtn = createIOSButton("بعدی", IOS_SYSTEM_BLUE);
        nextPageBtn.setOnAction(e -> navigateToPage(currentPage + 1));

        pageInfoLabel = new Label();
        pageInfoLabel.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 14));
        pageInfoLabel.setTextFill(currentSecondaryLabelColor);
        pageInfoLabel.setPadding(new Insets(6, 20, 6, 20));
        pageInfoLabel.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(44, 44, 46, 0.5);" :
                        "rgba(255, 255, 255, 0.5);") +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);"
        );

        paginationBox.getChildren().addAll(prevPageBtn, pageInfoLabel, nextPageBtn);
        return paginationBox;
    }

    private Button createIOSButton(String text, Color color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 13));
        btn.setTextFill(Color.WHITE);
        btn.setPadding(new Insets(8, 20, 8, 20));
        btn.setStyle(
                "-fx-background-color: " + toRgbString(color) + ";" +
                        "-fx-background-radius: 25;" +
                        "-fx-effect: dropshadow(gaussian, " + toRgbaString(color, 0.3) + ", 8, 0, 0, 2);" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), btn);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
            btn.setEffect(new DropShadow(15, color));
        });

        btn.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), btn);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
            btn.setEffect(new DropShadow(8, color.darker()));
        });

        btn.setOnMousePressed(e -> {
            ScaleTransition press = new ScaleTransition(Duration.millis(50), btn);
            press.setToX(0.95);
            press.setToY(0.95);
            press.play();
        });

        btn.setOnMouseReleased(e -> {
            ScaleTransition release = new ScaleTransition(Duration.millis(50), btn);
            release.setToX(1.0);
            release.setToY(1.0);
            release.play();
        });

        return btn;
    }

    private void loadUserButtons() {
        VBox loadingContainer = new VBox(16);
        loadingContainer.setAlignment(Pos.CENTER);
        loadingContainer.setPadding(new Insets(40));

        ProgressIndicator loading = new ProgressIndicator();
        loading.setStyle("-fx-progress-color: " + toRgbString(IOS_SYSTEM_BLUE) + ";");
        loading.setMaxSize(40, 40);
        loadingContainer.getChildren().add(loading);

        productsFlowPane.getChildren().clear();
        productsFlowPane.getChildren().add(loadingContainer);

        scheduler.submit(() -> {
            List<ButtonModel> buttons = api.getUserButtons(currentUser.getId());
            Platform.runLater(() -> {
                userButtons.clear();
                if (buttons != null && !buttons.isEmpty()) {
                    userButtons.addAll(buttons);
                }
                updateProductsGrid();
            });
        });
    }

    private void updateProductsGrid() {
        productsFlowPane.getChildren().clear();

        if (userButtons.isEmpty()) {
            VBox emptyBox = createIOSEmptyState();
            productsFlowPane.getChildren().add(emptyBox);
            return;
        }

        totalPages = (int) Math.ceil((double) userButtons.size() / itemsPerPage);
        currentPage = Math.min(currentPage, totalPages - 1);
        if (currentPage < 0) currentPage = 0;

        if (totalPages > 1) {
            paginationBox.setVisible(true);
            paginationBox.setManaged(true);
            pageInfoLabel.setText((currentPage + 1) + " از " + totalPages);
            prevPageBtn.setDisable(currentPage == 0);
            nextPageBtn.setDisable(currentPage == totalPages - 1);
            prevPageBtn.setOpacity(currentPage == 0 ? 0.5 : 1);
            nextPageBtn.setOpacity(currentPage == totalPages - 1 ? 0.5 : 1);
        } else {
            paginationBox.setVisible(false);
            paginationBox.setManaged(false);
        }

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, userButtons.size());

        for (int i = startIndex; i < endIndex; i++) {
            ButtonModel button = userButtons.get(i);
            VBox productCard = createIOSProductCard(button);
            productsFlowPane.getChildren().add(productCard);
        }
    }

    private VBox createIOSProductCard(ButtonModel button) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16, 10, 14, 10));
        card.setPrefWidth(200);
        card.setPrefHeight(250);
        card.setMaxWidth(200);
        card.setMaxHeight(250);

        String glassColor = isDarkMode ?
                "rgba(44, 44, 46, 0.7)" :
                "rgba(255, 255, 255, 0.8)";

        card.setStyle(
                "-fx-background-color: " + glassColor + ";" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);" +
                        "-fx-border-color: " + (isDarkMode ? "rgba(255,255,255,0.1)" : "rgba(0,0,0,0.05)") + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 16;"
        );

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(90, 90);
        imageContainer.setMaxSize(90, 90);
        imageContainer.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(255,255,255,0.08);" :
                        "rgba(0,0,0,0.03);") +
                        "-fx-background-radius: 45;" +
                        "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);"
        );

        try {
            String imageUrl = "https://menschwoodworks.ir/API/uploads/buttons/" + button.getImage();
            if (button.getImage() != null && !button.getImage().isEmpty()) {
                Image productImage = new Image(imageUrl, 90, 90, true, true, true);
                ImageView imageView = new ImageView(productImage);
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);

                Circle clip = new Circle(40);
                clip.setCenterX(40);
                clip.setCenterY(40);
                imageView.setClip(clip);
                imageContainer.getChildren().add(imageView);
            } else {
                Label emoji = new Label();
                emoji.setFont(Font.font("SF Pro Display", 42));
                imageContainer.getChildren().add(emoji);
            }
        } catch (Exception e) {
            Label emoji = new Label();
            emoji.setFont(Font.font("SF Pro Display", 42));
            imageContainer.getChildren().add(emoji);
        }

        Circle stockIndicator = new Circle(6);
        stockIndicator.setFill(button.getStock() > 0 ? IOS_SYSTEM_GREEN : IOS_SYSTEM_RED);
        stockIndicator.setStroke(Color.WHITE);
        stockIndicator.setStrokeWidth(2);
        stockIndicator.setEffect(new DropShadow(5, button.getStock() > 0 ? IOS_SYSTEM_GREEN : IOS_SYSTEM_RED));
        StackPane.setAlignment(stockIndicator, Pos.TOP_RIGHT);
        StackPane.setMargin(stockIndicator, new Insets(8, 8, 0, 0));
        imageContainer.getChildren().add(stockIndicator);

        Label titleLabel = new Label(button.getTitle());
        titleLabel.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 15));
        titleLabel.setTextFill(currentLabelColor);
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setMaxWidth(180);
        titleLabel.setMaxHeight(40);
        titleLabel.setAlignment(Pos.CENTER);

        Label priceLabel = new Label(button.getPriceFormatted());
        priceLabel.setFont(Font.font("SF Pro Display", FontWeight.SEMI_BOLD, 18));
        priceLabel.setTextFill(IOS_SYSTEM_ORANGE);
        priceLabel.setEffect(new DropShadow(5, IOS_SYSTEM_ORANGE.darker()));

        Label stockLabel = new Label(button.getStock() > 0 ? "موجود" : "ناموجود");
        stockLabel.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 11));
        stockLabel.setTextFill(button.getStock() > 0 ? IOS_SYSTEM_GREEN : IOS_SYSTEM_RED);
        stockLabel.setPadding(new Insets(4, 12, 4, 12));
        stockLabel.setStyle(
                "-fx-background-color: " + (button.getStock() > 0 ?
                        (isDarkMode ? "rgba(48, 209, 88, 0.2);" : "rgba(52, 199, 89, 0.15);") :
                        (isDarkMode ? "rgba(255, 69, 58, 0.2);" : "rgba(255, 59, 48, 0.15);")) +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
        );

        card.getChildren().addAll(imageContainer, titleLabel, priceLabel, stockLabel);

        card.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), card);
            scale.setToX(1.03);
            scale.setToY(1.03);
            scale.play();

            card.setStyle(
                    "-fx-background-color: " + glassColor + ";" +
                            "-fx-background-radius: 16;" +
                            "-fx-effect: dropshadow(gaussian, " + toRgbaString(IOS_SYSTEM_BLUE, 0.3) + ", 15, 0, 0, 4);" +
                            "-fx-border-color: " + toRgbString(IOS_SYSTEM_BLUE) + ";" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 16;"
            );
        });

        card.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();

            card.setStyle(
                    "-fx-background-color: " + glassColor + ";" +
                            "-fx-background-radius: 16;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);" +
                            "-fx-border-color: " + (isDarkMode ? "rgba(255,255,255,0.1)" : "rgba(0,0,0,0.05)") + ";" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 16;"
            );
        });

        // در متد createIOSProductCard، بخش کلیک:
        card.setOnMouseClicked(e -> {
            if (button.getStock() > 0) {
                ScaleTransition click = new ScaleTransition(Duration.millis(80), card);
                click.setToX(0.97);
                click.setToY(0.97);
                click.setAutoReverse(true);
                click.setCycleCount(2);
                click.play();

                click.setOnFinished(ev -> {
                    ProductActionPanel actionPanel = new ProductActionPanel(
                            currentUser, button, isDarkMode,
                            currentBackgroundColor, currentLabelColor, currentSecondaryLabelColor,
                            () -> {
                                // کال‌بک بازگشت - استفاده از switchToPanel
                                SceneManager.switchToPanel(primaryStage, UserPanel.this.getRoot());
                            }
                    );
                    SceneManager.switchToPanel(primaryStage, actionPanel.getRoot());
                });
            } else {
                showToast("این محصول موجود نیست", IOS_SYSTEM_RED);
            }
        });

        return card;
    }

    private void returnToMainPanel() {
        SceneManager.switchSceneWithSlideTransition(primaryStage, this.getRoot());
    }

    private VBox createIOSEmptyState() {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(50));

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(100, 100);
        iconContainer.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(255, 69, 58, 0.15);" :
                        "rgba(255, 59, 48, 0.1);") +
                        "-fx-background-radius: 50;" +
                        "-fx-effect: dropshadow(gaussian, " + toRgbaString(IOS_SYSTEM_RED, 0.2) + ", 15, 0, 0, 5);"
        );

        Label emptyIcon = new Label("🧃");
        emptyIcon.setFont(Font.font("SF Pro Display", 50));
        iconContainer.getChildren().add(emptyIcon);

        Label emptyText = new Label("محصولی یافت نشد");
        emptyText.setFont(Font.font("SF Pro Display", FontWeight.SEMI_BOLD, 22));
        emptyText.setTextFill(currentLabelColor);

        Label emptyDesc = new Label("لطفاً با پشتیبانی تماس بگیرید");
        emptyDesc.setFont(Font.font("SF Pro Text", 14));
        emptyDesc.setTextFill(currentSecondaryLabelColor);

        emptyBox.getChildren().addAll(iconContainer, emptyText, emptyDesc);
        return emptyBox;
    }

    private void navigateToPage(int page) {
        if (page >= 0 && page < totalPages) {
            currentPage = page;

            FadeTransition fade = new FadeTransition(Duration.millis(150), productsFlowPane);
            fade.setFromValue(1);
            fade.setToValue(0.5);
            fade.setOnFinished(e -> {
                updateProductsGrid();
                FadeTransition fadeIn = new FadeTransition(Duration.millis(150), productsFlowPane);
                fadeIn.setFromValue(0.5);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fade.play();
        }
    }

    private void showLogoutConfirmation() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(32));
        content.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(44, 44, 46, 0.95);" :
                        "rgba(255, 255, 255, 0.95);") +
                        "-fx-background-radius: 28;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);"
        );
        content.setPrefWidth(360);

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(64, 64);
        iconContainer.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(255, 69, 58, 0.2);" :
                        "rgba(255, 59, 48, 0.1);") +
                        "-fx-background-radius: 32;"
        );

        Label icon = new Label("🔐");
        icon.setFont(Font.font("SF Pro Display", 32));
        iconContainer.getChildren().add(icon);

        Label title = new Label("خروج");
        title.setFont(Font.font("SF Pro Display", FontWeight.SEMI_BOLD, 22));
        title.setTextFill(currentLabelColor);

        Label message = new Label("رمز عبور خود را وارد کنید");
        message.setFont(Font.font("SF Pro Text", 14));
        message.setTextFill(currentSecondaryLabelColor);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("رمز عبور");
        passwordField.setFont(Font.font("SF Pro Text", 13));
        passwordField.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(118, 118, 128, 0.24);" :
                        "rgba(118, 118, 128, 0.12);") +
                        "-fx-background-radius: 12;" +
                        "-fx-text-fill: " + toRgbString(currentLabelColor) + ";" +
                        "-fx-padding: 10 16;" +
                        "-fx-border-color: " + toRgbString(IOS_SYSTEM_RED) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;"
        );

        Label errorLabel = new Label("");
        errorLabel.setFont(Font.font("SF Pro Text", 12));
        errorLabel.setTextFill(IOS_SYSTEM_RED);

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER);

        Button confirmBtn = createIOSButton("خروج", IOS_SYSTEM_RED);
        Button cancelBtn = createIOSButton("انصراف", currentSecondaryLabelColor);
        cancelBtn.setOnAction(e -> dialog.close());

        confirmBtn.setOnAction(e -> {
            String password = passwordField.getText();
            if (password.isEmpty()) {
                errorLabel.setText("رمز عبور را وارد کنید");
                return;
            }

            confirmBtn.setDisable(true);
            confirmBtn.setText("...");

            new Thread(() -> {
                User verifiedUser = api.loginUser(currentUser.getUsername(), password);
                Platform.runLater(() -> {
                    confirmBtn.setDisable(false);
                    confirmBtn.setText("خروج");

                    if (verifiedUser != null) {
                        api.logUserLogout(currentUser.getId(), currentUser.getUsername(), currentUser.getFullname());
                        dialog.close();

                        LoginPage loginPage = new LoginPage();
                        SceneManager.switchSceneWithFadeTransition(
                                Main.getInstance().getPrimaryStage(),
                                loginPage.getPage()
                        );

                        showToast("خروج موفق", IOS_SYSTEM_GREEN);
                    } else {
                        errorLabel.setText("رمز عبور نادرست است");
                    }
                });
            }).start();
        });

        buttons.getChildren().addAll(confirmBtn, cancelBtn);
        content.getChildren().addAll(iconContainer, title, message, passwordField, errorLabel, buttons);

        Scene scene = new Scene(content);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        dialog.setOnShown(e -> {
            dialog.setX(primaryStage.getX() + (primaryStage.getWidth() - content.getPrefWidth()) / 2);
            dialog.setY(primaryStage.getY() + (primaryStage.getHeight() - content.getPrefHeight()) / 2);
        });

        dialog.showAndWait();
    }

    private void showToast(String message, Color color) {
        HBox toast = new HBox(8);
        toast.setAlignment(Pos.CENTER);
        toast.setPadding(new Insets(10, 24, 10, 24));
        toast.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(44, 44, 46, 0.9);" :
                        "rgba(255, 255, 255, 0.9);") +
                        "-fx-background-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
        );

        Label textLabel = new Label(message);
        textLabel.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 13));
        textLabel.setTextFill(color);

        toast.getChildren().add(textLabel);

        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new Insets(0, 0, 30, 0));
        root.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), toast);
        fadeOut.setDelay(Duration.seconds(2));
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> root.getChildren().remove(toast));

        fadeIn.play();
        fadeOut.play();
    }

    private void startDateTimeUpdater() {
        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> {
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm  •  EEEE d MMMM");
                    dateTimeLabel.setText(now.format(formatter));
                }),
                new KeyFrame(Duration.seconds(30))
        );
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
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
        if (idleMonitor != null) {
            idleMonitor.stop();
        }
        scheduler.shutdown();
    }

    // ✅ متد جدید برای شروع مانیتورینگ
    public void startIdleMonitoring(Scene scene) {
        if (idleMonitor != null) {
            idleMonitor.stop();
        }
        idleMonitor = new IdleMonitor(primaryStage, scene, () -> {
            Platform.runLater(() -> {
                if (!isScreenSaverActive) {
                    isScreenSaverActive = true;

                    // ✅ به IdleMonitor اطلاع بده که در حالت ScreenSaver هستیم
                    idleMonitor.setScreenSaverMode(true);

                    ScreenSaver screenSaver = new ScreenSaver(
                            primaryStage,
                            UserPanel.this.getRoot(),
                            () -> {
                                isScreenSaverActive = false;
                                idleMonitor.setScreenSaverMode(false);
                                SceneManager.switchToPanel(primaryStage, UserPanel.this.getRoot());
                            }
                    );
                    SceneManager.switchToPanel(primaryStage, screenSaver.getRoot());
                }
            });
        });
    }

    // ✅ متد برای توقف مانیتورینگ
    public void stopIdleMonitoring() {
        if (idleMonitor != null) {
            idleMonitor.stop();
            idleMonitor = null;
        }
    }
}