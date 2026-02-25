package View;

import api.ApiClient;
import api.Logger;
import com.example.vwm.Main;
import com.example.vwm.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddAdminForm {

    private VBox root;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField fullnameField;
    private TextField emailField;
    private TextField phoneField;
    private ComboBox<String> levelCombo;
    private Label statusLabel;

    private ApiClient api = new ApiClient();
    private Admin currentAdmin;

    private static final Color PRIMARY_COLOR = Color.web("#4e9cff");
    private static final Color ACCENT_COLOR = Color.web("#00ffaa");
    private static final Color ERROR_COLOR = Color.web("#ff6b6b");
    private static final Color SUCCESS_COLOR = Color.web("#90ee90");

    private static final DateTimeFormatter timestampFormatter =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public AddAdminForm(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        createUI();
    }

    private void createUI() {
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

        // Main form container
        VBox formContainer = new VBox(20);
        formContainer.setPadding(new Insets(40, 50, 40, 50));
        formContainer.setAlignment(Pos.TOP_CENTER);
        formContainer.setStyle("-fx-background-color: rgba(22, 33, 62, 0.8); " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: rgba(78, 156, 255, 0.3); " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 20;");
        formContainer.setEffect(new DropShadow(30, Color.BLACK));
        formContainer.setMaxWidth(500);

        // Header with icon
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER);

        Label icon = new Label("👥");
        icon.setStyle("-fx-font-size: 36px;");

        Label title = new Label("ایجاد حساب ادمین جدید");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(10, PRIMARY_COLOR));

        header.getChildren().addAll(icon, title);

        // Form fields container
        VBox fieldsContainer = new VBox(12);
        fieldsContainer.setAlignment(Pos.CENTER);

        // Fullname field
        VBox fullnameBox = createLabeledField("👤 نام و نام خانوادگی", fullnameField = new TextField());

        // Username field
        VBox usernameBox = createLabeledField("🔑 نام کاربری", usernameField = new TextField());

        // Password field
        VBox passwordBox = createLabeledField("🔒 رمز عبور", passwordField = new PasswordField());

        // Email field
        VBox emailBox = createLabeledField("📧 ایمیل", emailField = new TextField());

        // Phone field
        VBox phoneBox = createLabeledField("📱 تلفن", phoneField = new TextField());

        // Level ComboBox
        VBox levelBox = new VBox(5);
        levelBox.setAlignment(Pos.CENTER_LEFT);

        Label levelLabel = new Label("🎯 سطح دسترسی");
        levelLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("superadmin", "manager", "operator");
        levelCombo.setPromptText("انتخاب سطح دسترسی...");
        styleComboBox(levelCombo);

        levelBox.getChildren().addAll(levelLabel, levelCombo);

        // Status label
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setWrapText(true);

        // Buttons container
        HBox buttonsContainer = new HBox(15);
        buttonsContainer.setAlignment(Pos.CENTER);

        Button addBtn = new Button("➕ ایجاد حساب");
        styleActionButton(addBtn, PRIMARY_COLOR);
        addBtn.setOnAction(e -> handleAddAdmin());

        Button backBtn = new Button("↩️ بازگشت");
        styleActionButton(backBtn, Color.web("#8e2de2"));
        backBtn.setOnAction(e -> {
            AdminPanel panel = new AdminPanel(currentAdmin);
            SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), panel.getRoot());
        });

        buttonsContainer.getChildren().addAll(addBtn, backBtn);

        // Add all components to form
        fieldsContainer.getChildren().addAll(
                fullnameBox, usernameBox, passwordBox,
                emailBox, phoneBox, levelBox
        );

        formContainer.getChildren().addAll(
                header,
                new Separator(),
                fieldsContainer,
                statusLabel,
                buttonsContainer
        );

        // Final assembly
        StackPane mainContainer = new StackPane();
        mainContainer.getChildren().addAll(backgroundPane, formContainer);

        this.root = new VBox(mainContainer);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);
    }

    private VBox createLabeledField(String labelText, TextField field) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        field.setPromptText(labelText.substring(2)); // Remove emoji
        styleTextField(field);

        container.getChildren().addAll(label, field);
        return container;
    }

    private void styleTextField(TextField field) {
        String baseStyle = "-fx-font-size: 16px; " +
                "-fx-background-color: rgba(255, 255, 255, 0.08); " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: rgba(78, 156, 255, 0.3); " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 12; " +
                "-fx-padding: 12 20; " +
                "-fx-text-fill: white; " +
                "-fx-prompt-text-fill: rgba(255, 255, 255, 0.4); " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0.3, 0, 1);";

        field.setStyle(baseStyle);
        field.setPrefWidth(380);
        field.setPrefHeight(45);

        // Hover effect
        field.setOnMouseEntered(e -> {
            field.setStyle(baseStyle +
                    "-fx-border-color: rgba(78, 156, 255, 0.6); " +
                    "-fx-effect: dropshadow(gaussian, rgba(78,156,255,0.2), 5, 0.4, 0, 2);");
        });

        field.setOnMouseExited(e -> {
            field.setStyle(baseStyle);
        });
    }

    private void styleComboBox(ComboBox<String> combo) {
        String baseStyle = "-fx-font-size: 16px; " +
                "-fx-background-color: rgba(255, 255, 255, 0.08); " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: rgba(78, 156, 255, 0.3); " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 12; " +
                "-fx-padding: 12 20; " +
                "-fx-text-fill: white; " +
                "-fx-prompt-text-fill: rgba(255, 255, 255, 0.4); " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0.3, 0, 1);";

        combo.setStyle(baseStyle);
        combo.setPrefWidth(380);
        combo.setPrefHeight(45);

        // Style for the list
        combo.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-font-size: 14px; " +
                            "-fx-background-color: rgba(22, 33, 62, 0.9);");
                }
            }
        });

        // Hover effect
        combo.setOnMouseEntered(e -> {
            combo.setStyle(baseStyle +
                    "-fx-border-color: rgba(78, 156, 255, 0.6); " +
                    "-fx-effect: dropshadow(gaussian, rgba(78,156,255,0.2), 5, 0.4, 0, 2);");
        });

        combo.setOnMouseExited(e -> {
            combo.setStyle(baseStyle);
        });
    }

    private void styleActionButton(Button btn, Color color) {
        String baseStyle = "-fx-font-size: 15px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 12; " +
                "-fx-background-color: " + toRgbString(color) + "; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10 25; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, " + toRgbaString(color, 0.5) + ", 8, 0.4, 0, 2);";

        btn.setStyle(baseStyle);
        btn.setPrefHeight(45);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(baseStyle +
                    " -fx-scale-x: 1.05; -fx-scale-y: 1.05; " +
                    "-fx-effect: dropshadow(gaussian, " + toRgbaString(color, 0.7) + ", 10, 0.5, 0, 3);");
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
        });
    }

    private void handleAddAdmin() {
        String fullname = fullnameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String level = levelCombo.getValue();

        // Validation
        if (fullname.isEmpty() || username.isEmpty() || password.isEmpty() ||
                email.isEmpty() || phone.isEmpty() || level == null) {
            showStatus("❌ لطفاً تمام فیلدهای ضروری را پر کنید.", ERROR_COLOR);
            return;
        }

        if (password.length() < 6) {
            showStatus("❌ رمز عبور باید حداقل ۶ کاراکتر باشد.", ERROR_COLOR);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showStatus("❌ فرمت ایمیل نامعتبر است.", ERROR_COLOR);
            return;
        }

        // Show loading
        showStatus("⏳ در حال ایجاد حساب...", Color.web("#ffcc00"));

        // API call
        boolean ok = api.addAdmin(fullname, username, password, email, phone, level);

        if (ok) {
            showStatus("✅ حساب ادمین جدید با موفقیت ایجاد شد!", SUCCESS_COLOR);

            // لاگ گرفتن با استفاده از کلاس Logger موجود - **اصلاح شده**
            String desc = "Admin added: " + username
                    + ", Fullname: " + fullname
                    + ", Email: " + email
                    + ", Phone: " + phone
                    + ", Level: " + level;

            Logger.log(
                    currentAdmin.getUsername(),  // adminUsername
                    "Add Admin",                 // action
                    desc,                        // description
                    username,                    // targetId
                    "Admin",                     // targetType
                    "Info"                       // severity - با حروف بزرگ و کوچک درست
            );

            // Clear fields after successful creation
            clearFields();

            // نمایش فیش ثبت - اینجا اضافه شد
            showCustomReceipt(fullname, username, email, phone, level);

        } else {
            showStatus("❌ خطا در ایجاد حساب. ممکن است نام کاربری تکراری باشد یا خطای سرور رخ داده است.", ERROR_COLOR);
        }
    }

    private void showCustomReceipt(String fullname, String username, String email, String phone, String level) {
        // ایجاد پنجره مودال سفارشی
        Stage receiptStage = new Stage();
        receiptStage.initModality(Modality.APPLICATION_MODAL);
        receiptStage.initStyle(StageStyle.UNDECORATED); // بدون دکمه‌های استاندارد
        receiptStage.setResizable(false);

        // محتوای فیش
        VBox receiptContent = new VBox(20);
        receiptContent.setPadding(new Insets(40));
        receiptContent.setAlignment(Pos.CENTER);
        receiptContent.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: rgba(78, 156, 255, 0.8); " +
                "-fx-border-width: 3; " +
                "-fx-border-radius: 15;");
        receiptContent.setEffect(new DropShadow(25, Color.BLACK));

        // هدر فیش
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER);

        Label receiptIcon = new Label("📋");
        receiptIcon.setStyle("-fx-font-size: 40px;");

        VBox titleBox = new VBox(5);
        Label receiptTitle = new Label("فیش ثبت ادمین جدید");
        receiptTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 26));
        receiptTitle.setTextFill(Color.WHITE);
        receiptTitle.setEffect(new DropShadow(5, PRIMARY_COLOR));

        Label receiptSubtitle = new Label("VWM Admin System");
        receiptSubtitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));
        receiptSubtitle.setTextFill(Color.web("#aaaaaa"));

        titleBox.getChildren().addAll(receiptTitle, receiptSubtitle);
        headerBox.getChildren().addAll(receiptIcon, titleBox);

        // اطلاعات فیش در یک کارت
        VBox infoCard = new VBox(12);
        infoCard.setPadding(new Insets(25));
        infoCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: rgba(255, 255, 255, 0.15); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 12;");
        infoCard.setPrefWidth(400);

        // ردیف‌های اطلاعات
        HBox receiptIdRow = createInfoRow("🔢 شماره فیش:", System.currentTimeMillis() + "");
        HBox dateRow = createInfoRow("📅 تاریخ:",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        HBox timeRow = createInfoRow("🕒 زمان:",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        HBox issuerRow = createInfoRow("👤 صادرکننده:", currentAdmin.getFullname());

        // جداکننده
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(78, 156, 255, 0.3);");

        HBox nameRow = createInfoRow("👥 نام کامل:", fullname);
        HBox userRow = createInfoRow("🔑 نام کاربری:", username);
        HBox emailRow = createInfoRow("📧 ایمیل:", email);
        HBox phoneRow = createInfoRow("📱 تلفن:", phone);
        HBox levelRow = createInfoRow("🎯 سطح دسترسی:", getPersianLevel(level));

        infoCard.getChildren().addAll(
                receiptIdRow, dateRow, timeRow, issuerRow,
                separator, nameRow, userRow, emailRow, phoneRow, levelRow
        );

        // دکمه‌ها
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button copyInfoBtn = new Button("📋 کپی اطلاعات");
        styleReceiptButton(copyInfoBtn, Color.web("#00b894"));
        copyInfoBtn.setOnAction(e -> {
            copyReceiptToClipboard(fullname, username, email, phone, level);
            showCustomAlert("✅ اطلاعات کپی شد",
                    "تمام اطلاعات فیش به کلیپ‌بورد کپی شد.\n" +
                            "می‌توانید در Word یا هر ویرایشگر متنی پیست کرده و چاپ کنید.",
                    SUCCESS_COLOR);
        });

        Button saveTextBtn = new Button("💾 ذخیره متن");
        styleReceiptButton(saveTextBtn, Color.web("#0984e3"));
        saveTextBtn.setOnAction(e -> {
            saveReceiptAsText(fullname, username, email, phone, level, receiptStage);
        });

        Button closeBtn = new Button("✅ تایید و بستن");
        styleReceiptButton(closeBtn, Color.web("#00cec9"));
        closeBtn.setOnAction(e -> receiptStage.close());

        buttonBox.getChildren().addAll(copyInfoBtn, saveTextBtn, closeBtn);

        // پیام پایین
        Label footerNote = new Label("⚠️ این فیش به عنوان سند رسمی ثبت ادمین محسوب می‌شود.");
        footerNote.setStyle("-fx-text-fill: #ffcc00; -fx-font-size: 11px; -fx-font-style: italic;");
        footerNote.setAlignment(Pos.CENTER);

        // جمع کردن محتوا
        receiptContent.getChildren().addAll(
                headerBox, infoCard, buttonBox, footerNote
        );

        // تنظیم صحنه
        Scene scene = new Scene(receiptContent, 500, 650);
        scene.setFill(Color.TRANSPARENT);
        receiptStage.setScene(scene);

        // دکمه بستن اختصاصی
        HBox closeButtonBox = new HBox();
        closeButtonBox.setAlignment(Pos.TOP_RIGHT);
        closeButtonBox.setPadding(new Insets(10));

        Button customCloseBtn = new Button("✕");
        customCloseBtn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-text-fill: white; -fx-background-color: transparent; " +
                "-fx-cursor: hand; -fx-padding: 5 10;");
        customCloseBtn.setOnAction(e -> receiptStage.close());

        closeButtonBox.getChildren().add(customCloseBtn);

        StackPane rootPane = new StackPane();
        rootPane.getChildren().addAll(receiptContent, closeButtonBox);
        scene.setRoot(rootPane);

        receiptStage.showAndWait();
    }

    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelLbl = new Label(label);
        labelLbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px; -fx-font-weight: bold;");
        labelLbl.setPrefWidth(120);

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        valueLbl.setWrapText(true);

        row.getChildren().addAll(labelLbl, valueLbl);
        return row;
    }

    private String getPersianLevel(String level) {
        switch (level.toLowerCase()) {
            case "superadmin": return "سوپرادمین";
            case "manager": return "مدیر";
            case "operator": return "اپراتور";
            default: return level;
        }
    }

    private void styleReceiptButton(Button btn, Color color) {
        String baseStyle = "-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 10; " +
                "-fx-background-color: " + toRgbString(color) + "; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 8 15; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, " + toRgbaString(color, 0.5) + ", 5, 0.3, 0, 2);";

        btn.setStyle(baseStyle);
        btn.setPrefHeight(35);

        btn.setOnMouseEntered(e -> btn.setStyle(baseStyle +
                " -fx-scale-x: 1.05; -fx-scale-y: 1.05; " +
                "-fx-effect: dropshadow(gaussian, " + toRgbaString(color, 0.7) + ", 8, 0.4, 0, 3);"));

        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    }

    private void copyReceiptToClipboard(String fullname, String username, String email,
                                        String phone, String level) {
        String receiptText = String.format(
                "📋 فیش ثبت ادمین جدید - VWM System\n" +
                        "──────────────────────────────\n" +
                        "شماره فیش: %d\n" +
                        "تاریخ: %s\n" +
                        "زمان: %s\n" +
                        "صادرکننده: %s\n" +
                        "──────────────────────────────\n" +
                        "👤 نام کامل: %s\n" +
                        "🔑 نام کاربری: %s\n" +
                        "📧 ایمیل: %s\n" +
                        "📱 تلفن: %s\n" +
                        "🎯 سطح دسترسی: %s\n" +
                        "──────────────────────────────\n" +
                        "⚠️ این فیش به عنوان سند رسمی ثبت ادمین محسوب می‌شود.\n" +
                        "تاریخ تولید: %s",
                System.currentTimeMillis(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                currentAdmin.getFullname(),
                fullname,
                username,
                email,
                phone,
                getPersianLevel(level),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
        );

        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(receiptText);
        clipboard.setContent(content);
    }

    private void saveReceiptAsText(String fullname, String username, String email,
                                   String phone, String level, Stage receiptStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("ذخیره فیش به عنوان فایل متنی");
        fileChooser.setInitialFileName("receipt_admin_" + username + "_" +
                LocalDateTime.now().format(timestampFormatter) + ".txt");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                String receiptText = String.format(
                        "فیش ثبت ادمین جدید - VWM System\n" +
                                "================================\n" +
                                "شماره فیش: %d\n" +
                                "تاریخ: %s\n" +
                                "زمان: %s\n" +
                                "صادرکننده: %s\n" +
                                "================================\n" +
                                "نام کامل: %s\n" +
                                "نام کاربری: %s\n" +
                                "ایمیل: %s\n" +
                                "تلفن: %s\n" +
                                "سطح دسترسی: %s\n" +
                                "================================\n" +
                                "این فیش به عنوان سند رسمی ثبت ادمین محسوب می‌شود.\n" +
                                "تاریخ تولید: %s\n",
                        System.currentTimeMillis(),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                        currentAdmin.getFullname(),
                        fullname,
                        username,
                        email,
                        phone,
                        getPersianLevel(level),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
                );

                // ذخیره فایل متنی
                java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8");
                writer.print(receiptText);
                writer.close();

                showCustomAlert("💾 ذخیره موفق",
                        "فیش با موفقیت ذخیره شد:\n" + file.getAbsolutePath() +
                                "\n\nمی‌توانید این فایل را چاپ کنید یا به PDF تبدیل کنید.",
                        SUCCESS_COLOR);

            } catch (Exception e) {
                e.printStackTrace();
                showCustomAlert("❌ خطا در ذخیره",
                        "خطا در ذخیره فایل:\n" + e.getMessage(),
                        ERROR_COLOR);
            }
        }
    }

    private void showCustomAlert(String title, String message, Color color) {
        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL);
        alertStage.initStyle(StageStyle.UNDECORATED);
        alertStage.setResizable(false);

        VBox alertContent = new VBox(20);
        alertContent.setPadding(new Insets(30));
        alertContent.setAlignment(Pos.CENTER);
        alertContent.setStyle("-fx-background-color: rgba(22, 33, 62, 0.95); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: " + toRgbString(color) + "; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 15;");
        alertContent.setEffect(new DropShadow(20, Color.BLACK));

        // آیکون بر اساس نوع
        String icon = "ℹ️";
        if (title.contains("❌")) icon = "❌";
        else if (title.contains("✅")) icon = "✅";
        else if (title.contains("💾")) icon = "💾";
        else if (title.contains("📋")) icon = "📋";

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 40px;");

        Label titleLabel = new Label(title.replace("❌", "").replace("✅", "").replace("💾", "").replace("📋", "").trim());
        titleLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);

        TextArea messageArea = new TextArea(message);
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setPrefHeight(120);
        messageArea.setStyle("-fx-font-size: 13px; -fx-text-fill: white; " +
                "-fx-background-color: rgba(255,255,255,0.05); " +
                "-fx-border-color: rgba(255,255,255,0.1); " +
                "-fx-background-radius: 8; -fx-border-radius: 8;");

        Button okBtn = new Button("تایید");
        okBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-background-color: " + toRgbString(color) + "; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 8 25; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, " + toRgbaString(color, 0.5) + ", 5, 0.3, 0, 2);");
        okBtn.setOnAction(e -> alertStage.close());

        alertContent.getChildren().addAll(iconLabel, titleLabel, messageArea, okBtn);

        Scene scene = new Scene(alertContent, 400, 300);
        scene.setFill(Color.TRANSPARENT);
        alertStage.setScene(scene);
        alertStage.showAndWait();
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);

        // Add some styling based on status type
        if (color.equals(SUCCESS_COLOR)) {
            statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-background-color: rgba(144, 238, 144, 0.1); " +
                    "-fx-background-radius: 8; -fx-padding: 10; " +
                    "-fx-border-color: rgba(144, 238, 144, 0.3); " +
                    "-fx-border-width: 1; -fx-border-radius: 8;");
        } else if (color.equals(ERROR_COLOR)) {
            statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-background-color: rgba(255, 107, 107, 0.1); " +
                    "-fx-background-radius: 8; -fx-padding: 10; " +
                    "-fx-border-color: rgba(255, 107, 107, 0.3); " +
                    "-fx-border-width: 1; -fx-border-radius: 8;");
        } else {
            statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-background-color: rgba(255, 204, 0, 0.1); " +
                    "-fx-background-radius: 8; -fx-padding: 10; " +
                    "-fx-border-color: rgba(255, 204, 0, 0.3); " +
                    "-fx-border-width: 1; -fx-border-radius: 8;");
        }
    }

    private void clearFields() {
        fullnameField.clear();
        usernameField.clear();
        passwordField.clear();
        emailField.clear();
        phoneField.clear();
        levelCombo.setValue(null);

        // Reset focus
        fullnameField.requestFocus();
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

    public Parent getRoot() {
        return root;
    }
}