package View;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import com.example.vwm.SceneManager;
import com.example.vwm.Main;
import ui.LoginPage;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AdminPanel {

    private Admin currentAdmin;
    private VBox root;
    private static final Color PRIMARY_COLOR = Color.web("#4e9cff");
    private static final Color SECONDARY_COLOR = Color.web("#1c72ff");
    private static final Color ACCENT_COLOR = Color.web("#00ffaa");
    private static final Color BACKGROUND_COLOR = Color.web("#1a1a2e");
    private static final Color CARD_COLOR = Color.web("#16213e");

    public AdminPanel(String username, String fullname, String email, String phone, String level) {
        this.currentAdmin = new Admin(username, fullname, email, phone, level);
        createUI();
    }

    public AdminPanel(Admin admin) {
        this.currentAdmin = admin;
        createUI();
    }

    private void createUI() {
        // Background with gradient
        StackPane backgroundPane = new StackPane();

        // Main gradient background
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

        // Animated particles effect (simulated with dots)
        Pane particlesPane = new Pane();
        for (int i = 0; i < 30; i++) {
            Rectangle particle = new Rectangle(2, 2, Color.web("rgba(78,156,255,0.3)"));
            particle.setX(Math.random() * 1200);
            particle.setY(Math.random() * 800);
            particlesPane.getChildren().add(particle);
        }

        backgroundPane.getChildren().addAll(bgRect, particlesPane);

        // Main content container
        root = new VBox(13);
        root.setPadding(new Insets(20, 40, 40, 40));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: transparent;");

        // Header Section
//        HBox header = createHeader();

        // User Info Card
        HBox userInfoCard = createUserInfoCard();

        // Dashboard Title
//        Label dashboardTitle = new Label("داشبورد مدیریت");
//        dashboardTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 14));
//        dashboardTitle.setTextFill(Color.WHITE);
//        dashboardTitle.setEffect(new DropShadow(20, PRIMARY_COLOR));

        // Features Container
        VBox featuresContainer = new VBox(13);
        featuresContainer.setAlignment(Pos.CENTER);
        featuresContainer.setPadding(new Insets(20, 0, 40, 0));

        // Based on access level
        switch (currentAdmin.getLevel().toLowerCase()) {
            case "superadmin":
                featuresContainer.getChildren().addAll(
                        createSectionTitle("قابلیت‌های سوپرادمین"),
                        createSuperAdminDashboard()
                );
                break;
            case "manager":
                featuresContainer.getChildren().addAll(
                        createSectionTitle("قابلیت‌های مدیر"),
                        createManagerDashboard()
                );
                break;
            case "operator":
                featuresContainer.getChildren().addAll(
                        createSectionTitle("قابلیت‌های اپراتور"),
                        createOperatorDashboard()
                );
                break;
            default:
                featuresContainer.getChildren().add(createBasicAccessMessage());
                break;
        }

        // Bottom Navigation
        HBox bottomNav = createBottomNavigation();

        // Assemble everything
        root.getChildren().addAll(
//                header,
                userInfoCard,
//                dashboardTitle,
                featuresContainer,
                bottomNav
        );

        // Set background
        StackPane mainContainer = new StackPane();
        mainContainer.getChildren().addAll(backgroundPane, root);
        this.root = new VBox(mainContainer);
    }

    private HBox createUserInfoCard() {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15, 25, 15, 25));
        card.setStyle("-fx-background-color: rgba(22, 33, 62, 0.9); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: rgba(78, 156, 255, 0.3); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 15;");
        card.setEffect(new DropShadow(15, Color.BLACK));

        // User icon
        Label userIcon = new Label("👨‍💼");
        userIcon.setStyle("-fx-font-size: 40px;");

        // User info
        VBox infoBox = new VBox(5);

        Label nameLabel = new Label(currentAdmin.getFullname());
        nameLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.WHITE);

        Label roleLabel = new Label("سطح دسترسی: " + getRoleBadge(currentAdmin.getLevel()));
        roleLabel.setFont(Font.font("Tahoma", 14));
        roleLabel.setTextFill(ACCENT_COLOR);

        HBox contactInfo = new HBox(20);
        Label emailLabel = new Label("📧 " + currentAdmin.getEmail());
        Label phoneLabel = new Label("📱 " + currentAdmin.getPhone());
        emailLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");
        phoneLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");
        contactInfo.getChildren().addAll(emailLabel, phoneLabel);

        infoBox.getChildren().addAll(nameLabel, roleLabel, contactInfo);

        card.getChildren().addAll(userIcon, infoBox);
        return card;
    }

    private String getRoleBadge(String level) {
        switch (level.toLowerCase()) {
            case "superadmin": return "🔴 سوپرادمین";
            case "manager": return "🟡 مدیر";
            case "operator": return "🟢 اپراتور";
            default: return "⚪ کاربر";
        }
    }

    private Label createSectionTitle(String title) {
        Label sectionTitle = new Label(title);
        sectionTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));
        sectionTitle.setTextFill(Color.WHITE);
        sectionTitle.setStyle("-fx-padding: 0 0 10 0;");

        // Underline effect
        Rectangle underline = new Rectangle(200, 3);
        underline.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, PRIMARY_COLOR), new Stop(1, ACCENT_COLOR)));
        underline.setArcWidth(10);
        underline.setArcHeight(10);

        StackPane titleContainer = new StackPane();
        titleContainer.getChildren().addAll(sectionTitle, underline);
        StackPane.setAlignment(underline, Pos.BOTTOM_CENTER);

        return sectionTitle;
    }

    private GridPane createSuperAdminDashboard() {
        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(25);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));

        // Row 1
        grid.add(createFeatureCard("👥 مدیریت ادمین‌ها",
                "مدیریت کامل حساب‌های ادمین",
                createButton("➕ اضافه کردن ادمین", "admin", e -> goToAddAdmin()),
                createButton("✏️ ویرایش ادمین‌ها", "admin", e -> goToManageAdmins())
        ), 0, 0);

        grid.add(createFeatureCard("👤 مدیریت کاربران",
                "مدیریت کاربران سیستم",
                createButton("➕ کاربر جدید", "user", e -> goToAddUser()),
                createButton("✏️ ویرایش کاربران", "user", e -> goToEditUser())
        ), 1, 0);

        grid.add(createFeatureCard("🖥️ مدیریت دستگاه‌ها",
                "کنترل دستگاه‌های VWM",
                createButton("➕ دستگاه جدید", "device", e -> goToAddDevice()),
                createButton("✏️ ویرایش دستگاه‌ها", "device", e -> goToEditDevice())
        ), 2, 0);

        // Row 2
        grid.add(createFeatureCard("📦 مدیریت محصولات",
                "تنظیمات محصولات و دکمه‌ها",
                createButton("⚙️ ویرایش محصولات", "product", e -> goToEditProducts())
        ), 0, 1);

        grid.add(createFeatureCard("📊 گزارش‌های سیستمی",
                "مشاهده لاگ‌ها و گزارشات",
                createButton("📈 لاگ ورودها", "report", e -> viewLoginsLog()),
                createButton("🔐 لاگ ادمین‌ها", "report", e -> viewAdminLog()),
                createButton("💰 لاگ فروش", "report", e -> viewSales())
        ), 1, 1);

        grid.add(createFeatureCard("⚙️ تنظیمات سیستم",
                "تنظیمات پیشرفته سیستم",
                createButton("🎛️ تنظیمات", "settings", e -> showSystemSettings())
        ), 2, 1);

        return grid;
    }

    private GridPane createManagerDashboard() {
        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(25);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));

        grid.add(createFeatureCard("👤 مدیریت کاربران",
                "مدیریت کاربران سیستم",
                createButton("✏️ ویرایش کاربران", "user", e -> goToEditUser()),
                createButton("🔍 جستجوی کاربر", "user", e -> viewUsers())
        ), 0, 0);

        grid.add(createFeatureCard("🖥️ دستگاه‌ها",
                "مشاهده و مدیریت دستگاه‌ها",
                createButton("✏️ ویرایش دستگاه‌ها", "device", e -> goToEditDevice()),
                createButton("📱 مشاهده وضعیت", "device", e -> viewDevices())
        ), 1, 0);

        grid.add(createFeatureCard("📊 گزارش‌ها",
                "گزارش‌های مدیریتی",
                createButton("📈 گزارش فروش", "report", e -> viewReports()),
                createButton("📦 موجودی انبار", "report", e -> viewInventory())
        ), 2, 0);

        return grid;
    }

    private GridPane createOperatorDashboard() {
        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(25);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));

        grid.add(createFeatureCard("👤 کاربران",
                "مشاهده اطلاعات کاربران",
                createButton("🔍 جستجوی کاربر", "user", e -> viewUsers()),
                createButton("📋 لیست کاربران", "user", e -> viewUserList())
        ), 0, 0);

        grid.add(createFeatureCard("🖥️ دستگاه‌ها",
                "مشاهده وضعیت دستگاه‌ها",
                createButton("📊 وضعیت آنلاین", "device", e -> viewDevices()),
                createButton("⚠️ هشدارها", "device", e -> viewAlerts())
        ), 1, 0);

        return grid;
    }

    private VBox createBasicAccessMessage() {
        VBox messageBox = new VBox(20);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(40));
        messageBox.setStyle("-fx-background-color: rgba(255,87,87,0.1); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: #ff5757; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 15;");

        Label warningIcon = new Label("⚠️");
        warningIcon.setStyle("-fx-font-size: 50px;");

        Label message = new Label("دسترسی محدود");
        message.setFont(Font.font("Tahoma", FontWeight.BOLD, 24));
        message.setTextFill(Color.web("#ff5757"));

        Label description = new Label("حساب کاربری شما دارای دسترسی ویژه‌ای نمی‌باشد.\nبرای دسترسی بیشتر با مدیر سیستم تماس بگیرید.");
        description.setFont(Font.font("Tahoma", 16));
        description.setTextFill(Color.WHITE);
        description.setAlignment(Pos.CENTER);

        messageBox.getChildren().addAll(warningIcon, message, description);
        return messageBox;
    }

    private VBox createFeatureCard(String title, String description, Button... buttons) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: rgba(22, 33, 62, 0.8); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: rgba(78, 156, 255, 0.2); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 15;");
        card.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.5)));
        card.setPrefWidth(280);
        card.setPrefHeight(220);

        // Card header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String[] parts = title.split(" ", 2);
        if (parts.length > 0) {
            Label icon = new Label(parts[0]);
            icon.setStyle("-fx-font-size: 24px;");
            header.getChildren().add(icon);
        }

        if (parts.length > 1) {
            Label titleLabel = new Label(parts[1]);
            titleLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 18));
            titleLabel.setTextFill(Color.WHITE);
            header.getChildren().add(titleLabel);
        }

        // Description
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Tahoma", 13));
        descLabel.setTextFill(Color.web("#aaaaaa"));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        // Buttons container
        VBox buttonContainer = new VBox(8);
        buttonContainer.setAlignment(Pos.CENTER);
        for (Button btn : buttons) {
            buttonContainer.getChildren().add(btn);
        }

        card.getChildren().addAll(header, descLabel, buttonContainer);

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() +
                    "-fx-border-color: rgba(78, 156, 255, 0.6); " +
                    "-fx-effect: dropshadow(gaussian, rgba(78,156,255,0.3), 25, 0.5, 0, 0);");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: rgba(22, 33, 62, 0.8); " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: rgba(78, 156, 255, 0.2); " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 15;");
        });

        return card;
    }

    private Button createButton(String text, String type, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button(text);

        Color btnColor;
        switch (type) {
            case "admin": btnColor = Color.web("#ff6b6b"); break;
            case "user": btnColor = Color.web("#4ecdc4"); break;
            case "device": btnColor = Color.web("#45b7d1"); break;
            case "product": btnColor = Color.web("#96ceb4"); break;
            case "report": btnColor = Color.web("#ffeaa7"); break;
            case "settings": btnColor = Color.web("#a29bfe"); break;
            default: btnColor = PRIMARY_COLOR;
        }

        String baseStyle = "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 10; " +
                "-fx-background-color: " + toRgbString(btnColor) + "; " +
                "-fx-text-fill: #1a1a2e; " +
                "-fx-padding: 8 16; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, " + toRgbaString(btnColor, 0.5) + ", 5, 0.3, 0, 2);";

        btn.setStyle(baseStyle);
        btn.setPrefWidth(200);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(baseStyle +
                    " -fx-scale-x: 1.05; -fx-scale-y: 1.05; " +
                    "-fx-effect: dropshadow(gaussian, " + toRgbaString(btnColor, 0.8) + ", 8, 0.5, 0, 3);");
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
        });

        btn.setOnAction(action);
        return btn;
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

    private HBox createBottomNavigation() {
        HBox bottomNav = new HBox(15); // فاصله کمتر
        bottomNav.setAlignment(Pos.CENTER);
        bottomNav.setPadding(new Insets(15, 0, 15, 0)); // padding کمتر بالا و پایین

        // دکمه بازگشت به لاگین
        Button backBtn = createNavButton("↩️ بازگشت به ورود", "nav", e -> {
            LoginPage loginPage = new LoginPage();
            SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), loginPage.getPage());
        });

        // دکمه خروج از برنامه
        Button exitBtn = createNavButton("❌ خروج از برنامه", "logout", e -> {
            System.exit(0); // بستن کامل برنامه
        });

        bottomNav.getChildren().addAll(backBtn, exitBtn);
        return bottomNav;
    }

    private Button createNavButton(String text, String type, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button(text);

        String baseStyle = "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 25; " +
                "-fx-padding: 12 25; " +
                "-fx-cursor: hand; ";

        if (type.equals("logout")) {
            baseStyle += "-fx-background-color: linear-gradient(to right, #ff416c, #ff4b2b); " +
                    "-fx-text-fill: white; " +
                    "-fx-effect: dropshadow(gaussian, rgba(255,65,108,0.5), 10, 0.4, 0, 3);";
        } else {
            baseStyle += "-fx-background-color: rgba(255,255,255,0.1); " +
                    "-fx-text-fill: white; " +
                    "-fx-border-color: rgba(78,156,255,0.5); " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 25;";
        }

        btn.setStyle(baseStyle);

        String finalBaseStyle = baseStyle;
        btn.setOnMouseEntered(e -> {
            if (type.equals("logout")) {
                btn.setStyle(finalBaseStyle + " -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
            } else {
                btn.setStyle("-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 25; " +
                        "-fx-padding: 12 25; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-color: rgba(78,156,255,0.3); " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: rgba(78,156,255,0.8); " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 25; " +
                        "-fx-scale-x: 1.05; -fx-scale-y: 1.05;");
            }
        });

        String finalBaseStyle1 = baseStyle;
        btn.setOnMouseExited(e -> {
            btn.setStyle(finalBaseStyle1);
        });

        btn.setOnAction(action);
        return btn;
    }

    // ------------------- Navigation Methods -------------------
    private void goToAddAdmin() {
        AddAdminForm form = new AddAdminForm(currentAdmin);
        SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), form.getRoot());
    }

    private void goToManageAdmins() {
        ManageAdminsPage page = new ManageAdminsPage(currentAdmin);
        SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), page.getRoot());
    }

    private void goToAddUser() {
        AddUserForm form = new AddUserForm(currentAdmin);
        SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), form.getRoot());
    }
    private void goToEditUser() {
        ManageUsersPage page = new ManageUsersPage(currentAdmin);
        SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), page.getRoot());
    }
    private void goToAddDevice() {
        AddUserForm form = new AddUserForm(currentAdmin);
        SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), form.getRoot());
    }
    private void goToEditDevice() {
        ManageUsersPage page = new ManageUsersPage(currentAdmin);
        SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), page.getRoot());
    }
    private void goToEditProducts() {
        ManageProductsPage page = new ManageProductsPage(currentAdmin);
        SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), page.getRoot());
    }
    private void viewReports() { /* فرم مشاهده گزارش */ }
    private void viewLoginsLog() {
        UserLoginLogsPage page = new UserLoginLogsPage(currentAdmin);
        SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), page.getRoot());
    }

    private void viewAdminLog(){
        AdminLogsPage page = new AdminLogsPage(currentAdmin);
        SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), page.getRoot());
    }

    private void viewSales() {
        SalesLogsPage page = new SalesLogsPage(currentAdmin);
        SceneManager.switchToPanel(Main.getInstance().getPrimaryStage(), page.getRoot());
    }
    private void viewUsers() { /* مشاهده کاربران */ }
    private void viewDevices() { /* مشاهده وضعیت دستگاه‌ها */ }
    private void viewInventory() { /* مشاهده موجودی */ }
    private void viewAlerts() { /* مشاهده هشدارها */ }
    private void viewUserList() { /* لیست کاربران */ }
    private void showSystemSettings() { /* تنظیمات سیستم */ }
    private void showProfileSettings() { /* تنظیمات پروفایل */ }

    public Parent getRoot() {
        return root;
    }
}