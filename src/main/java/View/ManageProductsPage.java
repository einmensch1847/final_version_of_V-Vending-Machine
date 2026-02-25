package View;

import api.ApiClient;
import api.Logger;
import com.example.vwm.Main;
import com.example.vwm.SceneManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ManageProductsPage {

    private StackPane root;
    private Admin currentAdmin;
    private ApiClient api = new ApiClient();
    private Stage primaryStage;

    // UI Components
    private TableView<ButtonModel> buttonsTable;
    private TextField searchField;
    private ComboBox<String> filterCombo;
    private Label totalButtonsLabel, activeButtonsLabel, defaultButtonsLabel, maxPriceLabel;
    private Button addBtn, editBtn, deleteBtn, refreshBtn, backBtn;
    private ProgressIndicator loadingIndicator;
    private Pane animatedBg;

    // Data
    private ObservableList<ButtonModel> buttonsList = FXCollections.observableArrayList();
    private FilteredList<ButtonModel> filteredData;
    private SortedList<ButtonModel> sortedData;

    // لیست کاربران برای اختصاص دکمه
    private List<ManageUsersPage.UserModel> usersList;
    private ComboBox<ManageUsersPage.UserModel> userCombo;

    // Colors
    private static final Color PRIMARY_COLOR = Color.web("#4f46e5");
    private static final Color SECONDARY_COLOR = Color.web("#7c3aed");
    private static final Color ACCENT_COLOR = Color.web("#10b981");
    private static final Color WARNING_COLOR = Color.web("#f59e0b");
    private static final Color ERROR_COLOR = Color.web("#ef4444");
    private static final Color SUCCESS_COLOR = Color.web("#22c55e");
    private static final Color CARD_BG = Color.web("#1e293b");
    private static final Color TEXT_PRIMARY = Color.web("#f8fafc");
    private static final Color TEXT_SECONDARY = Color.web("#cbd5e1");
    private static final Color BORDER_COLOR = Color.web("#334155");

    // Status Colors
    private static final Color HAS_SWEETNESS_COLOR = Color.web("#f59e0b");
    private static final Color HAS_CAFFEINE_COLOR = Color.web("#10b981");
    private static final Color HAS_TEMPERATURE_COLOR = Color.web("#3b82f6");

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private FileChooser fileChooser;

    // مدل داده دکمه
    public static class ButtonModel {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty title;
        private final SimpleStringProperty caption;
        private final SimpleStringProperty image;
        private final SimpleDoubleProperty price;
        private final SimpleIntegerProperty sweetnessLevel;
        private final SimpleIntegerProperty caffeineLevel;
        private final SimpleIntegerProperty temperatureLevel;
        private final SimpleIntegerProperty stock;
        private final SimpleIntegerProperty userId;
        private final SimpleStringProperty userName;

        public ButtonModel(int id, String title, String caption, String image, double price,
                           int sweetnessLevel, int caffeineLevel, int temperatureLevel,
                           int stock, int userId, String userName) {
            this.id = new SimpleIntegerProperty(id);
            this.title = new SimpleStringProperty(title != null ? title : "");
            this.caption = new SimpleStringProperty(caption != null ? caption : "");
            this.image = new SimpleStringProperty(image != null ? image : "");
            this.price = new SimpleDoubleProperty(price);
            this.sweetnessLevel = new SimpleIntegerProperty(sweetnessLevel);
            this.caffeineLevel = new SimpleIntegerProperty(caffeineLevel);
            this.temperatureLevel = new SimpleIntegerProperty(temperatureLevel);
            this.stock = new SimpleIntegerProperty(stock);
            this.userId = new SimpleIntegerProperty(userId);
            this.userName = new SimpleStringProperty(userName != null ? userName : "پیش‌فرض");
        }

        // Getters
        public int getId() { return id.get(); }
        public String getTitle() { return title.get(); }
        public String getCaption() { return caption.get(); }
        public String getImage() { return image.get(); }
        public double getPrice() { return price.get(); }
        public int getSweetnessLevel() { return sweetnessLevel.get(); }
        public int getCaffeineLevel() { return caffeineLevel.get(); }
        public int getTemperatureLevel() { return temperatureLevel.get(); }
        public int getStock() { return stock.get(); }
        public int getUserId() { return userId.get(); }
        public String getUserName() { return userName.get(); }

        // Property Getters
        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty titleProperty() { return title; }
        public SimpleStringProperty captionProperty() { return caption; }
        public SimpleStringProperty imageProperty() { return image; }
        public SimpleDoubleProperty priceProperty() { return price; }
        public SimpleIntegerProperty sweetnessLevelProperty() { return sweetnessLevel; }
        public SimpleIntegerProperty caffeineLevelProperty() { return caffeineLevel; }
        public SimpleIntegerProperty temperatureLevelProperty() { return temperatureLevel; }
        public SimpleIntegerProperty stockProperty() { return stock; }
        public SimpleIntegerProperty userIdProperty() { return userId; }
        public SimpleStringProperty userNameProperty() { return userName; }

        // Helper methods for display
        public String getSweetnessStatus() {
            return sweetnessLevel.get() > 0 ? "✅ دارد" : "❌ ندارد";
        }

        public String getCaffeineStatus() {
            return caffeineLevel.get() > 0 ? "✅ دارد" : "❌ ندارد";
        }

        public String getTemperatureStatus() {
            return temperatureLevel.get() > 0 ? "✅ دارد (" + temperatureLevel.get() + "°C)" : "❌ ندارد";
        }

        public String getPriceFormatted() {
            return String.format("%,d", (int) price.get()) + " تومان";
        }

        public String getImagePath() {
            return image.get();
        }
    }

    public ManageProductsPage(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.primaryStage = Main.getInstance().getPrimaryStage();
        this.fileChooser = new FileChooser();
        configureFileChooser();
        createUI();
        loadButtons();
        loadUsers();
    }

    private void configureFileChooser() {
        fileChooser.setTitle("انتخاب تصویر محصول");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("تصاویر", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("همه فایل‌ها", "*.*")
        );
    }

    private void loadUsers() {
        scheduler.submit(() -> {
            usersList = api.getAllUsers();
            Platform.runLater(() -> {
                if (usersList != null && !usersList.isEmpty()) {
                    if (userCombo != null) {
                        userCombo.setItems(FXCollections.observableArrayList(usersList));
                    }
                }
            });
        });
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #0f172a;");

        // پس‌زمینه ثابت بدون انیمیشن برای جلوگیری از لرزش
        animatedBg = createStaticBackground();

        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setMaxWidth(1400);
        mainContainer.setMaxHeight(850);
        mainContainer.setPrefWidth(1400);
        mainContainer.setPrefHeight(850);

        // کادر اصلی با سایه ثابت
        StackPane contentPane = new StackPane();
        contentPane.setStyle(
                "-fx-background-color: " + toRgbString(CARD_BG) + ";" +
                        "-fx-background-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 30, 0, 0, 10);"
        );
        contentPane.setOpacity(0.95);

        VBox content = createContent();
        contentPane.getChildren().add(content);

        mainContainer.getChildren().add(contentPane);
        root.getChildren().addAll(animatedBg, mainContainer);
    }

    private Pane createStaticBackground() {
        Pane bg = new Pane();
        bg.setStyle("-fx-background-color: #0f172a;");

        // ذرات ثابت بدون انیمیشن برای جلوگیری از لرزش
        for (int i = 0; i < 30; i++) {
            Circle circle = new Circle(Math.random() * 3 + 1, Color.web("rgba(79,70,229,0.1)"));
            circle.setCenterX(Math.random() * 1400);
            circle.setCenterY(Math.random() * 850);
            bg.getChildren().add(circle);
        }
        return bg;
    }

    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);
        content.setPrefHeight(750);
        content.setMaxHeight(750);
        content.setPrefWidth(1340);
        content.setMaxWidth(1340);

        HBox header = createHeader();
        header.setPrefHeight(60);
        header.setMinHeight(60);

        HBox statsCards = createStatsCards();
        statsCards.setPrefHeight(120);
        statsCards.setMinHeight(120);
        statsCards.setMaxHeight(120);

        HBox searchBar = createSearchBar();
        searchBar.setPrefHeight(70);
        searchBar.setMinHeight(70);
        searchBar.setMaxHeight(70);

        VBox tableContainer = createButtonsTable();
        tableContainer.setPrefHeight(350);
        tableContainer.setMaxHeight(350);
        tableContainer.setMinHeight(350);

        HBox actionButtons = createActionButtons();
        actionButtons.setPrefHeight(60);
        actionButtons.setMinHeight(60);
        actionButtons.setMaxHeight(60);

        content.getChildren().addAll(header, statsCards, searchBar, tableContainer, actionButtons);

        VBox.setVgrow(tableContainer, Priority.NEVER);
        content.setFillWidth(true);

        return content;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-font-size: 20px; -fx-background-color: " + toRgbString(PRIMARY_COLOR) + ";" +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 10 15; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            AdminPanel panel = new AdminPanel(currentAdmin);
            SceneManager.switchSceneWithFadeTransition(primaryStage, panel.getRoot());
        });

        VBox titleBox = new VBox(5);
        Label title = new Label("مدیریت محصولات و دکمه‌ها");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 26));
        title.setTextFill(TEXT_PRIMARY);

        Label subtitle = new Label("افزودن، ویرایش و مدیریت دکمه‌های دستگاه وندینگ");
        subtitle.setFont(Font.font("Tahoma", 13));
        subtitle.setTextFill(TEXT_SECONDARY);

        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        header.getChildren().addAll(backBtn, titleBox);

        return header;
    }

    private HBox createStatsCards() {
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(0, 0, 10, 0));

        VBox totalCard = createStatCard("🖱️", "کل دکمه‌ها", "0", PRIMARY_COLOR);
        totalButtonsLabel = (Label) totalCard.getUserData();

        VBox activeCard = createStatCard("✅", "موجود در انبار", "0", SUCCESS_COLOR);
        activeButtonsLabel = (Label) activeCard.getUserData();

        VBox defaultCard = createStatCard("⚡", "دکمه‌های پیش‌فرض", "0", SECONDARY_COLOR);
        defaultButtonsLabel = (Label) defaultCard.getUserData();

        VBox priceCard = createStatCard("💰", "گرانترین", "0", ACCENT_COLOR);
        maxPriceLabel = (Label) priceCard.getUserData();

        stats.getChildren().addAll(totalCard, activeCard, defaultCard, priceCard);

        return stats;
    }

    private VBox createStatCard(String icon, String title, String value, Color color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setPrefHeight(120);
        card.setMaxWidth(200);
        card.setMaxHeight(120);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 15; " +
                "-fx-border-color: " + toRgbString(color) + "; -fx-border-width: 1; -fx-border-radius: 15;");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
        titleLabel.setTextFill(TEXT_SECONDARY);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 22));
        valueLabel.setTextFill(color);

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        card.setUserData(valueLabel);

        return card;
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox(15);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(10, 0, 10, 0));

        searchField = new TextField();
        searchField.setPromptText("🔍 جستجو بر اساس نام محصول، توضیحات، قیمت...");
        searchField.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 25; " +
                "-fx-border-color: #334155; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 25; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 12 20; " +
                "-fx-font-size: 14px;");
        searchField.setPrefHeight(45);
        searchField.setPrefWidth(450);
        searchField.setMinWidth(450);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterButtons());

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll(
                "همه دکمه‌ها",
                "دکمه‌های پیش‌فرض",
                "دکمه‌های کاربران",
                "دارای شیرینی",
                "دارای کافئین",
                "دارای دما",
                "کمتر از ۱۰۰۰۰",
                "بین ۱۰ تا ۵۰ هزار",
                "بیشتر از ۵۰ هزار"
        );
        filterCombo.setValue("همه دکمه‌ها");
        filterCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 25; " +
                "-fx-border-color: #334155; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 25; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 8 15; " +
                "-fx-font-size: 13px;");
        filterCombo.setPrefHeight(45);
        filterCombo.setPrefWidth(180);
        filterCombo.setOnAction(e -> filterButtons());

        refreshBtn = new Button("🔄 بروزرسانی");
        refreshBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(SECONDARY_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12 25; -fx-cursor: hand;");
        refreshBtn.setPrefHeight(45);
        refreshBtn.setOnAction(e -> {
            loadButtons();
            loadUsers();
        });

        searchBar.getChildren().addAll(searchField, filterCombo, refreshBtn);
        return searchBar;
    }

    private VBox createButtonsTable() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPrefHeight(350);
        container.setMaxHeight(350);
        container.setMinHeight(350);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4f46e5;");
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(40, 40);

        buttonsTable = new TableView<>();
        buttonsTable.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        buttonsTable.setPrefHeight(330);
        buttonsTable.setMinHeight(330);
        buttonsTable.setMaxHeight(330);
        buttonsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        buttonsTable.setPlaceholder(new Label("هیچ دکمه‌ای یافت نشد"));
        buttonsTable.setFixedCellSize(40);

        // ستون تصویر
        TableColumn<ButtonModel, Void> imageCol = new TableColumn<>("تصویر");
        imageCol.setPrefWidth(80);
        imageCol.setStyle("-fx-alignment: CENTER;");
        imageCol.setCellFactory(col -> new TableCell<ButtonModel, Void>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ButtonModel button = getTableView().getItems().get(getIndex());
                    String imagePath = button.getImage();
                    if (imagePath != null && !imagePath.isEmpty()) {
                        String imageUrl = "https://menschwoodworks.ir/API/uploads/buttons/" + imagePath;
                        Image image = new Image(imageUrl, true);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } else {
                        Label noImage = new Label("📷");
                        noImage.setStyle("-fx-font-size: 24px;");
                        setGraphic(noImage);
                    }
                }
            }
        });

        // ستون ID
        TableColumn<ButtonModel, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;-fx-text-fill: #ffff;-fx-text-alignment: #ffff");

        // ستون عنوان
        TableColumn<ButtonModel, String> titleCol = new TableColumn<>("عنوان محصول");
        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        titleCol.setPrefWidth(130);
        titleCol.setStyle("-fx-alignment: CENTER-LEFT;-fx-text-fill: #ffff;-fx-text-alignment: #ffff");

        // ستون قیمت
        TableColumn<ButtonModel, Number> priceCol = new TableColumn<>("قیمت");
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        priceCol.setPrefWidth(90);
        priceCol.setStyle("-fx-alignment: CENTER;-fx-text-fill: #ffff;-fx-text-alignment: #ffff");
        priceCol.setCellFactory(col -> new TableCell<ButtonModel, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d", item.intValue()) + " تومان");
                    setTextFill(TEXT_PRIMARY);
                }
            }
        });

        // ستون شیرینی
        TableColumn<ButtonModel, Number> sweetnessCol = new TableColumn<>("شیرینی");
        sweetnessCol.setCellValueFactory(cellData -> cellData.getValue().sweetnessLevelProperty());
        sweetnessCol.setPrefWidth(60);
        sweetnessCol.setStyle("-fx-alignment: CENTER;");
        sweetnessCol.setCellFactory(col -> new TableCell<ButtonModel, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    boolean hasSweetness = item.intValue() > 0;
                    setText(hasSweetness ? "✅" : "❌");
                    setTextFill(hasSweetness ? HAS_SWEETNESS_COLOR : TEXT_SECONDARY);
                    setTooltip(new Tooltip(hasSweetness ? "شیرینی: " + item.intValue() : "فاقد شیرینی"));
                }
            }
        });

        // ستون کافئین
        TableColumn<ButtonModel, Number> caffeineCol = new TableColumn<>("کافئین");
        caffeineCol.setCellValueFactory(cellData -> cellData.getValue().caffeineLevelProperty());
        caffeineCol.setPrefWidth(60);
        caffeineCol.setStyle("-fx-alignment: CENTER;");
        caffeineCol.setCellFactory(col -> new TableCell<ButtonModel, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    boolean hasCaffeine = item.intValue() > 0;
                    setText(hasCaffeine ? "✅" : "❌");
                    setTextFill(hasCaffeine ? HAS_CAFFEINE_COLOR : TEXT_SECONDARY);
                    setTooltip(new Tooltip(hasCaffeine ? "کافئین: " + item.intValue() : "فاقد کافئین"));
                }
            }
        });

        // ستون دما
        TableColumn<ButtonModel, Number> tempCol = new TableColumn<>("دما");
        tempCol.setCellValueFactory(cellData -> cellData.getValue().temperatureLevelProperty());
        tempCol.setPrefWidth(70);
        tempCol.setStyle("-fx-alignment: CENTER;");
        tempCol.setCellFactory(col -> new TableCell<ButtonModel, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    int temp = item.intValue();
                    if (temp > 0) {
                        setText(temp + "°C");
                        setTextFill(HAS_TEMPERATURE_COLOR);
                        setTooltip(new Tooltip("دمای سرو: " + temp + " درجه"));
                    } else {
                        setText("❌");
                        setTextFill(TEXT_SECONDARY);
                        setTooltip(new Tooltip("فاقد دمای مشخص"));
                    }
                }
            }
        });

        // ستون موجودی
        TableColumn<ButtonModel, Number> stockCol = new TableColumn<>("موجودی");
        stockCol.setCellValueFactory(cellData -> cellData.getValue().stockProperty());
        stockCol.setPrefWidth(70);
        stockCol.setStyle("-fx-alignment: CENTER;");
        stockCol.setCellFactory(col -> new TableCell<ButtonModel, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    int stock = item.intValue();
                    setText(String.valueOf(stock));
                    if (stock <= 5) {
                        setTextFill(ERROR_COLOR);
                        setTooltip(new Tooltip("موجودی کم!"));
                    } else if (stock <= 20) {
                        setTextFill(WARNING_COLOR);
                    } else {
                        setTextFill(TEXT_PRIMARY);
                    }
                }
            }
        });

        // ستون مالک
        TableColumn<ButtonModel, String> ownerCol = new TableColumn<>("مالک");
        ownerCol.setCellValueFactory(cellData -> cellData.getValue().userNameProperty());
        ownerCol.setPrefWidth(100);
        ownerCol.setStyle("-fx-alignment: CENTER-LEFT;-fx-text-fill: #ffff;-fx-text-alignment: #ffff");

        // ستون عملیات
        TableColumn<ButtonModel, Void> actionCol = new TableColumn<>("عملیات");
        actionCol.setPrefWidth(100);
        actionCol.setSortable(false);
        actionCol.setCellFactory(col -> {
            TableCell<ButtonModel, Void> cell = new TableCell<>() {
                private final HBox buttons = new HBox(5);
                private final Button editBtn = new Button("✏️");
                private final Button deleteBtn = new Button("🗑️");

                {
                    buttons.setAlignment(Pos.CENTER);

                    editBtn.setStyle("-fx-background-color: " + toRgbString(PRIMARY_COLOR) + "; " +
                            "-fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 8; " +
                            "-fx-cursor: hand; -fx-font-size: 11px;");
                    editBtn.setTooltip(new Tooltip("ویرایش"));
                    editBtn.setOnAction(e -> {
                        ButtonModel button = getTableView().getItems().get(getIndex());
                        showEditButtonDialog(button);
                    });

                    deleteBtn.setStyle("-fx-background-color: " + toRgbString(ERROR_COLOR) + "; " +
                            "-fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 8; " +
                            "-fx-cursor: hand; -fx-font-size: 11px;");
                    deleteBtn.setTooltip(new Tooltip("حذف"));
                    deleteBtn.setOnAction(e -> {
                        ButtonModel button = getTableView().getItems().get(getIndex());
                        showDeleteConfirmation(button);
                    });

                    buttons.getChildren().addAll(editBtn, deleteBtn);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttons);
                    }
                    setAlignment(Pos.CENTER);
                }
            };
            return cell;
        });

        buttonsTable.getColumns().addAll(imageCol, idCol, titleCol, priceCol, sweetnessCol,
                caffeineCol, tempCol, stockCol, ownerCol, actionCol);

        // استایل ردیف‌ها - بدون انیمیشن برای جلوگیری از لرزش
        buttonsTable.setRowFactory(tv -> {
            TableRow<ButtonModel> row = new TableRow<>();
            row.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            row.setPrefHeight(40);

            return row;
        });

        container.getChildren().addAll(loadingIndicator, buttonsTable);
        VBox.setVgrow(buttonsTable, Priority.NEVER);

        return container;
    }

    private HBox createActionButtons() {
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(15, 0, 0, 0));

        addBtn = new Button("➕ افزودن دکمه جدید");
        addBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(SUCCESS_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12 25; -fx-cursor: hand;");
        addBtn.setPrefHeight(45);
        addBtn.setOnAction(e -> showAddButtonDialog());

        editBtn = new Button("✏️ ویرایش انتخاب شده");
        editBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(PRIMARY_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12 25; -fx-cursor: hand;");
        editBtn.setPrefHeight(45);
        editBtn.setOnAction(e -> {
            ButtonModel selected = buttonsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditButtonDialog(selected);
            } else {
                showToast("لطفاً یک دکمه را انتخاب کنید", WARNING_COLOR);
            }
        });

        deleteBtn = new Button("🗑️ حذف انتخاب شده");
        deleteBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(ERROR_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12 25; -fx-cursor: hand;");
        deleteBtn.setPrefHeight(45);
        deleteBtn.setOnAction(e -> {
            ButtonModel selected = buttonsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showDeleteConfirmation(selected);
            } else {
                showToast("لطفاً یک دکمه را انتخاب کنید", WARNING_COLOR);
            }
        });

        actions.getChildren().addAll(addBtn, editBtn, deleteBtn);
        return actions;
    }

    private void showAddButtonDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 20; " +
                "-fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 20;");
        content.setEffect(new DropShadow(25, Color.BLACK));
        content.setPrefWidth(700);
        content.setPrefHeight(750);
        content.setMaxWidth(700);
        content.setMaxHeight(750);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setPrefWidth(720);
        scrollPane.setPrefHeight(770);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("➕");
        icon.setStyle("-fx-font-size: 30px;");

        VBox titleBox = new VBox(5);
        Label title = new Label("افزودن دکمه جدید");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 22));
        title.setTextFill(TEXT_PRIMARY);

        Label subtitle = new Label("ایجاد دکمه جدید برای محصولات دستگاه وندینگ");
        subtitle.setFont(Font.font("Tahoma", 12));
        subtitle.setTextFill(TEXT_SECONDARY);

        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(icon, titleBox);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #334155;");

        // Form
        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(15);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20, 0, 20, 0));
        form.setPrefWidth(600);

        int row = 0;

        // --- اطلاعات پایه ---
        Label basicTitle = new Label("📋 اطلاعات پایه");
        basicTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        basicTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(basicTitle, 2);
        form.add(basicTitle, 0, row++);

        // عنوان محصول
        TextField titleField = createEditField("");
        titleField.setPromptText("مثال: قهوه اسپرسو، چای سیاه، ...");
        form.add(createEditRow("📝 عنوان محصول", titleField), 0, row++, 2, 1);

        // توضیحات
        TextArea captionArea = createEditTextArea("");
        captionArea.setPromptText("توضیحات کامل محصول...");
        form.add(createEditRow("📄 توضیحات", captionArea), 0, row++, 2, 1);

        // --- قیمت و موجودی ---
        Label priceTitle = new Label("💰 قیمت و موجودی");
        priceTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        priceTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(priceTitle, 2);
        form.add(priceTitle, 0, row++);

        // قیمت
        TextField priceField = createEditField("");
        priceField.setPromptText("مثال: 15000");
        form.add(createEditRow("💵 قیمت (تومان)", priceField), 0, row++, 2, 1);

        // موجودی
        TextField stockField = createEditField("100");
        stockField.setPromptText("مثال: 100");
        form.add(createEditRow("📦 موجودی", stockField), 0, row++, 2, 1);

        // --- ویژگی‌های محصول ---
        Label featuresTitle = new Label("⚙️ ویژگی‌های محصول");
        featuresTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        featuresTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(featuresTitle, 2);
        form.add(featuresTitle, 0, row++);

        // شیرینی
        VBox sweetnessBox = createFeatureBox("🍬 شیرینی", "0");
        CheckBox sweetnessCheck = (CheckBox) ((HBox) sweetnessBox.getChildren().get(0)).getChildren().get(0);
        Slider sweetnessSlider = (Slider) sweetnessBox.getChildren().get(1);
        sweetnessSlider.setDisable(true);
        sweetnessSlider.setValue(0);

        sweetnessCheck.selectedProperty().addListener((obs, old, newVal) -> {
            sweetnessSlider.setDisable(!newVal);
            if (!newVal) sweetnessSlider.setValue(0);
        });

        form.add(createEditRow("", sweetnessBox), 0, row++, 2, 1);

        // کافئین
        VBox caffeineBox = createFeatureBox("☕ کافئین", "0");
        CheckBox caffeineCheck = (CheckBox) ((HBox) caffeineBox.getChildren().get(0)).getChildren().get(0);
        Slider caffeineSlider = (Slider) caffeineBox.getChildren().get(1);
        caffeineSlider.setDisable(true);
        caffeineSlider.setValue(0);

        caffeineCheck.selectedProperty().addListener((obs, old, newVal) -> {
            caffeineSlider.setDisable(!newVal);
            if (!newVal) caffeineSlider.setValue(0);
        });

        form.add(createEditRow("", caffeineBox), 0, row++, 2, 1);

        // دما
        VBox temperatureBox = createTemperatureBox();
        CheckBox tempCheck = (CheckBox) ((HBox) temperatureBox.getChildren().get(0)).getChildren().get(0);
        Slider tempSlider = (Slider) temperatureBox.getChildren().get(1);
        tempSlider.setDisable(true);
        tempSlider.setValue(70);

        tempCheck.selectedProperty().addListener((obs, old, newVal) -> {
            tempSlider.setDisable(!newVal);
            if (!newVal) tempSlider.setValue(0);
        });

        form.add(createEditRow("", temperatureBox), 0, row++, 2, 1);

        // --- تصویر محصول ---
        Label imageTitle = new Label("🖼️ تصویر محصول");
        imageTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        imageTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(imageTitle, 2);
        form.add(imageTitle, 0, row++);

        HBox imageBox = new HBox(10);
        imageBox.setAlignment(Pos.CENTER_LEFT);

        TextField imageField = createEditField("");
        imageField.setPromptText("نام فایل تصویر (پس از آپلود)");
        imageField.setEditable(false);
        imageField.setPrefWidth(250);

        Button uploadBtn = new Button("📤 آپلود");
        uploadBtn.setStyle("-fx-font-size: 13px; -fx-background-color: " + toRgbString(SECONDARY_COLOR) + ";" +
                "-fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");

        ProgressIndicator uploadProgress = new ProgressIndicator();
        uploadProgress.setStyle("-fx-progress-color: #10b981;");
        uploadProgress.setMaxSize(20, 20);
        uploadProgress.setVisible(false);

        uploadBtn.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(dialog);
            if (file != null) {
                uploadProgress.setVisible(true);
                uploadBtn.setDisable(true);

                scheduler.submit(() -> {
                    String uploadedFilename = api.uploadButtonImage(file);

                    Platform.runLater(() -> {
                        uploadProgress.setVisible(false);
                        uploadBtn.setDisable(false);

                        if (uploadedFilename != null) {
                            imageField.setText(uploadedFilename);
                            showToast("✅ تصویر با موفقیت آپلود شد", SUCCESS_COLOR);
                        } else {
                            showToast("❌ خطا در آپلود تصویر", ERROR_COLOR);
                        }
                    });
                });
            }
        });

        imageBox.getChildren().addAll(imageField, uploadBtn, uploadProgress);
        form.add(createEditRow("🖼️ تصویر", imageBox), 0, row++, 2, 1);

        // --- تنظیمات پیشرفته ---
        Label advancedTitle = new Label("⚡ تنظیمات پیشرفته");
        advancedTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        advancedTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(advancedTitle, 2);
        form.add(advancedTitle, 0, row++);

        // نوع دکمه
        HBox typeBox = new HBox(20);
        typeBox.setAlignment(Pos.CENTER_LEFT);

        RadioButton defaultBtn = new RadioButton("دکمه پیش‌فرض");
        defaultBtn.setStyle("-fx-text-fill: white;");
        defaultBtn.setSelected(true);

        RadioButton userBtn = new RadioButton("دکمه اختصاصی");
        userBtn.setStyle("-fx-text-fill: white;");

        ToggleGroup typeGroup = new ToggleGroup();
        defaultBtn.setToggleGroup(typeGroup);
        userBtn.setToggleGroup(typeGroup);

        typeBox.getChildren().addAll(defaultBtn, userBtn);
        form.add(createEditRow("📌 نوع دکمه", typeBox), 0, row++, 2, 1);

        // انتخاب کاربر (فقط زمانی نمایش داده می‌شود که دکمه اختصاصی انتخاب شود)
        VBox userSelectionBox = new VBox(5);
        userSelectionBox.setVisible(false);
        userSelectionBox.setManaged(false);

        Label userLabel = new Label("انتخاب مالک دکمه:");
        userLabel.setStyle("-fx-text-fill: " + toRgbString(TEXT_PRIMARY) + "; -fx-font-size: 13px;");

        userCombo = new ComboBox<>();
        userCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #334155; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 8 15; " +
                "-fx-font-size: 13px;");
        userCombo.setPrefHeight(40);
        userCombo.setPrefWidth(400);
        userCombo.setPromptText("انتخاب کاربر...");

        // تنظیم نحوه نمایش کاربران در کامبوباکس
        userCombo.setCellFactory(param -> new ListCell<ManageUsersPage.UserModel>() {
            @Override
            protected void updateItem(ManageUsersPage.UserModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullname() + " (" + item.getUsername() + ")");
                }
            }
        });

        userCombo.setButtonCell(new ListCell<ManageUsersPage.UserModel>() {
            @Override
            protected void updateItem(ManageUsersPage.UserModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullname() + " (" + item.getUsername() + ")");
                }
            }
        });

        // لود کردن کاربران
        if (usersList != null && !usersList.isEmpty()) {
            userCombo.setItems(FXCollections.observableArrayList(usersList));
        }

        userSelectionBox.getChildren().addAll(userLabel, userCombo);
        form.add(createEditRow("👤 مالک دکمه", userSelectionBox), 0, row++, 2, 1);

        // تغییر visibility بر اساس انتخاب
        typeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == userBtn) {
                userSelectionBox.setVisible(true);
                userSelectionBox.setManaged(true);
            } else {
                userSelectionBox.setVisible(false);
                userSelectionBox.setManaged(false);
            }
        });

        // Buttons
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20, 0, 0, 0));

        Button saveBtn = new Button("💾 ذخیره دکمه");
        saveBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(SUCCESS_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12 30; -fx-cursor: hand;");

        Button cancelBtn = new Button("❌ انصراف");
        cancelBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(ERROR_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12 30; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            // Validation
            if (titleField.getText().trim().isEmpty()) {
                showToast("عنوان محصول نمی‌تواند خالی باشد", ERROR_COLOR);
                return;
            }

            if (priceField.getText().trim().isEmpty()) {
                showToast("قیمت را وارد کنید", ERROR_COLOR);
                return;
            }

            double price;
            int stock;
            try {
                price = Double.parseDouble(priceField.getText().trim());
                stock = Integer.parseInt(stockField.getText().trim());
            } catch (NumberFormatException ex) {
                showToast("مقادیر عددی را به درستی وارد کنید", ERROR_COLOR);
                return;
            }

            // بررسی انتخاب کاربر برای دکمه اختصاصی
            if (userBtn.isSelected() && userCombo.getValue() == null) {
                showToast("لطفاً یک کاربر را به عنوان مالک انتخاب کنید", WARNING_COLOR);
                return;
            }

            // Show loading
            ProgressIndicator savingIndicator = new ProgressIndicator();
            savingIndicator.setStyle("-fx-progress-color: #10b981;");
            savingIndicator.setMaxSize(40, 40);
            content.getChildren().add(savingIndicator);
            saveBtn.setDisable(true);
            cancelBtn.setDisable(true);

            int targetUserId = defaultBtn.isSelected() ? 0 : userCombo.getValue() != null ?
                    Integer.parseInt(String.valueOf(userCombo.getValue().getId())) : 0;

            int finalTargetUserId = targetUserId;
            scheduler.submit(() -> {
                boolean success = api.addButton(
                        titleField.getText().trim(),
                        captionArea.getText().trim(),
                        imageField.getText().trim(),
                        price,
                        (int) sweetnessSlider.getValue(),
                        (int) caffeineSlider.getValue(),
                        tempCheck.isSelected() ? (int) tempSlider.getValue() : 0,
                        stock,
                        finalTargetUserId
                );

                Platform.runLater(() -> {
                    content.getChildren().remove(savingIndicator);
                    saveBtn.setDisable(false);
                    cancelBtn.setDisable(false);

                    if (success) {
                        String ownerName = defaultBtn.isSelected() ? "پیش‌فرض" :
                                userCombo.getValue().getFullname();
                        showToast("✅ دکمه با موفقیت اضافه شد", SUCCESS_COLOR);

                        Logger.log(
                                currentAdmin.getUsername(),
                                "Add Button",
                                "Button added: " + titleField.getText() + " - Owner: " + ownerName,
                                titleField.getText(),
                                "Button",
                                "Info"
                        );

                        dialog.close();
                        loadButtons();
                    } else {
                        showToast("❌ خطا در افزودن دکمه", ERROR_COLOR);
                    }
                });
            });
        });

        buttons.getChildren().addAll(saveBtn, cancelBtn);

        content.getChildren().addAll(header, separator, form, buttons);

        Scene scene = new Scene(scrollPane, 750, 800);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        // تنظیم موقعیت دیالوگ در مرکز صفحه اصلی
        dialog.setOnShown(e -> {
            dialog.setX(primaryStage.getX() + (primaryStage.getWidth() - 750) / 2);
            dialog.setY(primaryStage.getY() + (primaryStage.getHeight() - 800) / 2);
        });

        dialog.showAndWait();
    }

    private void showEditButtonDialog(ButtonModel button) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 20; " +
                "-fx-border-color: #4f46e5; -fx-border-width: 2; -fx-border-radius: 20;");
        content.setEffect(new DropShadow(25, Color.BLACK));
        content.setPrefWidth(700);
        content.setPrefHeight(750);
        content.setMaxWidth(700);
        content.setMaxHeight(750);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setPrefWidth(720);
        scrollPane.setPrefHeight(770);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("✏️");
        icon.setStyle("-fx-font-size: 30px;");

        VBox titleBox = new VBox(5);
        Label title = new Label("ویرایش دکمه");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 22));
        title.setTextFill(TEXT_PRIMARY);

        Label subtitle = new Label("شناسه دکمه: " + button.getId() + " | " + button.getTitle());
        subtitle.setFont(Font.font("Tahoma", 12));
        subtitle.setTextFill(TEXT_SECONDARY);

        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(icon, titleBox);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #334155;");

        // Form
        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(15);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20, 0, 20, 0));
        form.setPrefWidth(600);

        int row = 0;

        // --- اطلاعات پایه ---
        Label basicTitle = new Label("📋 اطلاعات پایه");
        basicTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        basicTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(basicTitle, 2);
        form.add(basicTitle, 0, row++);

        // عنوان محصول
        TextField titleField = createEditField(button.getTitle());
        form.add(createEditRow("📝 عنوان محصول", titleField), 0, row++, 2, 1);

        // توضیحات
        TextArea captionArea = createEditTextArea(button.getCaption());
        form.add(createEditRow("📄 توضیحات", captionArea), 0, row++, 2, 1);

        // --- قیمت و موجودی ---
        Label priceTitle = new Label("💰 قیمت و موجودی");
        priceTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        priceTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(priceTitle, 2);
        form.add(priceTitle, 0, row++);

        // قیمت
        TextField priceField = createEditField(String.valueOf((int) button.getPrice()));
        form.add(createEditRow("💵 قیمت (تومان)", priceField), 0, row++, 2, 1);

        // موجودی
        TextField stockField = createEditField(String.valueOf(button.getStock()));
        form.add(createEditRow("📦 موجودی", stockField), 0, row++, 2, 1);

        // --- ویژگی‌های محصول ---
        Label featuresTitle = new Label("⚙️ ویژگی‌های محصول");
        featuresTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        featuresTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(featuresTitle, 2);
        form.add(featuresTitle, 0, row++);

        // شیرینی
        VBox sweetnessBox = createFeatureBox("🍬 شیرینی", String.valueOf(button.getSweetnessLevel()));
        CheckBox sweetnessCheck = (CheckBox) ((HBox) sweetnessBox.getChildren().get(0)).getChildren().get(0);
        Slider sweetnessSlider = (Slider) sweetnessBox.getChildren().get(1);
        sweetnessCheck.setSelected(button.getSweetnessLevel() > 0);
        sweetnessSlider.setValue(button.getSweetnessLevel());
        sweetnessSlider.setDisable(!sweetnessCheck.isSelected());

        sweetnessCheck.selectedProperty().addListener((obs, old, newVal) -> {
            sweetnessSlider.setDisable(!newVal);
            if (!newVal) sweetnessSlider.setValue(0);
        });

        form.add(createEditRow("", sweetnessBox), 0, row++, 2, 1);

        // کافئین
        VBox caffeineBox = createFeatureBox("☕ کافئین", String.valueOf(button.getCaffeineLevel()));
        CheckBox caffeineCheck = (CheckBox) ((HBox) caffeineBox.getChildren().get(0)).getChildren().get(0);
        Slider caffeineSlider = (Slider) caffeineBox.getChildren().get(1);
        caffeineCheck.setSelected(button.getCaffeineLevel() > 0);
        caffeineSlider.setValue(button.getCaffeineLevel());
        caffeineSlider.setDisable(!caffeineCheck.isSelected());

        caffeineCheck.selectedProperty().addListener((obs, old, newVal) -> {
            caffeineSlider.setDisable(!newVal);
            if (!newVal) caffeineSlider.setValue(0);
        });

        form.add(createEditRow("", caffeineBox), 0, row++, 2, 1);

        // دما
        VBox temperatureBox = createTemperatureBox();
        CheckBox tempCheck = (CheckBox) ((HBox) temperatureBox.getChildren().get(0)).getChildren().get(0);
        Slider tempSlider = (Slider) temperatureBox.getChildren().get(1);
        tempCheck.setSelected(button.getTemperatureLevel() > 0);
        tempSlider.setValue(button.getTemperatureLevel() > 0 ? button.getTemperatureLevel() : 70);
        tempSlider.setDisable(!tempCheck.isSelected());

        tempCheck.selectedProperty().addListener((obs, old, newVal) -> {
            tempSlider.setDisable(!newVal);
            if (!newVal) tempSlider.setValue(0);
        });

        form.add(createEditRow("", temperatureBox), 0, row++, 2, 1);

        // --- تصویر محصول ---
        Label imageTitle = new Label("🖼️ تصویر محصول");
        imageTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        imageTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(imageTitle, 2);
        form.add(imageTitle, 0, row++);

        HBox imageBox = new HBox(10);
        imageBox.setAlignment(Pos.CENTER_LEFT);

        TextField imageField = createEditField(button.getImage());
        imageField.setEditable(false);
        imageField.setPrefWidth(250);

        Button uploadBtn = new Button("📤 آپلود");
        uploadBtn.setStyle("-fx-font-size: 13px; -fx-background-color: " + toRgbString(SECONDARY_COLOR) + ";" +
                "-fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");

        ProgressIndicator uploadProgress = new ProgressIndicator();
        uploadProgress.setStyle("-fx-progress-color: #10b981;");
        uploadProgress.setMaxSize(20, 20);
        uploadProgress.setVisible(false);

        uploadBtn.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(dialog);
            if (file != null) {
                uploadProgress.setVisible(true);
                uploadBtn.setDisable(true);

                scheduler.submit(() -> {
                    String uploadedFilename = api.uploadButtonImage(file);

                    Platform.runLater(() -> {
                        uploadProgress.setVisible(false);
                        uploadBtn.setDisable(false);

                        if (uploadedFilename != null) {
                            imageField.setText(uploadedFilename);
                            showToast("✅ تصویر با موفقیت آپلود شد", SUCCESS_COLOR);
                        } else {
                            showToast("❌ خطا در آپلود تصویر", ERROR_COLOR);
                        }
                    });
                });
            }
        });

        imageBox.getChildren().addAll(imageField, uploadBtn, uploadProgress);
        form.add(createEditRow("🖼️ تصویر", imageBox), 0, row++, 2, 1);

        // Buttons
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20, 0, 0, 0));

        Button saveBtn = new Button("💾 ذخیره تغییرات");
        saveBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(SUCCESS_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12 30; -fx-cursor: hand;");

        Button cancelBtn = new Button("❌ انصراف");
        cancelBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(ERROR_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12 30; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            // Validation
            if (titleField.getText().trim().isEmpty()) {
                showToast("عنوان محصول نمی‌تواند خالی باشد", ERROR_COLOR);
                return;
            }

            if (priceField.getText().trim().isEmpty()) {
                showToast("قیمت را وارد کنید", ERROR_COLOR);
                return;
            }

            double price;
            int stock;
            try {
                price = Double.parseDouble(priceField.getText().trim());
                stock = Integer.parseInt(stockField.getText().trim());
            } catch (NumberFormatException ex) {
                showToast("مقادیر عددی را به درستی وارد کنید", ERROR_COLOR);
                return;
            }

            // Show loading
            ProgressIndicator savingIndicator = new ProgressIndicator();
            savingIndicator.setStyle("-fx-progress-color: #10b981;");
            savingIndicator.setMaxSize(40, 40);
            content.getChildren().add(savingIndicator);
            saveBtn.setDisable(true);
            cancelBtn.setDisable(true);

            scheduler.submit(() -> {
                boolean success = api.updateButton(
                        button.getId(),
                        titleField.getText().trim(),
                        captionArea.getText().trim(),
                        imageField.getText().trim(),
                        price,
                        (int) sweetnessSlider.getValue(),
                        (int) caffeineSlider.getValue(),
                        tempCheck.isSelected() ? (int) tempSlider.getValue() : 0,
                        stock
                );

                Platform.runLater(() -> {
                    content.getChildren().remove(savingIndicator);
                    saveBtn.setDisable(false);
                    cancelBtn.setDisable(false);

                    if (success) {
                        showToast("✅ دکمه با موفقیت ویرایش شد", SUCCESS_COLOR);

                        Logger.log(
                                currentAdmin.getUsername(),
                                "Edit Button",
                                "Button updated: " + button.getTitle() + " -> " + titleField.getText(),
                                String.valueOf(button.getId()),
                                "Button",
                                "Info"
                        );

                        dialog.close();
                        loadButtons();
                    } else {
                        showToast("❌ خطا در ویرایش دکمه", ERROR_COLOR);
                    }
                });
            });
        });

        buttons.getChildren().addAll(saveBtn, cancelBtn);

        content.getChildren().addAll(header, separator, form, buttons);

        Scene scene = new Scene(scrollPane, 750, 800);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        // تنظیم موقعیت دیالوگ در مرکز صفحه اصلی
        dialog.setOnShown(e -> {
            dialog.setX(primaryStage.getX() + (primaryStage.getWidth() - 750) / 2);
            dialog.setY(primaryStage.getY() + (primaryStage.getHeight() - 800) / 2);
        });

        dialog.showAndWait();
    }

    private VBox createFeatureBox(String label, String defaultValue) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-background-radius: 10;");

        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        CheckBox checkBox = new CheckBox(label);
        checkBox.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        Label valueLabel = new Label(defaultValue);
        valueLabel.setStyle("-fx-text-fill: " + toRgbString(ACCENT_COLOR) + "; -fx-font-weight: bold;");

        topRow.getChildren().addAll(checkBox, valueLabel);
        box.setUserData(checkBox);

        Slider slider = new Slider(0, 10, Double.parseDouble(defaultValue));
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(2);
        slider.setMinorTickCount(1);
        slider.setPrefWidth(400);
        slider.setStyle("-fx-control-inner-background: #334155;");

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            valueLabel.setText(String.valueOf(newVal.intValue()));
        });

        box.getChildren().addAll(topRow, slider);
        return box;
    }

    private VBox createTemperatureBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-background-radius: 10;");

        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        CheckBox checkBox = new CheckBox("🌡️ دمای سرو");
        checkBox.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        Label valueLabel = new Label("70°C");
        valueLabel.setStyle("-fx-text-fill: " + toRgbString(HAS_TEMPERATURE_COLOR) + "; -fx-font-weight: bold;");

        topRow.getChildren().addAll(checkBox, valueLabel);

        Slider slider = new Slider(0, 100, 70);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(20);
        slider.setMinorTickCount(5);
        slider.setPrefWidth(400);
        slider.setStyle("-fx-control-inner-background: #334155;");

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            valueLabel.setText(newVal.intValue() + "°C");
        });

        box.getChildren().addAll(topRow, slider);
        return box;
    }

    private void showDeleteConfirmation(ButtonModel button) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 20; " +
                "-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 20;");
        content.setEffect(new DropShadow(25, Color.BLACK));
        content.setPrefWidth(450);
        content.setPrefHeight(300);

        Label warningIcon = new Label("⚠️");
        warningIcon.setStyle("-fx-font-size: 50px;");

        Label title = new Label("حذف دکمه");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        title.setTextFill(ERROR_COLOR);

        Label message = new Label("آیا از حذف دکمه " + button.getTitle() +
                "\nبا قیمت " + button.getPriceFormatted() + " اطمینان دارید؟");
        message.setFont(Font.font("Tahoma", 14));
        message.setTextFill(TEXT_SECONDARY);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setWrapText(true);

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button confirmBtn = new Button("🗑️ بله، حذف شود");
        confirmBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(ERROR_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");

        Button cancelBtn = new Button("❌ انصراف");
        cancelBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(SECONDARY_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        confirmBtn.setOnAction(e -> {
            dialog.close();

            StackPane loadingOverlay = createLoadingOverlay("در حال حذف دکمه...");
            root.getChildren().add(loadingOverlay);

            scheduler.submit(() -> {
                boolean deleted = api.deleteButton(button.getId());

                Platform.runLater(() -> {
                    root.getChildren().remove(loadingOverlay);

                    if (deleted) {
                        showToast("✅ دکمه با موفقیت حذف شد", SUCCESS_COLOR);

                        Logger.log(
                                currentAdmin.getUsername(),
                                "Delete Button",
                                "Button deleted: " + button.getTitle(),
                                String.valueOf(button.getId()),
                                "Button",
                                "Warning"
                        );

                        loadButtons();
                    } else {
                        showToast("❌ خطا در حذف دکمه", ERROR_COLOR);
                    }
                });
            });
        });

        buttons.getChildren().addAll(confirmBtn, cancelBtn);
        content.getChildren().addAll(warningIcon, title, message, buttons);

        Scene scene = new Scene(content);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        // تنظیم موقعیت دیالوگ در مرکز صفحه اصلی
        dialog.setOnShown(e -> {
            dialog.setX(primaryStage.getX() + (primaryStage.getWidth() - 450) / 2);
            dialog.setY(primaryStage.getY() + (primaryStage.getHeight() - 300) / 2);
        });

        dialog.showAndWait();
    }

    private void loadButtons() {
        loadingIndicator.setVisible(true);
        buttonsList.clear();

        scheduler.submit(() -> {
            List<ButtonModel> buttons = api.getAllButtons();

            Platform.runLater(() -> {
                if (buttons != null && !buttons.isEmpty()) {
                    buttonsList.addAll(buttons);

                    // Update stats
                    totalButtonsLabel.setText(String.valueOf(buttonsList.size()));

                    long inStock = buttonsList.stream()
                            .filter(b -> b.getStock() > 0)
                            .count();
                    activeButtonsLabel.setText(String.valueOf(inStock));

                    long defaultButtons = buttonsList.stream()
                            .filter(b -> b.getUserId() == 0)
                            .count();
                    defaultButtonsLabel.setText(String.valueOf(defaultButtons));

                    // Highest price
                    double maxPrice = buttonsList.stream()
                            .mapToDouble(ButtonModel::getPrice)
                            .max()
                            .orElse(0);
                    maxPriceLabel.setText(String.format("%,d", (int) maxPrice) + " تومان");
                }

                filteredData = new FilteredList<>(buttonsList, p -> true);
                sortedData = new SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(buttonsTable.comparatorProperty());

                buttonsTable.setItems(sortedData);
                loadingIndicator.setVisible(false);
            });
        });
    }

    private void filterButtons() {
        if (filteredData == null) return;

        String searchText = searchField.getText().toLowerCase();
        String filterType = filterCombo.getValue();

        filteredData.setPredicate(button -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    button.getTitle().toLowerCase().contains(searchText) ||
                    button.getCaption().toLowerCase().contains(searchText) ||
                    String.valueOf((int) button.getPrice()).contains(searchText) ||
                    button.getUserName().toLowerCase().contains(searchText);

            if (!matchesSearch) return false;

            switch (filterType) {
                case "دکمه‌های پیش‌فرض":
                    return button.getUserId() == 0;
                case "دکمه‌های کاربران":
                    return button.getUserId() > 0;
                case "دارای شیرینی":
                    return button.getSweetnessLevel() > 0;
                case "دارای کافئین":
                    return button.getCaffeineLevel() > 0;
                case "دارای دما":
                    return button.getTemperatureLevel() > 0;
                case "کمتر از ۱۰۰۰۰":
                    return button.getPrice() < 10000;
                case "بین ۱۰ تا ۵۰ هزار":
                    return button.getPrice() >= 10000 && button.getPrice() <= 50000;
                case "بیشتر از ۵۰ هزار":
                    return button.getPrice() > 50000;
                default:
                    return true;
            }
        });
    }

    private HBox createEditRow(String labelText, Node field) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefWidth(600);
        row.setMinWidth(600);

        Label label = new Label(labelText);
        label.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
        label.setTextFill(TEXT_SECONDARY);
        label.setPrefWidth(120);
        label.setMinWidth(120);
        label.setWrapText(true);

        if (field instanceof TextField) {
            ((TextField) field).setPrefWidth(400);
            ((TextField) field).setMinWidth(400);
        } else if (field instanceof TextArea) {
            ((TextArea) field).setPrefWidth(400);
            ((TextArea) field).setMinWidth(400);
            ((TextArea) field).setPrefRowCount(3);
        } else if (field instanceof PasswordField) {
            ((PasswordField) field).setPrefWidth(400);
            ((PasswordField) field).setMinWidth(400);
        } else if (field instanceof VBox) {
            ((VBox) field).setPrefWidth(400);
            ((VBox) field).setMinWidth(400);
        } else if (field instanceof HBox) {
            ((HBox) field).setPrefWidth(400);
            ((HBox) field).setMinWidth(400);
        }

        row.getChildren().addAll(label, field);
        return row;
    }

    private TextField createEditField(String value) {
        TextField field = new TextField(value != null ? value : "");
        field.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #334155; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10 15; " +
                "-fx-font-size: 13px;");
        field.setPrefHeight(40);
        return field;
    }

    private TextArea createEditTextArea(String value) {
        TextArea area = new TextArea(value != null ? value : "");
        area.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #334155; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10 15; " +
                "-fx-font-size: 13px; " +
                "-fx-control-inner-background: transparent;");
        area.setWrapText(true);
        area.setPrefRowCount(3);
        return area;
    }

    private StackPane createLoadingOverlay(String message) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        overlay.setPrefSize(1400, 850);

        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(Pos.CENTER);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setStyle("-fx-progress-color: #10b981;");
        progress.setMaxSize(50, 50);

        Label loadingLabel = new Label(message);
        loadingLabel.setFont(Font.font("Tahoma", 14));
        loadingLabel.setTextFill(Color.WHITE);

        loadingBox.getChildren().addAll(progress, loadingLabel);
        overlay.getChildren().add(loadingBox);

        return overlay;
    }

    private void showToast(String message, Color color) {
        Platform.runLater(() -> {
            Label toast = new Label(message);
            toast.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
            toast.setTextFill(Color.WHITE);
            toast.setPadding(new Insets(10, 20, 10, 20));
            toast.setAlignment(Pos.CENTER);
            toast.setStyle("-fx-background-color: " + toRgbString(color) + ";" +
                    "-fx-background-radius: 20;");
            toast.setMaxWidth(400);
            toast.setWrapText(true);

            StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
            StackPane.setMargin(toast, new Insets(0, 0, 20, 0));
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
        });
    }

    private String toRgbString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public Parent getRoot() {
        return root;
    }

    public void cleanup() {
        scheduler.shutdown();
    }
}