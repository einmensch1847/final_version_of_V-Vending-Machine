package View;

import api.ApiClient;
import com.example.vwm.Main;
import com.example.vwm.SceneManager;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class UserLoginLogsPage {

    private VBox root;
    private TableView<UserLoginLog> table;
    private ApiClient api = new ApiClient();
    private Admin currentAdmin;
    private ObservableList<UserLoginLog> allLogs = FXCollections.observableArrayList();
    private FilteredList<UserLoginLog> filteredLogs;
    private TextField searchField;
    private ComboBox<String> actionFilter;
    private ComboBox<String> userFilter;
    private DatePicker dateFromPicker;
    private DatePicker dateToPicker;
    private Label statsLabel;
    private ProgressIndicator loadingIndicator;

    // Colors
    private static final Color PRIMARY_COLOR = Color.web("#4f46e5");
    private static final Color SECONDARY_COLOR = Color.web("#7c3aed");
    private static final Color ACCENT_COLOR = Color.web("#10b981");
    private static final Color ERROR_COLOR = Color.web("#ef4444");
    private static final Color SUCCESS_COLOR = Color.web("#22c55e");
    private static final Color WARNING_COLOR = Color.web("#f59e0b");
    private static final Color TEXT_PRIMARY = Color.web("#f8fafc");
    private static final Color TEXT_SECONDARY = Color.web("#cbd5e1");
    private static final Color BORDER_COLOR = Color.web("#334155");

    public UserLoginLogsPage(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        filteredLogs = new FilteredList<>(allLogs, p -> true);
        createUI();
        setupFilters();
        loadLogs();
    }

    private void createUI() {
        root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #0f172a;");

        // Title
        Label title = new Label("📊 لاگ‌های ورود و خروج کاربران");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 28));
        title.setTextFill(TEXT_PRIMARY);

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4f46e5;");
        loadingIndicator.setMaxSize(50, 50);
        loadingIndicator.setVisible(false);

        // Search panel
        HBox searchPanel = createSearchPanel();

        // Filter panel
        HBox filterPanel = createFilterPanel();

        // Table
        table = new TableView<>();
        table.setPrefHeight(500);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: " + toRgbString(BORDER_COLOR) + "; " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
        table.setPlaceholder(new Label("هیچ لاگی یافت نشد"));
        addColumns();

        SortedList<UserLoginLog> sortedLogs = new SortedList<>(filteredLogs);
        sortedLogs.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedLogs);

        // Stats panel
        statsLabel = new Label("در حال بارگذاری...");
        statsLabel.setStyle("-fx-text-fill: " + toRgbString(TEXT_SECONDARY) + "; -fx-font-size: 14px;");

        // Button panel
        HBox buttonPanel = createButtonPanel();

        root.getChildren().addAll(title, searchPanel, filterPanel, loadingIndicator, table, statsLabel, buttonPanel);
    }

    private HBox createSearchPanel() {
        HBox panel = new HBox(10);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(10, 0, 10, 0));

        Label searchLabel = new Label("🔍 جستجو:");
        searchLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        searchField = new TextField();
        searchField.setPromptText("نام کاربری، نام کامل، IP، دستگاه...");
        searchField.setStyle("-fx-font-size: 14px; -fx-pref-width: 450; -fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-text-fill: white; -fx-prompt-text-fill: #94a3b8; -fx-background-radius: 8; " +
                "-fx-border-color: " + toRgbString(BORDER_COLOR) + "; -fx-border-radius: 8; -fx-padding: 10 15;");
        searchField.setPrefHeight(40);

        Button clearBtn = new Button("پاک کردن");
        clearBtn.setStyle("-fx-background-color: " + toRgbString(SECONDARY_COLOR) + "; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            applyFilters();
        });

        panel.getChildren().addAll(searchLabel, searchField, clearBtn);
        return panel;
    }

    private HBox createFilterPanel() {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(10, 0, 20, 0));

        // Action filter
        Label actionLabel = new Label("⚡ عملیات:");
        actionLabel.setStyle("-fx-text-fill: white;");

        actionFilter = new ComboBox<>();
        actionFilter.getItems().addAll("همه", "LOGIN", "LOGOUT", "LOGIN_FAILED");
        actionFilter.setValue("همه");
        actionFilter.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; " +
                "-fx-border-color: " + toRgbString(BORDER_COLOR) + "; -fx-border-radius: 8; -fx-padding: 8 15;");
        actionFilter.setPrefWidth(130);

        // User filter
        Label userLabel = new Label("👤 کاربر:");
        userLabel.setStyle("-fx-text-fill: white;");

        userFilter = new ComboBox<>();
        userFilter.getItems().add("همه");
        userFilter.setValue("همه");
        userFilter.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; " +
                "-fx-border-color: " + toRgbString(BORDER_COLOR) + "; -fx-border-radius: 8; -fx-padding: 8 15;");
        userFilter.setPrefWidth(150);

        // Date filters
        Label dateLabel = new Label("📅 بازه:");
        dateLabel.setStyle("-fx-text-fill: white;");

        dateFromPicker = new DatePicker();
        dateFromPicker.setPromptText("از تاریخ");
        dateFromPicker.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; " +
                "-fx-border-color: " + toRgbString(BORDER_COLOR) + "; -fx-border-radius: 8;");
        dateFromPicker.setPrefWidth(130);

        Label toLabel = new Label("تا");
        toLabel.setStyle("-fx-text-fill: white;");

        dateToPicker = new DatePicker();
        dateToPicker.setPromptText("تا تاریخ");
        dateToPicker.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; " +
                "-fx-border-color: " + toRgbString(BORDER_COLOR) + "; -fx-border-radius: 8;");
        dateToPicker.setPrefWidth(130);

        Button clearFiltersBtn = new Button("🧹 پاک کردن فیلترها");
        clearFiltersBtn.setStyle("-fx-background-color: " + toRgbString(SECONDARY_COLOR) + "; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        clearFiltersBtn.setOnAction(e -> {
            actionFilter.setValue("همه");
            userFilter.setValue("همه");
            dateFromPicker.setValue(null);
            dateToPicker.setValue(null);
            searchField.clear();
            applyFilters();
        });

        panel.getChildren().addAll(
                actionLabel, actionFilter,
                userLabel, userFilter,
                dateLabel, dateFromPicker, toLabel, dateToPicker,
                clearFiltersBtn
        );

        return panel;
    }

    private HBox createButtonPanel() {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20, 0, 0, 0));

        Button refreshBtn = new Button("🔄 بروزرسانی");
        refreshBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: " + toRgbString(PRIMARY_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12 25; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadLogs());

        Button exportBtn = new Button("📥 خروجی Excel");
        exportBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: " + toRgbString(ACCENT_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12 25; -fx-cursor: hand;");
        exportBtn.setOnAction(e -> exportToExcel());

        Button backBtn = new Button("🏠 بازگشت");
        backBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: " + toRgbString(SECONDARY_COLOR) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12 25; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            AdminPanel panel1 = new AdminPanel(currentAdmin);
            SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), panel1.getRoot());
        });

        panel.getChildren().addAll(refreshBtn, exportBtn, backBtn);
        return panel;
    }

    private void addColumns() {
        table.getColumns().clear();

        // ستون ردیف
        TableColumn<UserLoginLog, String> colRow = new TableColumn<>("#");
        colRow.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(table.getItems().indexOf(cellData.getValue()) + 1))
        );
        colRow.setPrefWidth(50);
        colRow.setStyle("-fx-alignment: CENTER;");

        // ستون کاربر
        TableColumn<UserLoginLog, String> colUser = new TableColumn<>("👤 کاربر");
        colUser.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getFullname() + " (" + cellData.getValue().getUsername() + ")"
                )
        );
        colUser.setPrefWidth(180);
        colUser.setStyle("-fx-alignment: CENTER-LEFT;");

        // ستون عملیات
        TableColumn<UserLoginLog, String> colAction = new TableColumn<>("⚡ عملیات");
        colAction.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAction())
        );
        colAction.setPrefWidth(100);
        colAction.setStyle("-fx-alignment: CENTER;");
        colAction.setCellFactory(column -> new TableCell<UserLoginLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "LOGIN":
                            setTextFill(SUCCESS_COLOR);
                            setStyle("-fx-font-weight: bold;");
                            break;
                        case "LOGOUT":
                            setTextFill(WARNING_COLOR);
                            setStyle("-fx-font-weight: bold;");
                            break;
                        case "LOGIN_FAILED":
                            setTextFill(ERROR_COLOR);
                            setStyle("-fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });

        // ستون IP
        TableColumn<UserLoginLog, String> colIp = new TableColumn<>("🌐 IP");
        colIp.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getIpAddress())
        );
        colIp.setPrefWidth(120);
        colIp.setStyle("-fx-alignment: CENTER-LEFT;");

        // ستون دستگاه
        TableColumn<UserLoginLog, String> colDevice = new TableColumn<>("📱 دستگاه");
        colDevice.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDeviceInfo())
        );
        colDevice.setPrefWidth(100);
        colDevice.setStyle("-fx-alignment: CENTER;");

        // ستون وضعیت
        TableColumn<UserLoginLog, String> colStatus = new TableColumn<>("✅ وضعیت");
        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus())
        );
        colStatus.setPrefWidth(80);
        colStatus.setStyle("-fx-alignment: CENTER;");
        colStatus.setCellFactory(column -> new TableCell<UserLoginLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill("SUCCESS".equals(item) ? SUCCESS_COLOR : ERROR_COLOR);
                }
            }
        });

        // ستون خطا
        TableColumn<UserLoginLog, String> colError = new TableColumn<>("❌ خطا");
        colError.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getErrorMessage())
        );
        colError.setPrefWidth(200);
        colError.setStyle("-fx-alignment: CENTER-LEFT;");

        // ستون زمان
        TableColumn<UserLoginLog, String> colTime = new TableColumn<>("🕒 زمان");
        colTime.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt())
        );
        colTime.setPrefWidth(180);
        colTime.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(colRow, colUser, colAction, colIp, colDevice, colStatus, colError, colTime);
    }

    private void setupFilters() {
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        actionFilter.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        userFilter.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        dateFromPicker.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        dateToPicker.valueProperty().addListener((obs, old, newVal) -> applyFilters());
    }

    private void applyFilters() {
        if (filteredLogs == null) return;

        filteredLogs.setPredicate(log -> {
            // Search filter
            String search = searchField.getText();
            if (search != null && !search.isEmpty()) {
                String lower = search.toLowerCase();
                boolean matches =
                        (log.getUsername() != null && log.getUsername().toLowerCase().contains(lower)) ||
                                (log.getFullname() != null && log.getFullname().toLowerCase().contains(lower)) ||
                                (log.getIpAddress() != null && log.getIpAddress().toLowerCase().contains(lower)) ||
                                (log.getDeviceInfo() != null && log.getDeviceInfo().toLowerCase().contains(lower)) ||
                                (log.getErrorMessage() != null && log.getErrorMessage().toLowerCase().contains(lower));
                if (!matches) return false;
            }

            // Action filter
            String selectedAction = actionFilter.getValue();
            if (selectedAction != null && !selectedAction.equals("همه") && !selectedAction.equals("all")) {
                if (log.getAction() == null || !log.getAction().equals(selectedAction)) {
                    return false;
                }
            }

            // User filter
            String selectedUser = userFilter.getValue();
            if (selectedUser != null && !selectedUser.equals("همه")) {
                if (log.getUsername() == null || !log.getUsername().equals(selectedUser)) {
                    return false;
                }
            }

            return true;
        });
        updateStats();
    }

    private void loadLogs() {
        loadingIndicator.setVisible(true);
        table.setVisible(false);
        allLogs.clear();

        new Thread(() -> {
            try {
                System.out.println("=== Loading User Login Logs ===");
                List<ApiClient.UserLoginLog> logs = api.getUserLoginLogs(0, null, 500);
                System.out.println("API returned " + (logs != null ? logs.size() : 0) + " logs");

                Platform.runLater(() -> {
                    if (logs != null && !logs.isEmpty()) {
                        for (ApiClient.UserLoginLog log : logs) {
                            allLogs.add(new UserLoginLog(log));
                        }
                        System.out.println("Added " + allLogs.size() + " logs to table");
                    } else {
                        System.out.println("No logs to display");
                    }

                    updateUserFilter();
                    applyFilters();
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
                    statsLabel.setText("❌ خطا در بارگذاری لاگ‌ها");
                });
            }
        }).start();
    }

    private void updateUserFilter() {
        String current = userFilter.getValue();
        userFilter.getItems().clear();
        userFilter.getItems().add("همه");

        List<String> usernames = allLogs.stream()
                .map(UserLoginLog::getUsername)
                .filter(u -> u != null && !u.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        userFilter.getItems().addAll(usernames);

        if (current != null && userFilter.getItems().contains(current)) {
            userFilter.setValue(current);
        } else {
            userFilter.setValue("همه");
        }
    }

    private void updateStats() {
        int total = allLogs.size();
        int filtered = filteredLogs.size();

        long logins = filteredLogs.stream().filter(l -> "LOGIN".equals(l.getAction())).count();
        long logouts = filteredLogs.stream().filter(l -> "LOGOUT".equals(l.getAction())).count();
        long failed = filteredLogs.stream().filter(l -> "LOGIN_FAILED".equals(l.getAction())).count();

        statsLabel.setText(String.format(
                "📊 آمار: کل: %d | نمایش: %d | ✅ ورود موفق: %d | ⬅️ خروج: %d | ❌ ورود ناموفق: %d",
                total, filtered, logins, logouts, failed
        ));
    }

    private void exportToExcel() {
        if (allLogs.isEmpty()) {
            showAlert("خطا", "داده‌ای برای خروجی وجود ندارد", Alert.AlertType.WARNING);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("ذخیره فایل");
        chooser.setInitialFileName("user_login_logs_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        File file = chooser.showSaveDialog(null);
        if (file == null) return;

        try (java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8")) {
            writer.write('\ufeff'); // BOM for UTF-8
            writer.println("شناسه,کاربر,نام کامل,عملیات,IP,دستگاه,وضعیت,خطا,زمان");

            for (UserLoginLog log : filteredLogs) {
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        log.getId(),
                        escapeCsv(log.getUsername()),
                        escapeCsv(log.getFullname()),
                        log.getAction(),
                        log.getIpAddress(),
                        log.getDeviceInfo(),
                        log.getStatus(),
                        escapeCsv(log.getErrorMessage()),
                        log.getCreatedAt()
                );
            }

            showAlert("موفقیت", "فایل با موفقیت ذخیره شد:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
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

    public Parent getRoot() {
        return root;
    }

    // کلاس داخلی برای نمایش لاگ - با Property های عمومی
    public static class UserLoginLog {
        private final SimpleStringProperty id;
        private final SimpleStringProperty userId;
        private final SimpleStringProperty username;
        private final SimpleStringProperty fullname;
        private final SimpleStringProperty action;
        private final SimpleStringProperty ipAddress;
        private final SimpleStringProperty userAgent;
        private final SimpleStringProperty deviceInfo;
        private final SimpleStringProperty status;
        private final SimpleStringProperty errorMessage;
        private final SimpleStringProperty createdAt;

        public UserLoginLog(ApiClient.UserLoginLog log) {
            this.id = new SimpleStringProperty(String.valueOf(log.getId()));
            this.userId = new SimpleStringProperty(String.valueOf(log.getUserId()));
            this.username = new SimpleStringProperty(log.getUsername() != null ? log.getUsername() : "");
            this.fullname = new SimpleStringProperty(log.getFullname() != null ? log.getFullname() : "");
            this.action = new SimpleStringProperty(log.getAction() != null ? log.getAction() : "");
            this.ipAddress = new SimpleStringProperty(log.getIpAddress() != null ? log.getIpAddress() : "");
            this.userAgent = new SimpleStringProperty(log.getUserAgent() != null ? log.getUserAgent() : "");
            this.deviceInfo = new SimpleStringProperty(log.getDeviceInfo() != null ? log.getDeviceInfo() : "");
            this.status = new SimpleStringProperty(log.getStatus() != null ? log.getStatus() : "");
            this.errorMessage = new SimpleStringProperty(log.getErrorMessage() != null ? log.getErrorMessage() : "");
            this.createdAt = new SimpleStringProperty(log.getCreatedAt() != null ? log.getCreatedAt() : "");
        }

        public int getId() { return Integer.parseInt(id.get()); }
        public int getUserId() { return Integer.parseInt(userId.get()); }
        public String getUsername() { return username.get(); }
        public String getFullname() { return fullname.get(); }
        public String getAction() { return action.get(); }
        public String getIpAddress() { return ipAddress.get(); }
        public String getUserAgent() { return userAgent.get(); }
        public String getDeviceInfo() { return deviceInfo.get(); }
        public String getStatus() { return status.get(); }
        public String getErrorMessage() { return errorMessage.get(); }
        public String getCreatedAt() { return createdAt.get(); }

        // Property Getters برای JavaFX
        public SimpleStringProperty idProperty() { return id; }
        public SimpleStringProperty userIdProperty() { return userId; }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty fullnameProperty() { return fullname; }
        public SimpleStringProperty actionProperty() { return action; }
        public SimpleStringProperty ipAddressProperty() { return ipAddress; }
        public SimpleStringProperty userAgentProperty() { return userAgent; }
        public SimpleStringProperty deviceInfoProperty() { return deviceInfo; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty errorMessageProperty() { return errorMessage; }
        public SimpleStringProperty createdAtProperty() { return createdAt; }
    }
}