package View;

import api.ApiClient;
import com.example.vwm.Main;
import com.example.vwm.SceneManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class SalesLogsPage {

    private StackPane root;
    private Admin currentAdmin;
    private ApiClient api = new ApiClient();

    // UI Components
    private TableView<SalesLog> table;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> userFilter;
    private ComboBox<String> productFilter;
    private DatePicker dateFromPicker;
    private DatePicker dateToPicker;
    private Label totalSalesLabel;
    private Label totalAmountLabel;
    private Label avgOrderLabel;
    private Label todaySalesLabel;
    private ProgressIndicator loadingIndicator;

    // Data
    private ObservableList<SalesLog> allLogs = FXCollections.observableArrayList();
    private FilteredList<SalesLog> filteredLogs;
    private SortedList<SalesLog> sortedLogs;

    // iOS 16 Colors
    private static final Color IOS_SYSTEM_BLUE = Color.web("#0A84FF");
    private static final Color IOS_SYSTEM_GREEN = Color.web("#30D158");
    private static final Color IOS_SYSTEM_ORANGE = Color.web("#FF9F0A");
    private static final Color IOS_SYSTEM_RED = Color.web("#FF453A");
    private static final Color IOS_SYSTEM_PURPLE = Color.web("#BF5AF2");
    private static final Color IOS_SYSTEM_BACKGROUND_DARK = Color.web("#000000");
    private static final Color IOS_SYSTEM_BACKGROUND_LIGHT = Color.web("#F2F2F7");
    private static final Color IOS_LABEL_DARK = Color.web("#FFFFFF");
    private static final Color IOS_LABEL_LIGHT = Color.web("#1C1C1E");
    private static final Color IOS_SECONDARY_LABEL_DARK = Color.web("rgba(235, 235, 245, 0.6)");
    private static final Color IOS_SECONDARY_LABEL_LIGHT = Color.web("rgba(60, 60, 67, 0.6)");

    private boolean isDarkMode;
    private Color currentBackgroundColor;
    private Color currentLabelColor;
    private Color currentSecondaryLabelColor;
    private Random random = new Random();
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public SalesLogsPage(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;

        int hour = LocalDateTime.now().getHour();
        this.isDarkMode = hour >= 19 || hour < 7;
        updateIOSColors();

        filteredLogs = new FilteredList<>(allLogs, p -> true);
        createUI();
        setupFilters();
        loadLogs();
        startBackgroundAnimation();
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

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        StackPane layers = new StackPane();
        Pane backgroundLayer = createBackgroundLayer();
        VBox mainContent = createMainContent();

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

        for (int i = 0; i < 25; i++) {
            double size = random.nextDouble() * 80 + 40;
            Circle bubble = new Circle(size);
            bubble.setCenterX(random.nextDouble() * 1400);
            bubble.setCenterY(random.nextDouble() * 900);

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
        }

        return bg;
    }

    private void startBackgroundAnimation() {
        // انیمیشن ساده برای پس‌زمینه
        FadeTransition fade = new FadeTransition(Duration.seconds(3), root);
        fade.setFromValue(0.95);
        fade.setToValue(1.0);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
    }

    private VBox createMainContent() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);
        content.setMaxWidth(1400);
        content.setMaxHeight(850);
        content.setPrefWidth(1400);
        content.setPrefHeight(850);
        content.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(44, 44, 46, 0.9);" :
                        "rgba(255, 255, 255, 0.9);") +
                        "-fx-background-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 10);" +
                        "-fx-border-color: " + toRgbString(IOS_SYSTEM_BLUE) + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 30;"
        );

        // ===== HEADER =====
        HBox header = createHeader();
        header.setPadding(new Insets(0, 0, 10, 0));

        // ===== STATS CARDS =====
        HBox statsCards = createStatsCards();

        // ===== FILTER PANEL =====
        VBox filterPanel = createFilterPanel();

        // ===== TABLE =====
        VBox tableContainer = createTable();

        // ===== FOOTER =====
        HBox footer = createFooter();

        content.getChildren().addAll(header, statsCards, filterPanel, tableContainer, footer);
        return content;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-font-size: 20px; -fx-background-color: " + toRgbString(IOS_SYSTEM_BLUE) + ";" +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 10 15; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            AdminPanel panel = new AdminPanel(currentAdmin);
            SceneManager.switchToPanel(Main.getInstance().getPrimaryStage(), panel.getRoot());
        });

        VBox titleBox = new VBox(5);
        Label title = new Label("📊 گزارش فروش");
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 28));
        title.setTextFill(currentLabelColor);

        Label subtitle = new Label("مشاهده و تحلیل فروش محصولات");
        subtitle.setFont(Font.font("SF Pro Text", 14));
        subtitle.setTextFill(currentSecondaryLabelColor);

        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        header.getChildren().addAll(backBtn, titleBox);

        return header;
    }

    private HBox createStatsCards() {
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(0, 0, 20, 0));

        VBox totalCard = createStatCard("💰", "کل فروش", "0", IOS_SYSTEM_BLUE);
        totalSalesLabel = (Label) totalCard.getUserData();

        VBox amountCard = createStatCard("💵", "مبلغ کل", "0 تومان", IOS_SYSTEM_GREEN);
        totalAmountLabel = (Label) amountCard.getUserData();

        VBox avgCard = createStatCard("📊", "میانگین هر سفارش", "0 تومان", IOS_SYSTEM_ORANGE);
        avgOrderLabel = (Label) avgCard.getUserData();

        VBox todayCard = createStatCard("📅", "فروش امروز", "0 تومان", IOS_SYSTEM_PURPLE);
        todaySalesLabel = (Label) todayCard.getUserData();

        stats.getChildren().addAll(totalCard, amountCard, avgCard, todayCard);
        return stats;
    }

    private VBox createStatCard(String icon, String title, String value, Color color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setPrefHeight(120);
        card.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(255,255,255,0.05);" :
                        "rgba(0,0,0,0.02);") +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + toRgbString(color) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 20;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("SF Pro Display", 24));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 13));
        titleLabel.setTextFill(currentSecondaryLabelColor);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        valueLabel.setTextFill(color);

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        card.setUserData(valueLabel);
        return card;
    }

    private VBox createFilterPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(0, 0, 15, 0));

        // ردیف اول فیلترها
        HBox firstRow = new HBox(15);
        firstRow.setAlignment(Pos.CENTER_LEFT);

        // جستجو
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPrefWidth(400);

        Label searchLabel = new Label("🔍");
        searchLabel.setFont(Font.font("SF Pro Display", 18));

        searchField = new TextField();
        searchField.setPromptText("جستجو در نام کاربر، محصول...");
        searchField.setPrefWidth(350);
        searchField.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(255,255,255,0.08);" :
                        "rgba(0,0,0,0.03);") +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + toRgbString(IOS_SYSTEM_BLUE) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-text-fill: " + toRgbString(currentLabelColor) + ";" +
                        "-fx-padding: 10 15;"
        );

        searchBox.getChildren().addAll(searchLabel, searchField);

        // فیلتر وضعیت
        Label statusLabel = new Label("وضعیت:");
        statusLabel.setTextFill(currentSecondaryLabelColor);

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("همه", "COMPLETED", "PENDING", "CANCELLED");
        statusFilter.setValue("همه");
        statusFilter.setPrefWidth(120);
        statusFilter.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(255,255,255,0.08);" :
                        "rgba(0,0,0,0.03);") +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + toRgbString(IOS_SYSTEM_BLUE) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-text-fill: " + toRgbString(currentLabelColor) + ";"
        );

        firstRow.getChildren().addAll(searchBox, statusLabel, statusFilter);

        // ردیف دوم فیلترها
        HBox secondRow = new HBox(15);
        secondRow.setAlignment(Pos.CENTER_LEFT);

        // فیلتر کاربر
        Label userLabel = new Label("کاربر:");
        userLabel.setTextFill(currentSecondaryLabelColor);

        userFilter = new ComboBox<>();
        userFilter.getItems().add("همه");
        userFilter.setValue("همه");
        userFilter.setPrefWidth(150);
        userFilter.setStyle(statusFilter.getStyle());

        // فیلتر محصول
        Label productLabel = new Label("محصول:");
        productLabel.setTextFill(currentSecondaryLabelColor);

        productFilter = new ComboBox<>();
        productFilter.getItems().add("همه");
        productFilter.setValue("همه");
        productFilter.setPrefWidth(150);
        productFilter.setStyle(statusFilter.getStyle());

        // فیلتر تاریخ
        Label dateLabel = new Label("تاریخ:");
        dateLabel.setTextFill(currentSecondaryLabelColor);

        dateFromPicker = new DatePicker();
        dateFromPicker.setPromptText("از تاریخ");
        dateFromPicker.setPrefWidth(130);
        dateFromPicker.setStyle(
                "-fx-background-color: " + (isDarkMode ?
                        "rgba(255,255,255,0.08);" :
                        "rgba(0,0,0,0.03);") +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + toRgbString(IOS_SYSTEM_BLUE) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;"
        );

        Label toLabel = new Label("تا");
        toLabel.setTextFill(currentSecondaryLabelColor);

        dateToPicker = new DatePicker();
        dateToPicker.setPromptText("تا تاریخ");
        dateToPicker.setPrefWidth(130);
        dateToPicker.setStyle(dateFromPicker.getStyle());

        Button clearBtn = new Button("🧹 پاک کردن فیلترها");
        clearBtn.setStyle(
                "-fx-background-color: " + toRgbString(IOS_SYSTEM_RED) + ";" +
                        "-fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 10 20; -fx-cursor: hand;"
        );
        clearBtn.setOnAction(e -> clearFilters());

        secondRow.getChildren().addAll(userLabel, userFilter, productLabel, productFilter,
                dateLabel, dateFromPicker, toLabel, dateToPicker, clearBtn);

        panel.getChildren().addAll(firstRow, secondRow);
        return panel;
    }

    private VBox createTable() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPrefHeight(400);
        container.setMaxHeight(400);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: " + toRgbString(IOS_SYSTEM_BLUE) + ";");
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(40, 40);

        table = new TableView<>();
        table.setStyle(
                "-fx-background-color: transparent; -fx-border-color: " + toRgbString(IOS_SYSTEM_BLUE) + ";" +
                        "-fx-border-width: 1; -fx-border-radius: 15; -fx-background-radius: 15;"
        );
        table.setPrefHeight(380);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("هیچ فروشی یافت نشد"));
        table.setFixedCellSize(40);

        addColumns();

        container.getChildren().addAll(loadingIndicator, table);
        return container;
    }

    private void addColumns() {
        // ستون ردیف
        TableColumn<SalesLog, String> colRow = new TableColumn<>("#");
        colRow.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(table.getItems().indexOf(cellData.getValue()) + 1)));
        colRow.setPrefWidth(50);
        colRow.setStyle("-fx-alignment: CENTER;");

        // ستون کاربر
        TableColumn<SalesLog, String> colUser = new TableColumn<>("👤 کاربر");
        colUser.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullname() + " (" + cellData.getValue().getUsername() + ")"));
        colUser.setPrefWidth(180);
        colUser.setStyle("-fx-alignment: CENTER-LEFT;");

        // ستون محصول
        TableColumn<SalesLog, String> colProduct = new TableColumn<>("📦 محصول");
        colProduct.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getButtonTitle()));
        colProduct.setPrefWidth(150);
        colProduct.setStyle("-fx-alignment: CENTER-LEFT;");

        // ستون قیمت واحد
        TableColumn<SalesLog, String> colPrice = new TableColumn<>("💰 قیمت واحد");
        colPrice.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPricePerUnitFormatted()));
        colPrice.setPrefWidth(100);
        colPrice.setStyle("-fx-alignment: CENTER;");

        // ستون تعداد
        TableColumn<SalesLog, String> colQuantity = new TableColumn<>("📊 تعداد");
        colQuantity.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantity())));
        colQuantity.setPrefWidth(60);
        colQuantity.setStyle("-fx-alignment: CENTER;");

        // ستون قیمت کل
        TableColumn<SalesLog, String> colTotal = new TableColumn<>("💵 قیمت کل");
        colTotal.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTotalPriceFormatted()));
        colTotal.setPrefWidth(120);
        colTotal.setStyle("-fx-alignment: CENTER;");

        // ستون ویژگی‌ها
        TableColumn<SalesLog, String> colFeatures = new TableColumn<>("⚙️ ویژگی‌ها");
        colFeatures.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFeaturesFormatted()));
        colFeatures.setPrefWidth(120);
        colFeatures.setStyle("-fx-alignment: CENTER;");

        // ستون وضعیت
        TableColumn<SalesLog, String> colStatus = new TableColumn<>("✅ وضعیت");
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        colStatus.setPrefWidth(90);
        colStatus.setStyle("-fx-alignment: CENTER;");
        colStatus.setCellFactory(column -> new TableCell<SalesLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "COMPLETED":
                            setTextFill(IOS_SYSTEM_GREEN);
                            setStyle("-fx-font-weight: bold;");
                            break;
                        case "PENDING":
                            setTextFill(IOS_SYSTEM_ORANGE);
                            break;
                        case "CANCELLED":
                            setTextFill(IOS_SYSTEM_RED);
                            break;
                    }
                }
            }
        });

        // ستون روش پرداخت
        TableColumn<SalesLog, String> colPayment = new TableColumn<>("💳 پرداخت");
        colPayment.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentMethod()));
        colPayment.setPrefWidth(80);
        colPayment.setStyle("-fx-alignment: CENTER;");

        // ستون زمان
        TableColumn<SalesLog, String> colTime = new TableColumn<>("🕒 زمان");
        colTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCreatedAt()));
        colTime.setPrefWidth(160);
        colTime.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(colRow, colUser, colProduct, colPrice, colQuantity,
                colTotal, colFeatures, colStatus, colPayment, colTime);
    }

    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15, 0, 0, 0));

        Button refreshBtn = createActionButton("🔄 بروزرسانی", IOS_SYSTEM_BLUE);
        refreshBtn.setOnAction(e -> loadLogs());

        Button exportBtn = createActionButton("📥 خروجی Excel", IOS_SYSTEM_GREEN);
        exportBtn.setOnAction(e -> exportToExcel());

        Button backBtn = createActionButton("🏠 بازگشت", IOS_SYSTEM_PURPLE);
        backBtn.setOnAction(e -> {
            AdminPanel panel = new AdminPanel(currentAdmin);
            SceneManager.switchToPanel(Main.getInstance().getPrimaryStage(), panel.getRoot());
        });

        footer.getChildren().addAll(refreshBtn, exportBtn, backBtn);
        return footer;
    }

    private Button createActionButton(String text, Color color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("SF Pro Text", FontWeight.MEDIUM, 14));
        btn.setTextFill(Color.WHITE);
        btn.setPadding(new Insets(10, 25, 10, 25));
        btn.setStyle(
                "-fx-background-color: " + toRgbString(color) + ";" +
                        "-fx-background-radius: 25;" +
                        "-fx-effect: dropshadow(gaussian, " + toRgbaString(color, 0.3) + ", 10, 0, 0, 2);" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), btn);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        btn.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), btn);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return btn;
    }

    private void setupFilters() {
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        userFilter.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        productFilter.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        dateFromPicker.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        dateToPicker.valueProperty().addListener((obs, old, newVal) -> applyFilters());
    }

    private void clearFilters() {
        searchField.clear();
        statusFilter.setValue("همه");
        userFilter.setValue("همه");
        productFilter.setValue("همه");
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        applyFilters();
    }

    private void applyFilters() {
        filteredLogs.setPredicate(log -> {
            // Search filter
            String search = searchField.getText().toLowerCase();
            if (!search.isEmpty()) {
                boolean matches =
                        (log.getUsername() != null && log.getUsername().toLowerCase().contains(search)) ||
                                (log.getFullname() != null && log.getFullname().toLowerCase().contains(search)) ||
                                (log.getButtonTitle() != null && log.getButtonTitle().toLowerCase().contains(search));
                if (!matches) return false;
            }

            // Status filter
            String selectedStatus = statusFilter.getValue();
            if (selectedStatus != null && !selectedStatus.equals("همه") && !selectedStatus.equals("all")) {
                if (!log.getStatus().equals(selectedStatus)) return false;
            }

            // User filter
            String selectedUser = userFilter.getValue();
            if (selectedUser != null && !selectedUser.equals("همه") && !selectedUser.equals("all")) {
                if (!log.getUsername().equals(selectedUser)) return false;
            }

            // Product filter
            String selectedProduct = productFilter.getValue();
            if (selectedProduct != null && !selectedProduct.equals("همه") && !selectedProduct.equals("all")) {
                if (!log.getButtonTitle().equals(selectedProduct)) return false;
            }

            // Date filter (simplified)
            if (dateFromPicker.getValue() != null && log.getCreatedAt() != null) {
                String logDate = log.getCreatedAt().split(" ")[0];
                if (logDate.compareTo(dateFromPicker.getValue().toString()) < 0) return false;
            }
            if (dateToPicker.getValue() != null && log.getCreatedAt() != null) {
                String logDate = log.getCreatedAt().split(" ")[0];
                if (logDate.compareTo(dateToPicker.getValue().toString()) > 0) return false;
            }

            return true;
        });
        updateStats();
    }

    private void loadLogs() {
        loadingIndicator.setVisible(true);
        table.setVisible(false);
        allLogs.clear();

        scheduler.submit(() -> {
            try {
                System.out.println("=== Loading Sales Logs ===");
                List<ApiClient.SalesLog> logs = api.getSalesLogs(0, null, 0, null, null, null, 1000);
                System.out.println("API returned " + (logs != null ? logs.size() : 0) + " logs");

                Platform.runLater(() -> {
                    if (logs != null && !logs.isEmpty()) {
                        for (ApiClient.SalesLog log : logs) {
                            allLogs.add(new SalesLog(log));
                        }
                        System.out.println("Added " + allLogs.size() + " logs to table");
                    } else {
                        System.out.println("No logs to display");
                    }

                    updateFilters();
                    sortedLogs = new SortedList<>(filteredLogs);
                    sortedLogs.comparatorProperty().bind(table.comparatorProperty());
                    table.setItems(sortedLogs);

                    updateStats();

                    loadingIndicator.setVisible(false);
                    table.setVisible(true);
                    table.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    table.setVisible(true);
                });
            }
        });
    }

    private void updateFilters() {
        String currentUser = userFilter.getValue();
        String currentProduct = productFilter.getValue();

        userFilter.getItems().clear();
        userFilter.getItems().add("همه");

        productFilter.getItems().clear();
        productFilter.getItems().add("همه");

        List<String> usernames = allLogs.stream()
                .map(SalesLog::getUsername)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        userFilter.getItems().addAll(usernames);

        List<String> products = allLogs.stream()
                .map(SalesLog::getButtonTitle)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        productFilter.getItems().addAll(products);

        if (currentUser != null && userFilter.getItems().contains(currentUser)) {
            userFilter.setValue(currentUser);
        } else {
            userFilter.setValue("همه");
        }

        if (currentProduct != null && productFilter.getItems().contains(currentProduct)) {
            productFilter.setValue(currentProduct);
        } else {
            productFilter.setValue("همه");
        }
    }

    private void updateStats() {
        int total = allLogs.size();
        int filtered = filteredLogs.size();

        double totalAmount = filteredLogs.stream()
                .mapToDouble(SalesLog::getTotalPrice)
                .sum();

        double avgOrder = filtered > 0 ? totalAmount / filtered : 0;

        LocalDateTime now = LocalDateTime.now();
        String today = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        double todayAmount = filteredLogs.stream()
                .filter(log -> log.getCreatedAt().startsWith(today))
                .mapToDouble(SalesLog::getTotalPrice)
                .sum();

        totalSalesLabel.setText(String.valueOf(filtered));
        totalAmountLabel.setText(String.format("%,d", (int) totalAmount) + " تومان");
        avgOrderLabel.setText(String.format("%,d", (int) avgOrder) + " تومان");
        todaySalesLabel.setText(String.format("%,d", (int) todayAmount) + " تومان");
    }

    private void exportToExcel() {
        if (allLogs.isEmpty()) {
            showAlert("خطا", "داده‌ای برای خروجی وجود ندارد", Alert.AlertType.WARNING);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("ذخیره فایل");
        chooser.setInitialFileName("sales_logs_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        File file = chooser.showSaveDialog(null);
        if (file == null) return;

        try (java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8")) {
            writer.write('\ufeff');
            writer.println("شناسه,کاربر,نام کامل,محصول,تعداد,قیمت واحد,قیمت کل,شیرینی,کافئین,دما,وضعیت,پرداخت,زمان");

            for (SalesLog log : filteredLogs) {
                writer.printf("%d,%s,%s,%s,%d,%.0f,%.0f,%d,%d,%d,%s,%s,%s\n",
                        log.getId(),
                        escapeCsv(log.getUsername()),
                        escapeCsv(log.getFullname()),
                        escapeCsv(log.getButtonTitle()),
                        log.getQuantity(),
                        log.getPricePerUnit(),
                        log.getTotalPrice(),
                        log.getSweetnessLevel(),
                        log.getCaffeineLevel(),
                        log.getTemperatureLevel(),
                        log.getStatus(),
                        log.getPaymentMethod(),
                        log.getCreatedAt()
                );
            }

            showAlert("موفقیت", "فایل با موفقیت ذخیره شد:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("خطا", "خطا در ذخیره فایل: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        scheduler.shutdown();
    }

    // کلاس داخلی برای نمایش لاگ
    // کلاس داخلی برای نمایش لاگ - با متدهای فرمت‌کننده کامل
    public static class SalesLog {
        private final SimpleStringProperty id;
        private final SimpleStringProperty userId;
        private final SimpleStringProperty username;
        private final SimpleStringProperty fullname;
        private final SimpleStringProperty buttonTitle;
        private final SimpleStringProperty buttonImage;
        private final SimpleStringProperty quantity;
        private final SimpleStringProperty pricePerUnit;
        private final SimpleStringProperty totalPrice;
        private final SimpleStringProperty sweetnessLevel;
        private final SimpleStringProperty caffeineLevel;
        private final SimpleStringProperty temperatureLevel;
        private final SimpleStringProperty paymentMethod;
        private final SimpleStringProperty status;
        private final SimpleStringProperty createdAt;

        public SalesLog(ApiClient.SalesLog log) {
            this.id = new SimpleStringProperty(String.valueOf(log.getId()));
            this.userId = new SimpleStringProperty(String.valueOf(log.getUserId()));
            this.username = new SimpleStringProperty(log.getUsername() != null ? log.getUsername() : "");
            this.fullname = new SimpleStringProperty(log.getFullname() != null ? log.getFullname() : "");
            this.buttonTitle = new SimpleStringProperty(log.getButtonTitle() != null ? log.getButtonTitle() : "");
            this.buttonImage = new SimpleStringProperty(log.getButtonImage() != null ? log.getButtonImage() : "");
            this.quantity = new SimpleStringProperty(String.valueOf(log.getQuantity()));
            this.pricePerUnit = new SimpleStringProperty(String.valueOf((int)log.getPricePerUnit()));
            this.totalPrice = new SimpleStringProperty(String.valueOf((int)log.getTotalPrice()));
            this.sweetnessLevel = new SimpleStringProperty(String.valueOf(log.getSweetnessLevel()));
            this.caffeineLevel = new SimpleStringProperty(String.valueOf(log.getCaffeineLevel()));
            this.temperatureLevel = new SimpleStringProperty(String.valueOf(log.getTemperatureLevel()));
            this.paymentMethod = new SimpleStringProperty(log.getPaymentMethod() != null ? log.getPaymentMethod() : "");
            this.status = new SimpleStringProperty(log.getStatus() != null ? log.getStatus() : "");
            this.createdAt = new SimpleStringProperty(log.getCreatedAt() != null ? log.getCreatedAt() : "");
        }

        // Getters
        public int getId() { return Integer.parseInt(id.get()); }
        public int getUserId() { return Integer.parseInt(userId.get()); }
        public String getUsername() { return username.get(); }
        public String getFullname() { return fullname.get(); }
        public String getButtonTitle() { return buttonTitle.get(); }
        public String getButtonImage() { return buttonImage.get(); }
        public int getQuantity() { return Integer.parseInt(quantity.get()); }
        public double getPricePerUnit() {
            try {
                return Double.parseDouble(pricePerUnit.get());
            } catch (Exception e) {
                return 0;
            }
        }
        public double getTotalPrice() {
            try {
                return Double.parseDouble(totalPrice.get());
            } catch (Exception e) {
                return 0;
            }
        }
        public int getSweetnessLevel() { return Integer.parseInt(sweetnessLevel.get()); }
        public int getCaffeineLevel() { return Integer.parseInt(caffeineLevel.get()); }
        public int getTemperatureLevel() { return Integer.parseInt(temperatureLevel.get()); }
        public String getPaymentMethod() { return paymentMethod.get(); }
        public String getStatus() { return status.get(); }
        public String getCreatedAt() { return createdAt.get(); }

        // متدهای فرمت‌کننده برای نمایش
        public String getPricePerUnitFormatted() {
            try {
                double price = Double.parseDouble(pricePerUnit.get());
                return String.format("%,d", (int) price) + " تومان";
            } catch (Exception e) {
                return "0 تومان";
            }
        }

        public String getTotalPriceFormatted() {
            try {
                double price = Double.parseDouble(totalPrice.get());
                return String.format("%,d", (int) price) + " تومان";
            } catch (Exception e) {
                return "0 تومان";
            }
        }

        public String getFeaturesFormatted() {
            StringBuilder features = new StringBuilder();
            int sweetness = Integer.parseInt(sweetnessLevel.get());
            int caffeine = Integer.parseInt(caffeineLevel.get());
            int temp = Integer.parseInt(temperatureLevel.get());

            if (sweetness > 0) features.append("🍬").append(sweetness).append(" ");
            if (caffeine > 0) features.append("☕").append(caffeine).append(" ");
            if (temp > 0) features.append("🌡️").append(temp).append("°C");

            return features.toString().trim();
        }
    }
}