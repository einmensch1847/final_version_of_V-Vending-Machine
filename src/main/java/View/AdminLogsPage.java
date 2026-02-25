package View;

import api.ApiClient;
import com.example.vwm.Main;
import com.example.vwm.SceneManager;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminLogsPage {

    private VBox root;
    private TableView<AdminLog> table;
    private ApiClient api = new ApiClient();
    private Admin currentAdmin;
    private ObservableList<AdminLog> allLogs = FXCollections.observableArrayList();
    private FilteredList<AdminLog> filteredLogs;
    private TextField searchField;
    private ComboBox<String> severityFilter;
    private ComboBox<String> adminFilter;
    private DatePicker dateFromPicker;
    private DatePicker dateToPicker;
    private Label statsLabel;

    // برای ذخیره Excel
    private static final DateTimeFormatter excelTimestampFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public AdminLogsPage(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;

        // اول filteredLogs را initialize کنیم
        filteredLogs = new FilteredList<>(allLogs, p -> true);

        createUI();
        setupFilters();
        loadLogs();
    }

    private void createUI() {
        root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: rgba(40,40,40,0.95);");

        // Title
        Label title = new Label("📊 سیستم لاگ فعالیت ادمین‌ها");
        title.setStyle("-fx-font-size: 28px; -fx-text-fill: linear-gradient(to right, #4e9cff, #1c72ff); " +
                "-fx-font-weight: bold;");

        // Create search panel
        HBox searchPanel = createSearchPanel();

        // Create filter panel
        HBox filterPanel = createFilterPanel();

        // Create table
        table = new TableView<>();
        table.setPrefHeight(550);
        table.setStyle("-fx-background-color: rgba(30,30,30,0.9); -fx-background-radius: 10; " +
                "-fx-border-color: #4e9cff; -fx-border-width: 1; -fx-border-radius: 10;");
        addColumns();

        // Setup table with filtered data
        SortedList<AdminLog> sortedLogs = new SortedList<>(filteredLogs);
        sortedLogs.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedLogs);

        // Stats panel
        statsLabel = new Label("در حال بارگذاری...");
        statsLabel.setStyle("-fx-text-fill: #90ee90; -fx-font-size: 14px;");

        // Button panel
        HBox buttonPanel = createButtonPanel();

        // Add all components
        root.getChildren().addAll(
                title,
                searchPanel,
                filterPanel,
                table,
                statsLabel,
                buttonPanel
        );
    }

    private HBox createSearchPanel() {
        HBox searchPanel = new HBox(10);
        searchPanel.setAlignment(Pos.CENTER);

        Label searchLabel = new Label("🔍 جستجو:");
        searchLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        searchField = new TextField();
        searchField.setPromptText("جستجو در همه فیلدها...");
        searchField.setStyle("-fx-font-size: 14px; -fx-pref-width: 350; -fx-pref-height: 35; " +
                "-fx-background-radius: 8; -fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-text-fill: white; -fx-prompt-text-fill: #aaa;");

        Button clearSearchBtn = new Button("پاک کردن");
        styleSmallButton(clearSearchBtn);
        clearSearchBtn.setOnAction(e -> {
            searchField.clear();
            applyFilters();
        });

        searchPanel.getChildren().addAll(searchLabel, searchField, clearSearchBtn);
        return searchPanel;
    }

    private HBox createFilterPanel() {
        HBox filterPanel = new HBox(10);
        filterPanel.setAlignment(Pos.CENTER);

        // Severity filter
        Label severityLabel = new Label("⚡ شدت:");
        severityLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        severityFilter = new ComboBox<>();
        severityFilter.getItems().addAll("همه", "INFO", "WARNING", "ERROR", "CRITICAL");
        severityFilter.setValue("همه");
        severityFilter.setStyle("-fx-font-size: 14px; -fx-pref-width: 120; " +
                "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white;");

        // Admin filter
        Label adminLabel = new Label("👤 ادمین:");
        adminLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        adminFilter = new ComboBox<>();
        adminFilter.getItems().add("همه");
        adminFilter.setStyle("-fx-font-size: 14px; -fx-pref-width: 150; " +
                "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white;");

        // Date filters
        Label dateLabel = new Label("📅 بازه زمانی:");
        dateLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        dateFromPicker = new DatePicker();
        dateFromPicker.setPromptText("از تاریخ");
        dateFromPicker.setStyle("-fx-font-size: 14px; -fx-pref-width: 120;");

        Label toLabel = new Label("تا");
        toLabel.setStyle("-fx-text-fill: white;");

        dateToPicker = new DatePicker();
        dateToPicker.setPromptText("تا تاریخ");
        dateToPicker.setStyle("-fx-font-size: 14px; -fx-pref-width: 120;");

        Button clearFiltersBtn = new Button("🧹 پاک کردن فیلترها");
        styleSmallButton(clearFiltersBtn);
        clearFiltersBtn.setOnAction(e -> {
            severityFilter.setValue("همه");
            adminFilter.setValue("همه");
            dateFromPicker.setValue(null);
            dateToPicker.setValue(null);
            searchField.clear();
            applyFilters();
        });

        filterPanel.getChildren().addAll(
                severityLabel, severityFilter,
                adminLabel, adminFilter,
                dateLabel, dateFromPicker, toLabel, dateToPicker,
                clearFiltersBtn
        );

        return filterPanel;
    }

    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(15);
        buttonPanel.setAlignment(Pos.CENTER);

        Button refreshBtn = new Button("🔄 بروزرسانی");
        refreshBtn.setOnAction(e -> loadLogs());
        styleButton(refreshBtn);

        Button exportBtn = new Button("📊 خروجی Excel");
        exportBtn.setOnAction(e -> exportToExcel());
        styleButton(exportBtn, "-fx-background-color: linear-gradient(to right, #00b09b, #96c93d);");

//        Button clearAllBtn = new Button("🗑️ پاک کردن همه");
//        clearAllBtn.setOnAction(e -> clearAllLogs());
//        styleButton(clearAllBtn, "-fx-background-color: linear-gradient(to right, #ff416c, #ff4b2b);");

        Button backBtn = new Button("🏠 بازگشت");
        backBtn.setOnAction(e -> {
            AdminPanel panel = new AdminPanel(currentAdmin);
            SceneManager.switchSceneWithFadeTransition(
                    Main.getInstance().getPrimaryStage(),
                    panel.getRoot()
            );
        });
        styleButton(backBtn, "-fx-background-color: linear-gradient(to right, #8e2de2, #4a00e0);");

        buttonPanel.getChildren().addAll(refreshBtn, exportBtn, backBtn);
        return buttonPanel;
    }

    private void addColumns() {
        table.getColumns().clear();

        // Admin column
        TableColumn<AdminLog, String> colUser = new TableColumn<>("👤 ادمین");
        colUser.setCellValueFactory(cellData -> cellData.getValue().adminUsernameProperty());
        colUser.setPrefWidth(120);

        // Action column
        TableColumn<AdminLog, String> colAction = new TableColumn<>("⚡ عملیات");
        colAction.setCellValueFactory(cellData -> cellData.getValue().actionProperty());
        colAction.setPrefWidth(120);
        colAction.setCellFactory(column -> new TableCell<AdminLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("DELETE") || item.contains("REMOVE")) {
                        setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
                    } else if (item.contains("ADD") || item.contains("CREATE")) {
                        setStyle("-fx-text-fill: #90ee90; -fx-font-weight: bold;");
                    } else if (item.contains("UPDATE") || item.contains("EDIT")) {
                        setStyle("-fx-text-fill: #ffcc00; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });

        // Description column
        TableColumn<AdminLog, String> colDesc = new TableColumn<>("📝 توضیحات");
        colDesc.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        colDesc.setPrefWidth(250);

        // Target column
        TableColumn<AdminLog, String> colTarget = new TableColumn<>("🎯 هدف");
        colTarget.setCellValueFactory(cellData -> cellData.getValue().targetProperty());
        colTarget.setPrefWidth(150);

        // Severity column
        TableColumn<AdminLog, String> colSeverity = new TableColumn<>("⚠️ شدت");
        colSeverity.setCellValueFactory(cellData -> cellData.getValue().severityProperty());
        colSeverity.setPrefWidth(100);
        colSeverity.setCellFactory(column -> new TableCell<AdminLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toUpperCase()) {
                        case "CRITICAL":
                            setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold; " +
                                    "-fx-background-color: rgba(255,68,68,0.2); -fx-background-radius: 5;");
                            break;
                        case "ERROR":
                            setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
                            break;
                        case "WARNING":
                            setStyle("-fx-text-fill: #ffcc00; -fx-font-weight: bold;");
                            break;
                        case "INFO":
                            setStyle("-fx-text-fill: #90ee90;");
                            break;
                        default:
                            setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });

        // Time column
        TableColumn<AdminLog, String> colTime = new TableColumn<>("🕒 زمان");
        colTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        colTime.setPrefWidth(180);

        // Details button column (حذف شده - همه اطلاعات در جدول نمایش داده می‌شود)

        table.getColumns().addAll(colUser, colAction, colDesc, colTarget, colSeverity, colTime);

        // Set row factory for hover effect
        table.setRowFactory(tv -> new TableRow<AdminLog>() {
            @Override
            protected void updateItem(AdminLog item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    setOnMouseEntered(event -> {
                        setStyle("-fx-background-color: rgba(78,156,255,0.2);");
                    });
                    setOnMouseExited(event -> {
                        setStyle("");
                    });
                }
            }
        });
    }

    private void setupFilters() {
        // Search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Severity filter listener
        severityFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Admin filter listener
        adminFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Date filter listeners
        dateFromPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        dateToPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        filteredLogs.setPredicate(log -> {
            // Search filter
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                boolean matchesSearch =
                        (log.getAdminUsername() != null && log.getAdminUsername().toLowerCase().contains(lowerCaseFilter)) ||
                                (log.getAction() != null && log.getAction().toLowerCase().contains(lowerCaseFilter)) ||
                                (log.getDescription() != null && log.getDescription().toLowerCase().contains(lowerCaseFilter)) ||
                                (log.getTarget() != null && log.getTarget().toLowerCase().contains(lowerCaseFilter)) ||
                                (log.getSeverity() != null && log.getSeverity().toLowerCase().contains(lowerCaseFilter)) ||
                                (log.getTime() != null && log.getTime().toLowerCase().contains(lowerCaseFilter));
                if (!matchesSearch) {
                    return false;
                }
            }

            // Severity filter
            String selectedSeverity = severityFilter.getValue();
            if (selectedSeverity != null && !selectedSeverity.equals("همه") &&
                    log.getSeverity() != null) {
                if (!log.getSeverity().equals(selectedSeverity)) {
                    return false;
                }
            }

            // Admin filter
            String selectedAdmin = adminFilter.getValue();
            if (selectedAdmin != null && !selectedAdmin.equals("همه") &&
                    log.getAdminUsername() != null) {
                if (!log.getAdminUsername().equals(selectedAdmin)) {
                    return false;
                }
            }

            // Date filter - پیاده‌سازی ساده
            if (dateFromPicker.getValue() != null && dateToPicker.getValue() != null &&
                    log.getTime() != null) {
                // اینجا می‌توانید منطق مقایسه تاریخ را پیاده‌سازی کنید
                // فعلاً فقط return true می‌کنیم
            }

            return true;
        });
        updateStats();
    }

    private void loadLogs() {
        allLogs.clear();

        String data = api.getLogs();
        if (data == null || data.trim().isEmpty()) {
            updateStats();
            return;
        }

        String[] lines = data.split("\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split("\\|");
            if (parts.length >= 7) {
                allLogs.add(new AdminLog(
                        parts[0],
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4],
                        parts[5],
                        parts[6]
                ));
            }
        }

        // Update admin filter
        updateAdminFilter();
        applyFilters();
        updateStats();
    }

    private void updateAdminFilter() {
        String currentSelection = adminFilter.getValue();
        adminFilter.getItems().clear();
        adminFilter.getItems().add("همه");

        List<String> adminNames = allLogs.stream()
                .map(AdminLog::getAdminUsername)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        adminFilter.getItems().addAll(adminNames);

        if (currentSelection != null && adminFilter.getItems().contains(currentSelection)) {
            adminFilter.setValue(currentSelection);
        } else {
            adminFilter.setValue("همه");
        }
    }

    private void updateStats() {
        if (filteredLogs == null) {
            statsLabel.setText("در حال آماده‌سازی...");
            return;
        }

        int total = allLogs.size();
        int filtered = filteredLogs.size();

        long critical = allLogs.stream()
                .filter(l -> l.getSeverity() != null && l.getSeverity().equals("CRITICAL"))
                .count();
        long error = allLogs.stream()
                .filter(l -> l.getSeverity() != null && l.getSeverity().equals("ERROR"))
                .count();
        long warning = allLogs.stream()
                .filter(l -> l.getSeverity() != null && l.getSeverity().equals("WARNING"))
                .count();
        long info = allLogs.stream()
                .filter(l -> l.getSeverity() != null && l.getSeverity().equals("INFO"))
                .count();

        statsLabel.setText(String.format(
                "📊 آمار: کل: %d | نمایش: %d | 🟢 INFO: %d | 🟡 WARNING: %d | 🔴 ERROR: %d | ⚫ CRITICAL: %d",
                total, filtered, info, warning, error, critical
        ));
    }

    private void exportToExcel() {
        if (allLogs.isEmpty()) {
            showAlert("خطا", "داده‌ای برای خروجی گرفتن وجود ندارد!", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("ذخیره فایل خروجی");
        fileChooser.setInitialFileName("admin_logs_" +
                LocalDateTime.now().format(excelTimestampFormatter) + ".csv");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showSaveDialog(Main.getInstance().getPrimaryStage());
        if (file == null) {
            return;
        }

        try (java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8")) {
            // اضافه کردن BOM برای UTF-8 در Excel
            writer.write('\ufeff');

            // هدرها
            writer.println("ادمین,عملیات,توضیحات,هدف,شدت,زمان,آی‌دی هدف,نوع هدف");

            // داده‌ها
            for (AdminLog log : filteredLogs) {
                // escaping برای کاما و کوتیشن
                String admin = escapeCsv(log.getAdminUsername());
                String action = escapeCsv(log.getAction());
                String desc = escapeCsv(log.getDescription());
                String target = escapeCsv(log.getTarget());
                String severity = escapeCsv(log.getSeverity());
                String time = escapeCsv(log.getTime());
                String targetId = escapeCsv(log.getOriginalTargetId());
                String targetType = escapeCsv(log.getOriginalTargetType());

                writer.println(String.join(",",
                        admin, action, desc, target, severity, time, targetId, targetType
                ));
            }

            writer.flush();
            showAlert("موفقیت", "فایل با موفقیت ذخیره شد:\n" +
                            file.getAbsolutePath() +
                            "\n\nنکته: فایل CSV را می‌توانید در Excel باز کنید.",
                    Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("خطا", "خطا در ذخیره فایل: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        // اگر حاوی کاما، خط جدید یا کوتیشن باشد، در کوتیشن قرار می‌دهیم
        if (value.contains(",") || value.contains("\n") || value.contains("\"") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void clearAllLogs() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("تایید حذف");
        alert.setHeaderText("⚠️ اخطار: حذف همه لاگ‌ها");
        alert.setContentText("آیا مطمئن هستید می‌خواهید همه لاگ‌ها را پاک کنید؟\nاین عمل غیرقابل بازگشت است!");

        // استفاده از Dialog Pane مخصوص برای حفظ Full Screen
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: rgba(40,40,40,0.95); " +
                "-fx-border-color: #ff416c; -fx-border-width: 2;");
        dialogPane.lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: linear-gradient(to right, #ff416c, #ff4b2b); " +
                        "-fx-text-fill: white; -fx-font-weight: bold;"
        );
        dialogPane.lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: linear-gradient(to right, #4e9cff, #1c72ff); " +
                        "-fx-text-fill: white; -fx-font-weight: bold;"
        );

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            allLogs.clear();
            updateAdminFilter();
            applyFilters();
            updateStats();

            showAlert("موفقیت", "همه لاگ‌ها با موفقیت پاک شدند.",
                    Alert.AlertType.INFORMATION);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // استایل کردن Alert برای حفظ Full Screen
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: rgba(40,40,40,0.95); " +
                "-fx-text-fill: white;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // استفاده از StageStyle.UTILITY برای جلوگیری از خروج از Full Screen
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);

        alert.showAndWait();
    }

    private void styleButton(Button btn) {
        String baseStyle = "-fx-font-size: 16px; " +
                "-fx-background-radius: 10; " +
                "-fx-background-color: linear-gradient(to right, #4e9cff, #1c72ff); " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10 20; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(28,114,255,0.45), 8, 0.35, 0, 2);";

        btn.setStyle(baseStyle);
        btn.setPrefWidth(160);

        btn.setOnMouseEntered(e -> btn.setStyle(baseStyle +
                " -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));

        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    }

    private void styleButton(Button btn, String customStyle) {
        String baseStyle = customStyle +
                " -fx-font-size: 16px; " +
                "-fx-background-radius: 10; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10 20; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 8, 0.35, 0, 2);";

        btn.setStyle(baseStyle);
        btn.setPrefWidth(160);

        btn.setOnMouseEntered(e -> btn.setStyle(baseStyle +
                " -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));

        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    }

    private void styleSmallButton(Button btn) {
        String baseStyle = "-fx-font-size: 14px; " +
                "-fx-background-radius: 8; " +
                "-fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-text-fill: white; " +
                "-fx-padding: 5 15; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: #4e9cff; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8;";

        btn.setStyle(baseStyle);

        btn.setOnMouseEntered(e -> btn.setStyle(baseStyle +
                " -fx-background-color: rgba(78,156,255,0.3);"));

        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    }

    public Parent getRoot() {
        return root;
    }

    // ================== کلاس داخلی لاگ ==================
    public static class AdminLog {
        private final SimpleStringProperty adminUsername;
        private final SimpleStringProperty action;
        private final SimpleStringProperty description;
        private final SimpleStringProperty target;
        private final SimpleStringProperty severity;
        private final SimpleStringProperty time;
        private final String originalTargetId;
        private final String originalTargetType;

        public AdminLog(String adminUsername, String action, String description,
                        String targetId, String targetType, String severity, String time) {
            this.adminUsername = new SimpleStringProperty(adminUsername != null ? adminUsername : "");
            this.action = new SimpleStringProperty(action != null ? action : "");
            this.description = new SimpleStringProperty(description != null ? description : "");
            this.target = new SimpleStringProperty(
                    (targetType != null ? targetType : "") +
                            " (" + (targetId != null ? targetId : "") + ")"
            );
            this.severity = new SimpleStringProperty(severity != null ? severity : "");
            this.time = new SimpleStringProperty(time != null ? time : "");
            this.originalTargetId = targetId != null ? targetId : "";
            this.originalTargetType = targetType != null ? targetType : "";
        }

        // Getters
        public String getAdminUsername() { return adminUsername.get(); }
        public String getAction() { return action.get(); }
        public String getDescription() { return description.get(); }
        public String getTarget() { return target.get(); }
        public String getSeverity() { return severity.get(); }
        public String getTime() { return time.get(); }
        public String getOriginalTargetId() { return originalTargetId; }
        public String getOriginalTargetType() { return originalTargetType; }

        // Property getters
        public SimpleStringProperty adminUsernameProperty() { return adminUsername; }
        public SimpleStringProperty actionProperty() { return action; }
        public SimpleStringProperty descriptionProperty() { return description; }
        public SimpleStringProperty targetProperty() { return target; }
        public SimpleStringProperty severityProperty() { return severity; }
        public SimpleStringProperty timeProperty() { return time; }
    }
}