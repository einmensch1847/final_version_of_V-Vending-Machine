package View;

import api.ApiClient;
import api.Logger;
import com.example.vwm.Main;
import com.example.vwm.SceneManager;
import javafx.animation.*;
import javafx.application.Platform;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ManageUsersPage {

    private StackPane root;
    private Admin currentAdmin;
    private ApiClient api = new ApiClient();
    private Stage primaryStage;

    // UI Components
    private TableView<UserModel> userTable;
    private TextField searchField;
    private ComboBox<String> filterCombo;
    private Label totalUsersLabel, activeUsersLabel, companiesLabel, devicesLabel;
    private Button editBtn, deleteBtn, refreshBtn, backBtn;
    private ProgressIndicator loadingIndicator;
    private Pane animatedBg;

    // Data
    private ObservableList<UserModel> userList = FXCollections.observableArrayList();
    private FilteredList<UserModel> filteredData;
    private SortedList<UserModel> sortedData;

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

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // مدل داده کاربر - کاملاً مطابق با دیتابیس
    public static class UserModel {
        private final SimpleStringProperty id;
        private final SimpleStringProperty username;
        private final SimpleStringProperty fullname;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty email;
        private final SimpleStringProperty address;
        private final SimpleStringProperty companyName;
        private final SimpleStringProperty ceoName;
        private final SimpleStringProperty ceoPhone;
        private final SimpleStringProperty ceoEmail;
        private final SimpleStringProperty deviceLocation;
        private final SimpleStringProperty backgroundImage;
        private final SimpleStringProperty createdAt;
        private final SimpleStringProperty createdByAdmin;

        public UserModel(String id, String username, String fullname, String phone,
                         String email, String address, String companyName, String ceoName,
                         String ceoPhone, String ceoEmail, String deviceLocation,
                         String backgroundImage, String createdAt, String createdByAdmin) {
            this.id = new SimpleStringProperty(id != null ? id : "");
            this.username = new SimpleStringProperty(username != null ? username : "");
            this.fullname = new SimpleStringProperty(fullname != null ? fullname : "");
            this.phone = new SimpleStringProperty(phone != null ? phone : "");
            this.email = new SimpleStringProperty(email != null ? email : "");
            this.address = new SimpleStringProperty(address != null ? address : "");
            this.companyName = new SimpleStringProperty(companyName != null ? companyName : "");
            this.ceoName = new SimpleStringProperty(ceoName != null ? ceoName : "");
            this.ceoPhone = new SimpleStringProperty(ceoPhone != null ? ceoPhone : "");
            this.ceoEmail = new SimpleStringProperty(ceoEmail != null ? ceoEmail : "");
            this.deviceLocation = new SimpleStringProperty(deviceLocation != null ? deviceLocation : "");
            this.backgroundImage = new SimpleStringProperty(backgroundImage != null ? backgroundImage : "default_bg.jpg");
            this.createdAt = new SimpleStringProperty(createdAt != null ? createdAt : "");
            this.createdByAdmin = new SimpleStringProperty(createdByAdmin != null ? createdByAdmin : "");
        }

        // Getters
        public int getId() {
            try {
                return Integer.parseInt(id.get());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        public String getIdString() { return id.get(); }
        public String getUsername() { return username.get(); }
        public String getFullname() { return fullname.get(); }
        public String getPhone() { return phone.get(); }
        public String getEmail() { return email.get(); }
        public String getAddress() { return address.get(); }
        public String getCompanyName() { return companyName.get(); }
        public String getCeoName() { return ceoName.get(); }
        public String getCeoPhone() { return ceoPhone.get(); }
        public String getCeoEmail() { return ceoEmail.get(); }
        public String getDeviceLocation() { return deviceLocation.get(); }
        public String getBackgroundImage() { return backgroundImage.get(); }
        public String getCreatedAt() { return createdAt.get(); }
        public String getCreatedByAdmin() { return createdByAdmin.get(); }

        // Property Getters for TableView
        public SimpleStringProperty idProperty() { return id; }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty fullnameProperty() { return fullname; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty companyNameProperty() { return companyName; }
        public SimpleStringProperty deviceLocationProperty() { return deviceLocation; }
        public SimpleStringProperty createdAtProperty() { return createdAt; }
    }

    public ManageUsersPage(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.primaryStage = Main.getInstance().getPrimaryStage();
        createUI();
        loadUsers();
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #0f172a;");

        // پس‌زمینه ثابت بدون انیمیشن
        animatedBg = createStaticBackground();

        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setMaxWidth(1400);
        mainContainer.setMaxHeight(850);
        mainContainer.setPrefWidth(1400);
        mainContainer.setPrefHeight(850);

        // Glass card background
        StackPane contentPane = new StackPane();
        contentPane.setStyle(
                "-fx-background-color: " + toRgbString(CARD_BG) + ";" +
                        "-fx-background-radius: 30;" +
                        "-fx-opacity: 0.95;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 30, 0, 0, 10);"
        );

        VBox content = createContent();
        contentPane.getChildren().add(content);

        mainContainer.getChildren().add(contentPane);
        root.getChildren().addAll(animatedBg, mainContainer);
    }

    private Pane createStaticBackground() {
        Pane bg = new Pane();
        bg.setStyle("-fx-background-color: #0f172a;");

        // ذرات ثابت بدون انیمیشن
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

        // Header
        HBox header = createHeader();
        header.setPrefHeight(60);
        header.setMinHeight(60);

        // Stats cards
        HBox statsCards = createStatsCards();
        statsCards.setPrefHeight(120);
        statsCards.setMinHeight(120);
        statsCards.setMaxHeight(120);

        // Search and filter bar
        HBox searchBar = createSearchBar();
        searchBar.setPrefHeight(70);
        searchBar.setMinHeight(70);
        searchBar.setMaxHeight(70);

        // User table
        VBox tableContainer = createUserTable();
        tableContainer.setPrefHeight(400);
        tableContainer.setMaxHeight(400);
        tableContainer.setMinHeight(400);

        // Action buttons
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
        Label title = new Label("مدیریت کاربران");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 28));
        title.setTextFill(TEXT_PRIMARY);

        Label subtitle = new Label("مشاهده، جستجو و ویرایش اطلاعات کاربران");
        subtitle.setFont(Font.font("Tahoma", 14));
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

        VBox totalCard = createStatCard("👥", "کل کاربران", "0", PRIMARY_COLOR);
        totalUsersLabel = (Label) totalCard.getUserData();

        VBox activeCard = createStatCard("✅", "کاربران فعال", "0", SUCCESS_COLOR);
        activeUsersLabel = (Label) activeCard.getUserData();

        VBox companyCard = createStatCard("🏢", "شرکت‌ها", "0", SECONDARY_COLOR);
        companiesLabel = (Label) companyCard.getUserData();

        VBox deviceCard = createStatCard("📱", "دستگاه‌ها", "0", ACCENT_COLOR);
        devicesLabel = (Label) deviceCard.getUserData();

        stats.getChildren().addAll(totalCard, activeCard, companyCard, deviceCard);
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
        titleLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 14));
        titleLabel.setTextFill(TEXT_SECONDARY);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 24));
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
        searchField.setPromptText("🔍 جستجو بر اساس نام، نام کاربری، شرکت، ایمیل، تلفن...");
        searchField.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 25; " +
                "-fx-border-color: #334155; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 25; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 12 20; " +
                "-fx-font-size: 14px;");
        searchField.setPrefHeight(45);
        searchField.setPrefWidth(500);
        searchField.setMinWidth(500);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers());

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("همه کاربران", "دارای شرکت", "بدون شرکت");
        filterCombo.setValue("همه کاربران");
        filterCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 25; " +
                "-fx-border-color: #334155; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 25; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 8 15; " +
                "-fx-font-size: 14px;");
        filterCombo.setPrefHeight(45);
        filterCombo.setPrefWidth(150);
        filterCombo.setOnAction(e -> filterUsers());

        refreshBtn = new Button("🔄 بروزرسانی");
        refreshBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(SECONDARY_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12 25; -fx-cursor: hand;");
        refreshBtn.setPrefHeight(45);
        refreshBtn.setOnAction(e -> loadUsers());

        searchBar.getChildren().addAll(searchField, filterCombo, refreshBtn);
        return searchBar;
    }

    private VBox createUserTable() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPrefHeight(400);
        container.setMaxHeight(400);
        container.setMinHeight(400);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4f46e5;");
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(50, 50);

        userTable = new TableView<>();
        userTable.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        userTable.setPrefHeight(380);
        userTable.setMinHeight(380);
        userTable.setMaxHeight(380);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setPlaceholder(new Label("هیچ کاربری یافت نشد"));
        userTable.setFixedCellSize(40);

        // ID Column
        TableColumn<UserModel, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;-fx-text-fill: #ffff;-fx-text-alignment: #ffff");

        // Username Column
        TableColumn<UserModel, String> usernameCol = new TableColumn<>("نام کاربری");
        usernameCol.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        usernameCol.setPrefWidth(120);
        usernameCol.setStyle("-fx-alignment: CENTER-LEFT;-fx-text-fill: #ffff;-fx-text-alignment: #ffff");

        // Fullname Column
        TableColumn<UserModel, String> fullnameCol = new TableColumn<>("نام کامل");
        fullnameCol.setCellValueFactory(cellData -> cellData.getValue().fullnameProperty());
        fullnameCol.setPrefWidth(150);
        fullnameCol.setStyle("-fx-alignment: CENTER-LEFT;-fx-text-fill: #ffff;-fx-text-alignment: #ffff");

        // Company Column
        TableColumn<UserModel, String> companyCol = new TableColumn<>("شرکت");
        companyCol.setCellValueFactory(cellData -> cellData.getValue().companyNameProperty());
        companyCol.setPrefWidth(150);
        companyCol.setStyle("-fx-alignment: CENTER-LEFT;-fx-text-fill: #ffff;-fx-text-alignment: #ffff");

        // Phone Column
        TableColumn<UserModel, String> phoneCol = new TableColumn<>("تلفن");
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        phoneCol.setPrefWidth(120);
        phoneCol.setStyle("-fx-alignment: CENTER-LEFT;-fx-text-fill: #ffff;-fx-text-alignment: #ffff");

        // Email Column
        TableColumn<UserModel, String> emailCol = new TableColumn<>("ایمیل");
        emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        emailCol.setPrefWidth(180);
        emailCol.setStyle("-fx-alignment: CENTER-LEFT;-fx-text-fill: #ffff;-fx-text-alignment: #ffff");

        // Device Location Column
        TableColumn<UserModel, String> deviceCol = new TableColumn<>("محل دستگاه");
        deviceCol.setCellValueFactory(cellData -> cellData.getValue().deviceLocationProperty());
        deviceCol.setPrefWidth(150);
        deviceCol.setStyle("-fx-alignment: CENTER-LEFT;-fx-text-fill: #ffff;-fx-text-alignment: #ffff");

        // Created At Column
        TableColumn<UserModel, String> createdCol = new TableColumn<>("تاریخ ثبت");
        createdCol.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());
        createdCol.setPrefWidth(150);
        createdCol.setStyle("-fx-alignment: CENTER;-fx-text-fill: #ffff;-fx-text-alignment: #ffff");

        // Action Column
        TableColumn<UserModel, Void> actionCol = new TableColumn<>("عملیات");
        actionCol.setPrefWidth(100);
        actionCol.setSortable(false);
        actionCol.setCellFactory(col -> {
            TableCell<UserModel, Void> cell = new TableCell<>() {
                private final Button editBtn = new Button("✏️ ویرایش");
                {
                    editBtn.setStyle("-fx-background-color: rgba(79,70,229,0.8); -fx-text-fill: white; " +
                            "-fx-background-radius: 5; -fx-padding: 5 10; -fx-cursor: hand; " +
                            "-fx-font-size: 11px;");
                    editBtn.setMaxWidth(70);
                    editBtn.setMinWidth(70);
                    editBtn.setOnAction(e -> {
                        UserModel user = getTableView().getItems().get(getIndex());
                        showEditUserDialog(user);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(editBtn);
                    }
                    setAlignment(Pos.CENTER);
                }
            };
            return cell;
        });

        userTable.getColumns().addAll(idCol, usernameCol, fullnameCol, companyCol,
                phoneCol, emailCol, deviceCol, createdCol, actionCol);

        // Row styling - بدون انیمیشن برای جلوگیری از لرزش
        userTable.setRowFactory(tv -> {
            TableRow<UserModel> row = new TableRow<>();
            row.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            row.setPrefHeight(40);
            return row;
        });

        container.getChildren().addAll(loadingIndicator, userTable);
        VBox.setVgrow(userTable, Priority.NEVER);

        return container;
    }

    private HBox createActionButtons() {
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10, 0, 0, 0));

        editBtn = new Button("✏️ ویرایش کاربر انتخاب شده");
        editBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(PRIMARY_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12 25; -fx-cursor: hand;");
        editBtn.setPrefHeight(40);
        editBtn.setOnAction(e -> {
            UserModel selected = userTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditUserDialog(selected);
            } else {
                showToast("لطفاً یک کاربر را انتخاب کنید", WARNING_COLOR);
            }
        });

        deleteBtn = new Button("🗑️ حذف کاربر");
        deleteBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: " + toRgbString(ERROR_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12 25; -fx-cursor: hand;");
        deleteBtn.setPrefHeight(40);
        deleteBtn.setOnAction(e -> {
            UserModel selected = userTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showDeleteConfirmation(selected);
            } else {
                showToast("لطفاً یک کاربر را انتخاب کنید", WARNING_COLOR);
            }
        });

        actions.getChildren().addAll(editBtn, deleteBtn);
        return actions;
    }

    private void loadUsers() {
        loadingIndicator.setVisible(true);
        userList.clear();

        scheduler.submit(() -> {
            List<UserModel> users = api.getAllUsers();

            Platform.runLater(() -> {
                if (users != null && !users.isEmpty()) {
                    userList.addAll(users);

                    // Update stats
                    totalUsersLabel.setText(String.valueOf(userList.size()));

                    long companies = userList.stream()
                            .filter(u -> u.getCompanyName() != null && !u.getCompanyName().isEmpty())
                            .count();
                    companiesLabel.setText(String.valueOf(companies));

                    long devices = userList.stream()
                            .filter(u -> u.getDeviceLocation() != null && !u.getDeviceLocation().isEmpty())
                            .count();
                    devicesLabel.setText(String.valueOf(devices));

                    activeUsersLabel.setText(String.valueOf(userList.size())); // Temporary
                }

                filteredData = new FilteredList<>(userList, p -> true);
                sortedData = new SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(userTable.comparatorProperty());

                userTable.setItems(sortedData);
                loadingIndicator.setVisible(false);
            });
        });
    }

    private void filterUsers() {
        if (filteredData == null) return;

        String searchText = searchField.getText().toLowerCase();
        String filterType = filterCombo.getValue();

        filteredData.setPredicate(user -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    user.getFullname().toLowerCase().contains(searchText) ||
                    user.getUsername().toLowerCase().contains(searchText) ||
                    (user.getCompanyName() != null && user.getCompanyName().toLowerCase().contains(searchText)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText)) ||
                    (user.getPhone() != null && user.getPhone().contains(searchText)) ||
                    (user.getDeviceLocation() != null && user.getDeviceLocation().toLowerCase().contains(searchText));

            if (!matchesSearch) return false;

            switch (filterType) {
                case "دارای شرکت":
                    return user.getCompanyName() != null && !user.getCompanyName().isEmpty();
                case "بدون شرکت":
                    return user.getCompanyName() == null || user.getCompanyName().isEmpty();
                default:
                    return true;
            }
        });
    }

    private void showEditUserDialog(UserModel user) {
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
        content.setPrefWidth(750);
        content.setPrefHeight(700);
        content.setMaxWidth(750);
        content.setMaxHeight(700);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setPrefWidth(770);
        scrollPane.setPrefHeight(720);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("✏️");
        icon.setStyle("-fx-font-size: 30px;");

        VBox titleBox = new VBox(5);
        Label title = new Label("ویرایش اطلاعات کاربر");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 22));
        title.setTextFill(TEXT_PRIMARY);

        Label subtitle = new Label("نام کاربری: " + user.getUsername() + " (قابل ویرایش نیست)");
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
        form.setPrefWidth(650);

        int row = 0;

        // --- اطلاعات شخصی ---
        Label personalTitle = new Label("📋 اطلاعات شخصی");
        personalTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        personalTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(personalTitle, 2);
        form.add(personalTitle, 0, row++);

        // نام کامل
        TextField fullnameField = createEditField(user.getFullname());
        form.add(createEditRow("👤 نام کامل", fullnameField), 0, row++, 2, 1);

        // شماره تلفن
        TextField phoneField = createEditField(user.getPhone());
        form.add(createEditRow("📱 تلفن همراه", phoneField), 0, row++, 2, 1);

        // ایمیل
        TextField emailField = createEditField(user.getEmail());
        form.add(createEditRow("📧 ایمیل", emailField), 0, row++, 2, 1);

        // آدرس
        TextArea addressArea = createEditTextArea(user.getAddress());
        form.add(createEditRow("🏠 آدرس", addressArea), 0, row++, 2, 1);

        // --- اطلاعات شرکت ---
        Label companyTitle = new Label("🏢 اطلاعات شرکت");
        companyTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        companyTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(companyTitle, 2);
        form.add(companyTitle, 0, row++);

        // نام شرکت
        TextField companyNameField = createEditField(user.getCompanyName());
        form.add(createEditRow("🏭 نام شرکت", companyNameField), 0, row++, 2, 1);

        // نام مدیرعامل
        TextField ceoNameField = createEditField(user.getCeoName());
        form.add(createEditRow("👨‍💼 مدیرعامل", ceoNameField), 0, row++, 2, 1);

        // تلفن مدیرعامل
        TextField ceoPhoneField = createEditField(user.getCeoPhone());
        form.add(createEditRow("📞 تلفن مدیرعامل", ceoPhoneField), 0, row++, 2, 1);

        // ایمیل مدیرعامل
        TextField ceoEmailField = createEditField(user.getCeoEmail());
        form.add(createEditRow("📧 ایمیل مدیرعامل", ceoEmailField), 0, row++, 2, 1);

        // --- اطلاعات دستگاه ---
        Label deviceTitle = new Label("📱 اطلاعات دستگاه");
        deviceTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        deviceTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(deviceTitle, 2);
        form.add(deviceTitle, 0, row++);

        // محل دستگاه
        TextField deviceLocationField = createEditField(user.getDeviceLocation());
        form.add(createEditRow("📍 محل دستگاه", deviceLocationField), 0, row++, 2, 1);

        // عکس پس‌زمینه
        TextField bgField = createEditField(user.getBackgroundImage());
        bgField.setEditable(false);
        bgField.setStyle(bgField.getStyle() + "-fx-background-color: rgba(255,255,255,0.03);");
        form.add(createEditRow("🖼️ پس‌زمینه", bgField), 0, row++, 2, 1);

        // --- تغییر رمز عبور ---
        Label passwordTitle = new Label("🔐 تغییر رمز عبور");
        passwordTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        passwordTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(passwordTitle, 2);
        form.add(passwordTitle, 0, row++);

        // رمز جدید
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("رمز عبور جدید (خالی بگذارید برای عدم تغییر)");
        newPasswordField.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #334155; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10 15; " +
                "-fx-font-size: 13px;");
        newPasswordField.setPrefHeight(40);
        newPasswordField.setPrefWidth(400);
        form.add(createEditRow("🔒 رمز جدید", newPasswordField), 0, row++, 2, 1);

        // تکرار رمز
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("تکرار رمز عبور جدید");
        confirmPasswordField.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #334155; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10 15; " +
                "-fx-font-size: 13px;");
        confirmPasswordField.setPrefHeight(40);
        confirmPasswordField.setPrefWidth(400);
        form.add(createEditRow("🔐 تکرار رمز", confirmPasswordField), 0, row++, 2, 1);

        // --- اطلاعات سیستم ---
        Label systemTitle = new Label("⚙️ اطلاعات سیستم");
        systemTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        systemTitle.setTextFill(PRIMARY_COLOR);
        GridPane.setColumnSpan(systemTitle, 2);
        form.add(systemTitle, 0, row++);

        // تاریخ ثبت
        TextField createdAtField = createEditField(user.getCreatedAt());
        createdAtField.setEditable(false);
        createdAtField.setStyle("-fx-background-color: rgba(255,255,255,0.03); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #334155; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-text-fill: #94a3b8; " +
                "-fx-padding: 10 15; " +
                "-fx-font-size: 13px;");
        form.add(createEditRow("📅 تاریخ ثبت", createdAtField), 0, row++, 2, 1);

        // ثبت کننده
        TextField createdByField = createEditField(user.getCreatedByAdmin());
        createdByField.setEditable(false);
        createdByField.setStyle("-fx-background-color: rgba(255,255,255,0.03); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #334155; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-text-fill: #94a3b8; " +
                "-fx-padding: 10 15; " +
                "-fx-font-size: 13px;");
        form.add(createEditRow("👤 ثبت کننده", createdByField), 0, row++, 2, 1);

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
            if (fullnameField.getText().trim().isEmpty()) {
                showToast("نام کامل نمی‌تواند خالی باشد", ERROR_COLOR);
                return;
            }

            if (!newPasswordField.getText().isEmpty() &&
                    !newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showToast("رمز عبور و تکرار آن مطابقت ندارند", ERROR_COLOR);
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
                // Update user info
                boolean updated = api.updateUser(
                        user.getId(),
                        fullnameField.getText().trim(),
                        phoneField.getText().trim(),
                        emailField.getText().trim(),
                        addressArea.getText().trim(),
                        companyNameField.getText().trim(),
                        ceoNameField.getText().trim(),
                        ceoPhoneField.getText().trim(),
                        ceoEmailField.getText().trim(),
                        deviceLocationField.getText().trim(),
                        user.getBackgroundImage()
                );

                // Update password if provided
                boolean passwordUpdated = true;
                if (!newPasswordField.getText().isEmpty()) {
                    passwordUpdated = api.updateUserPassword(
                            user.getId(),
                            newPasswordField.getText().trim()
                    );
                }

                final boolean finalUpdated = updated;
                final boolean finalPasswordUpdated = passwordUpdated;

                Platform.runLater(() -> {
                    content.getChildren().remove(savingIndicator);
                    saveBtn.setDisable(false);
                    cancelBtn.setDisable(false);

                    if (finalUpdated && finalPasswordUpdated) {
                        showToast("✅ اطلاعات کاربر با موفقیت بروزرسانی شد", SUCCESS_COLOR);

                        Logger.log(
                                currentAdmin.getUsername(),
                                "Edit User",
                                "User updated: " + user.getUsername(),
                                user.getUsername(),
                                "User",
                                "Info"
                        );

                        dialog.close();
                        loadUsers();
                    } else {
                        showToast("❌ خطا در بروزرسانی اطلاعات", ERROR_COLOR);
                    }
                });
            });
        });

        buttons.getChildren().addAll(saveBtn, cancelBtn);

        content.getChildren().addAll(header, separator, form, buttons);

        Scene scene = new Scene(scrollPane, 800, 750);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        // تنظیم موقعیت دیالوگ در مرکز صفحه اصلی
        dialog.setOnShown(e -> {
            dialog.setX(primaryStage.getX() + (primaryStage.getWidth() - 800) / 2);
            dialog.setY(primaryStage.getY() + (primaryStage.getHeight() - 750) / 2);
        });

        dialog.showAndWait();
    }

    private HBox createEditRow(String labelText, Control field) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefWidth(650);
        row.setMinWidth(650);

        Label label = new Label(labelText);
        label.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
        label.setTextFill(TEXT_SECONDARY);
        label.setPrefWidth(120);
        label.setMinWidth(120);
        label.setWrapText(true);

        if (field instanceof TextField) {
            ((TextField) field).setPrefWidth(450);
            ((TextField) field).setMinWidth(450);
        } else if (field instanceof TextArea) {
            ((TextArea) field).setPrefWidth(450);
            ((TextArea) field).setMinWidth(450);
            ((TextArea) field).setPrefRowCount(3);
        } else if (field instanceof PasswordField) {
            ((PasswordField) field).setPrefWidth(450);
            ((PasswordField) field).setMinWidth(450);
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

    private void showDeleteConfirmation(UserModel user) {
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

        Label title = new Label("حذف کاربر");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        title.setTextFill(ERROR_COLOR);

        Label message = new Label("آیا از حذف کاربر " + user.getFullname() +
                "\nبا نام کاربری " + user.getUsername() + " اطمینان دارید؟");
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

            StackPane loadingOverlay = createLoadingOverlay("در حال حذف کاربر...");
            root.getChildren().add(loadingOverlay);

            scheduler.submit(() -> {
                boolean deleted = api.deleteUser(user.getId());

                Platform.runLater(() -> {
                    root.getChildren().remove(loadingOverlay);

                    if (deleted) {
                        showToast("✅ کاربر با موفقیت حذف شد", SUCCESS_COLOR);

                        Logger.log(
                                currentAdmin.getUsername(),
                                "Delete User",
                                "User deleted: " + user.getUsername(),
                                user.getUsername(),
                                "User",
                                "Warning"
                        );

                        loadUsers();
                    } else {
                        showToast("❌ خطا در حذف کاربر", ERROR_COLOR);
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
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    public Parent getRoot() {
        return root;
    }

    public void cleanup() {
        scheduler.shutdown();
    }
}