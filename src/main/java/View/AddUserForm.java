package View;

import api.ApiClient;
import api.Logger;
import com.example.vwm.Main;
import com.example.vwm.SceneManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.effect.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AddUserForm {

    private StackPane root;
    private Admin currentAdmin;
    private ApiClient api = new ApiClient();

    // Main containers
    private VBox stepContentContainer;
    private HBox stepperContainer;

    // Fields
    private TextField fullnameField, usernameField, phoneField, emailField;
    private PasswordField passwordField;
    private TextArea addressField, deviceLocationField;
    private TextField companyNameField, ceoNameField, ceoPhoneField, ceoEmailField;

    // UI Components
    private Button nextBtn, prevBtn, saveBtn;
    private int currentStep = 1;
    private int totalSteps = 4;

    // Button and background management
    private List<ApiClient.ButtonItem> availableButtons = new ArrayList<>();
    private List<ApiClient.ButtonItem> selectedButtons = new ArrayList<>();
    private List<ApiClient.BackgroundImage> availableBackgrounds = new ArrayList<>();
    private String selectedBackground = "default_bg.jpg";
    private VBox selectedButtonsContainer;
    private VBox backgroundsContainer;

    // Colors
    private static final Color PRIMARY_COLOR = Color.web("#4f46e5");
    private static final Color SECONDARY_COLOR = Color.web("#7c3aed");
    private static final Color ACCENT_COLOR = Color.web("#10b981");
    private static final Color WARNING_COLOR = Color.web("#f59e0b");
    private static final Color ERROR_COLOR = Color.web("#ef4444");
    private static final Color SUCCESS_COLOR = Color.web("#22c55e");
    private static final Color DARK_BG = Color.web("#0f172a");
    private static final Color CARD_BG = Color.web("#1e293b");
    private static final Color TEXT_PRIMARY = Color.web("#f8fafc");
    private static final Color TEXT_SECONDARY = Color.web("#cbd5e1");
    private static final Color BORDER_COLOR = Color.web("#334155");

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private int createdUserId = -1;
    private FileChooser fileChooser;

    public AddUserForm(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.fileChooser = new FileChooser();
        configureFileChooser();
        createUI();
        loadData();
    }

    private void configureFileChooser() {
        fileChooser.setTitle("انتخاب عکس پس‌زمینه");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("تصاویر", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("همه فایل‌ها", "*.*")
        );
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #0f172a;");

        // Main card
        VBox mainCard = createMainCard();
        root.getChildren().add(mainCard);
    }

    private void loadData() {
        scheduler.submit(() -> {
            // Load available buttons
            List<ApiClient.ButtonItem> buttons = api.getDefaultButtons();
            Platform.runLater(() -> {
                if (buttons.isEmpty()) {
                    availableButtons = new ArrayList<>();
                } else {
                    availableButtons = buttons;
                }
            });

            // Load available backgrounds
            List<ApiClient.BackgroundImage> backgrounds = api.getBackgroundImages();
            Platform.runLater(() -> {
                if (backgrounds.isEmpty()) {
                    // Add default background
                    ApiClient.BackgroundImage defaultBg = new ApiClient.BackgroundImage();
                    defaultBg.id = 0;
                    defaultBg.filename = "default_bg.jpg";
                    defaultBg.isDefault = true;
                    availableBackgrounds.add(defaultBg);
                } else {
                    availableBackgrounds = backgrounds;
                    // Set default background
                    for (ApiClient.BackgroundImage bg : availableBackgrounds) {
                        if (bg.isDefault) {
                            selectedBackground = bg.filename;
                            break;
                        }
                    }
                }
            });
        });
    }

    private VBox createMainCard() {
        VBox card = new VBox();
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setMaxSize(900, 700);

        // Card background
        Rectangle cardBg = new Rectangle(900, 700);
        cardBg.setArcWidth(20);
        cardBg.setArcHeight(20);
        cardBg.setFill(CARD_BG);
        cardBg.setOpacity(0.95);
        cardBg.setEffect(new DropShadow(30, Color.BLACK));

        StackPane cardContainer = new StackPane();
        cardContainer.getChildren().addAll(cardBg, createCardContent());

        card.getChildren().add(cardContainer);
        return card;
    }

    private VBox createCardContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);
        content.setMaxSize(860, 640);

        // Header
        HBox header = createHeader();

        // Stepper
        stepperContainer = createStepper();

        // Step content container
        stepContentContainer = new VBox();
        stepContentContainer.setAlignment(Pos.TOP_CENTER);
        stepContentContainer.setPrefHeight(420);
        stepContentContainer.setMaxHeight(420);

        // Load initial step
        loadStepContent(1);

        // Navigation
        HBox navigation = createNavigation();

        content.getChildren().addAll(header, stepperContainer, stepContentContainer, navigation);
        return content;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        // Back button
        Button backBtn = createIconButton("←", PRIMARY_COLOR);
        backBtn.setOnAction(e -> {
            AdminPanel panel = new AdminPanel(currentAdmin);
            SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), panel.getRoot());
        });

        // Title
        VBox titleBox = new VBox(3);
        Label mainTitle = new Label("ایجاد کاربر جدید");
        mainTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 24));
        mainTitle.setTextFill(TEXT_PRIMARY);

        Label subtitle = new Label("افزودن کاربر، تنظیم پس‌زمینه و انتخاب دکمه‌ها");
        subtitle.setFont(Font.font("Tahoma", 13));
        subtitle.setTextFill(TEXT_SECONDARY);

        titleBox.getChildren().addAll(mainTitle, subtitle);

        HBox.setHgrow(titleBox, Priority.ALWAYS);
        header.getChildren().addAll(backBtn, titleBox);

        return header;
    }

    private HBox createStepper() {
        HBox stepper = new HBox(5);
        stepper.setAlignment(Pos.CENTER);
        stepper.setPadding(new Insets(10, 0, 20, 0));

        String[] stepNames = {"اطلاعات اصلی", "پس‌زمینه", "دکمه‌ها", "تایید"};
        String[] stepIcons = {"👤", "🖼️", "🖱️", "✅"};

        for (int i = 1; i <= totalSteps; i++) {
            VBox step = createStepBox(i, stepIcons[i-1], stepNames[i-1]);
            stepper.getChildren().add(step);

            if (i < totalSteps) {
                Rectangle connector = new Rectangle(40, 2);
                connector.setFill(i < currentStep ? PRIMARY_COLOR : BORDER_COLOR);
                stepper.getChildren().add(connector);
            }
        }

        return stepper;
    }

    private VBox createStepBox(int stepNumber, String icon, String title) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(5));

        // Step circle
        StackPane circleContainer = new StackPane();
        circleContainer.setMinSize(50, 50);

        Circle outerCircle = new Circle(25);
        outerCircle.setFill(stepNumber <= currentStep ? PRIMARY_COLOR : Color.TRANSPARENT);
        outerCircle.setStroke(stepNumber <= currentStep ? PRIMARY_COLOR : BORDER_COLOR);
        outerCircle.setStrokeWidth(2);

        Circle innerCircle = new Circle(20);
        innerCircle.setFill(stepNumber == currentStep ? Color.WHITE :
                stepNumber < currentStep ? PRIMARY_COLOR : CARD_BG);

        Label stepIcon = new Label(icon);
        stepIcon.setFont(Font.font("Tahoma", FontWeight.BOLD, 14));
        stepIcon.setTextFill(stepNumber == currentStep ? PRIMARY_COLOR :
                stepNumber < currentStep ? Color.WHITE : TEXT_SECONDARY);

        circleContainer.getChildren().addAll(outerCircle, innerCircle, stepIcon);

        // Step label
        Label stepTitle = new Label(title);
        stepTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
        stepTitle.setTextFill(stepNumber <= currentStep ? TEXT_PRIMARY : TEXT_SECONDARY);
        stepTitle.setAlignment(Pos.CENTER);

        box.getChildren().addAll(circleContainer, stepTitle);

        // Add click navigation
        box.setOnMouseClicked(e -> {
            if (stepNumber < currentStep) {
                navigateToStep(stepNumber);
            }
        });

        return box;
    }

    private void loadStepContent(int step) {
        stepContentContainer.getChildren().clear();

        switch (step) {
            case 1:
                stepContentContainer.getChildren().add(createStep1());
                break;
            case 2:
                stepContentContainer.getChildren().add(createStep2());
                break;
            case 3:
                stepContentContainer.getChildren().add(createStep3());
                break;
            case 4:
                stepContentContainer.getChildren().add(createStep4());
                break;
        }

        // Animate content change
        FadeTransition fade = new FadeTransition(Duration.millis(300), stepContentContainer);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private VBox createStep1() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("اطلاعات پایه کاربر");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 18));
        title.setTextFill(TEXT_PRIMARY);
        title.setPadding(new Insets(0, 0, 10, 0));

        // Grid layout
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setPadding(new Insets(10));

        // Row 1
        fullnameField = createFormField("نام کامل");
        usernameField = createFormField("نام کاربری");

        grid.add(createFieldWithLabel("👤 نام کامل", fullnameField), 0, 0);
        grid.add(createFieldWithLabel("🔐 نام کاربری", usernameField), 1, 0);

        // Row 2
        passwordField = createPasswordField("رمز عبور");
        phoneField = createFormField("شماره تلفن");

        grid.add(createFieldWithLabel("🔒 رمز عبور", passwordField), 0, 1);
        grid.add(createFieldWithLabel("📱 تلفن همراه", phoneField), 1, 1);

        // Row 3
        emailField = createFormField("ایمیل");
        deviceLocationField = createTextAreaField("محل نصب دستگاه", 2);
        deviceLocationField.setStyle("-fx-text-alignment: rgba(34,32,32,0.77);-fx-text-fill: rgba(34,32,32,0.77);-fx-font-weight: bold;-fx-font-size: 14px;");

        grid.add(createFieldWithLabel("📧 ایمیل", emailField), 0, 2);
        grid.add(createFieldWithLabel("📍 محل دستگاه", deviceLocationField), 1, 2);

        // Row 4 - Address
        addressField = createTextAreaField("آدرس کامل", 3);
        addressField.setStyle("-fx-text-alignment: rgba(34,32,32,0.77);-fx-text-fill: rgba(34,32,32,0.77);-fx-font-weight: bold;-fx-font-size: 14px;");
        VBox addressContainer = createFieldWithLabel("🏠 آدرس کامل", addressField);
        GridPane.setColumnSpan(addressContainer, 2);
        grid.add(addressContainer, 0, 3);

        // Row 5 - Company info (optional)
        companyNameField = createFormField("نام شرکت");
        ceoNameField = createFormField("نام مدیرعامل");
        ceoPhoneField = createFormField("شماره تماس مدیر");
        ceoEmailField = createFormField("ایمیل مدیر");

        grid.add(createFieldWithLabel("🏢 نام شرکت (اختیاری)", companyNameField), 0, 4);
        grid.add(createFieldWithLabel("👨‍💼 نام مدیرعامل (اختیاری)", ceoNameField), 1, 4);
//        grid.add(createFieldWithLabel("شماره تماس مدیر(اختیاری)", ceoPhoneField), 2, 4);
//        grid.add(createFieldWithLabel("ایمیل مدیر(اختیاری)", ceoEmailField), 3, 4);

        container.getChildren().addAll(title, grid);
        return container;
    }

    private VBox createStep2() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("تنظیم عکس پس‌زمینه");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 18));
        title.setTextFill(TEXT_PRIMARY);
        title.setPadding(new Insets(0, 0, 10, 0));

        Label description = new Label("یک عکس پس‌زمینه از لیست انتخاب کنید یا عکس جدید آپلود کنید:");
        description.setFont(Font.font("Tahoma", 13));
        description.setTextFill(TEXT_SECONDARY);
        description.setAlignment(Pos.CENTER);

        // Upload button
        HBox uploadBox = new HBox(10);
        uploadBox.setAlignment(Pos.CENTER);

        Button uploadBtn = new Button("📤 آپلود عکس جدید");
        uploadBtn.setStyle("-fx-font-size: 13px; " +
                "-fx-background-color: rgba(79,70,229,0.1); " +
                "-fx-text-fill: #4f46e5; " +
                "-fx-border-color: #4f46e5; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 10 20; " +
                "-fx-cursor: hand;");
        uploadBtn.setOnAction(e -> uploadBackgroundImage());

        Label uploadHint = new Label("فرمت‌های مجاز: PNG, JPG, JPEG, GIF");
        uploadHint.setFont(Font.font("Tahoma", 11));
        uploadHint.setTextFill(TEXT_SECONDARY);

        uploadBox.getChildren().addAll(uploadBtn, uploadHint);

        // Backgrounds container
        backgroundsContainer = new VBox(10);
        backgroundsContainer.setAlignment(Pos.TOP_CENTER);
        backgroundsContainer.setPrefHeight(280);

        // Load backgrounds
        loadBackgrounds();

        container.getChildren().addAll(title, description, uploadBox, backgroundsContainer);
        return container;
    }

    private void uploadBackgroundImage() {
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            // Show loading
            StackPane loadingOverlay = createLoadingOverlay("در حال آپلود عکس...");
            root.getChildren().add(loadingOverlay);

            scheduler.submit(() -> {
                String uploadedFilename = api.uploadBackgroundImage(file);

                Platform.runLater(() -> {
                    root.getChildren().remove(loadingOverlay);

                    if (uploadedFilename != null) {
                        // Add new background to list
                        ApiClient.BackgroundImage newBg = new ApiClient.BackgroundImage();
                        newBg.id = 0; // Temporary ID
                        newBg.filename = uploadedFilename;
                        newBg.isDefault = false;

                        availableBackgrounds.add(newBg);
                        selectedBackground = uploadedFilename;

                        // Update display
                        loadBackgrounds();

                        showToast("✅ عکس با موفقیت آپلود شد: " + uploadedFilename, SUCCESS_COLOR);
                    } else {
                        showToast("❌ خطا در آپلود عکس", ERROR_COLOR);
                    }
                });
            });
        }
    }

    private void loadBackgrounds() {
        backgroundsContainer.getChildren().clear();

        if (availableBackgrounds.isEmpty()) {
            Label emptyLabel = new Label("در حال بارگذاری عکس‌ها...");
            emptyLabel.setFont(Font.font("Tahoma", 14));
            emptyLabel.setTextFill(TEXT_SECONDARY);
            backgroundsContainer.getChildren().add(emptyLabel);
        } else {
            updateBackgroundsDisplay();
        }
    }

    private void updateBackgroundsDisplay() {
        backgroundsContainer.getChildren().clear();

        FlowPane backgroundsGrid = new FlowPane();
        backgroundsGrid.setHgap(15);
        backgroundsGrid.setVgap(15);
        backgroundsGrid.setAlignment(Pos.CENTER);
        backgroundsGrid.setPrefWrapLength(500);

        for (ApiClient.BackgroundImage bg : availableBackgrounds) {
            VBox bgCard = createBackgroundCard(bg);
            backgroundsGrid.getChildren().add(bgCard);
        }

        backgroundsContainer.getChildren().add(backgroundsGrid);
    }

    private VBox createBackgroundCard(ApiClient.BackgroundImage bg) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10;");

        // Check if selected
        boolean isSelected = bg.filename.equals(selectedBackground);
        if (isSelected) {
            card.setStyle("-fx-background-color: rgba(16,185,129,0.1); " +
                    "-fx-border-color: #10b981; -fx-border-width: 1; -fx-border-radius: 10;");
        }

        // Preview container
        StackPane previewContainer = new StackPane();
        previewContainer.setPrefSize(110, 80);
        previewContainer.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 5;");

        // Background preview
        Rectangle preview = new Rectangle(110, 80);
        preview.setArcWidth(5);
        preview.setArcHeight(5);

        // Generate gradient based on filename
        LinearGradient gradient = generateGradientFromName(bg.filename);
        preview.setFill(gradient);

        // Default badge
        if (bg.isDefault) {
            Label defaultBadge = new Label("⚡");
            defaultBadge.setStyle("-fx-font-size: 12px;");
            StackPane.setAlignment(defaultBadge, Pos.TOP_LEFT);
            StackPane.setMargin(defaultBadge, new Insets(3, 0, 0, 3));
            previewContainer.getChildren().add(defaultBadge);
        }

        // Selection indicator
        Circle selectionCircle = new Circle(8);
        selectionCircle.setFill(isSelected ? ACCENT_COLOR : Color.TRANSPARENT);
        selectionCircle.setStroke(isSelected ? ACCENT_COLOR : BORDER_COLOR);
        selectionCircle.setStrokeWidth(2);

        previewContainer.getChildren().addAll(preview, selectionCircle);
        StackPane.setAlignment(selectionCircle, Pos.TOP_RIGHT);
        StackPane.setMargin(selectionCircle, new Insets(5, 5, 0, 0));

        // Background info
        VBox infoBox = new VBox(2);
        infoBox.setAlignment(Pos.CENTER);

        String displayName = bg.filename.length() > 15 ? bg.filename.substring(0, 12) + "..." : bg.filename;
        Label nameLabel = new Label(displayName);
        nameLabel.setFont(Font.font("Tahoma", 11));
        nameLabel.setTextFill(isSelected ? ACCENT_COLOR : TEXT_SECONDARY);
        nameLabel.setMaxWidth(100);
        nameLabel.setWrapText(true);

        if (bg.isDefault) {
            Label defaultLabel = new Label("(پیش‌فرض)");
            defaultLabel.setFont(Font.font("Tahoma", 9));
            defaultLabel.setTextFill(WARNING_COLOR);
            infoBox.getChildren().addAll(nameLabel, defaultLabel);
        } else {
            infoBox.getChildren().add(nameLabel);
        }

        card.getChildren().addAll(previewContainer, infoBox);

        // Click handler
        card.setOnMouseClicked(e -> {
            selectedBackground = bg.filename;
            updateBackgroundsDisplay();
            showToast("پس‌زمینه انتخاب شد: " + bg.filename, ACCENT_COLOR);
        });

        // Hover effect
        card.setOnMouseEntered(e -> {
            if (!isSelected) {
                card.setStyle("-fx-background-color: rgba(79,70,229,0.1); -fx-background-radius: 10;");
            }
        });

        card.setOnMouseExited(e -> {
            if (!isSelected) {
                card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10;");
            } else {
                card.setStyle("-fx-background-color: rgba(16,185,129,0.1); " +
                        "-fx-border-color: #10b981; -fx-border-width: 1; -fx-border-radius: 10;");
            }
        });

        return card;
    }

    private LinearGradient generateGradientFromName(String name) {
        int hash = Math.abs(name.hashCode());
        Color color1 = Color.hsb(hash % 360, 0.7, 0.8);
        Color color2 = Color.hsb((hash + 60) % 360, 0.7, 0.6);

        return new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, color1),
                new Stop(1, color2)
        );
    }

    private VBox createStep3() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("انتخاب دکمه‌های دستگاه");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 18));
        title.setTextFill(TEXT_PRIMARY);

        Label description = new Label("دکمه‌های مورد نیاز برای دستگاه وندینگ را انتخاب کنید:");
        description.setFont(Font.font("Tahoma", 13));
        description.setTextFill(TEXT_SECONDARY);
        description.setAlignment(Pos.CENTER);

        // Selected buttons container
        selectedButtonsContainer = new VBox(10);
        selectedButtonsContainer.setAlignment(Pos.TOP_CENTER);
        selectedButtonsContainer.setPrefHeight(300);
        selectedButtonsContainer.setMaxHeight(300);

        // Load buttons
        loadButtons();

        container.getChildren().addAll(title, description, selectedButtonsContainer);
        return container;
    }

    private void loadButtons() {
        selectedButtonsContainer.getChildren().clear();

        if (availableButtons.isEmpty()) {
            showButtonsLoading();

            // Try to load buttons
            scheduler.submit(() -> {
                List<ApiClient.ButtonItem> buttons = api.getDefaultButtons();
                Platform.runLater(() -> {
                    if (buttons.isEmpty()) {
                        showNoButtonsMessage();
                    } else {
                        availableButtons = buttons;
                        updateButtonsDisplay();
                    }
                });
            });
        } else {
            updateButtonsDisplay();
        }
    }

    private void showButtonsLoading() {
        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(20));

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4f46e5;");

        Label loadingLabel = new Label("در حال بارگذاری دکمه‌ها...");
        loadingLabel.setFont(Font.font("Tahoma", 14));
        loadingLabel.setTextFill(TEXT_SECONDARY);

        loadingBox.getChildren().addAll(loadingIndicator, loadingLabel);
        selectedButtonsContainer.getChildren().add(loadingBox);
    }

    private void showNoButtonsMessage() {
        selectedButtonsContainer.getChildren().clear();

        VBox messageBox = new VBox(15);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(20));

        Label warningIcon = new Label("⚠️");
        warningIcon.setStyle("-fx-font-size: 40px;");

        Label message = new Label("هیچ دکمه‌ای یافت نشد");
        message.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        message.setTextFill(WARNING_COLOR);

        Label description = new Label("در حال حاضر دکمه‌ای برای انتخاب وجود ندارد.\nمی‌توانید کاربر را بدون دکمه ایجاد کنید.");
        description.setFont(Font.font("Tahoma", 13));
        description.setTextFill(TEXT_SECONDARY);
        description.setTextAlignment(TextAlignment.CENTER);
        description.setWrapText(true);

        Button continueBtn = new Button("ادامه بدون دکمه");
        continueBtn.setStyle("-fx-font-size: 13px; -fx-background-color: #f59e0b; " +
                "-fx-text-fill: white; -fx-padding: 8 15; -fx-cursor: hand;");
        continueBtn.setOnAction(e -> {
            selectedButtons.clear();
            nextStep();
        });

        messageBox.getChildren().addAll(warningIcon, message, description, continueBtn);
        selectedButtonsContainer.getChildren().add(messageBox);
    }

    private void updateButtonsDisplay() {
        selectedButtonsContainer.getChildren().clear();

        if (availableButtons.isEmpty()) {
            showNoButtonsMessage();
            return;
        }

        FlowPane buttonsGrid = new FlowPane();
        buttonsGrid.setHgap(15);
        buttonsGrid.setVgap(15);
        buttonsGrid.setAlignment(Pos.CENTER);
        buttonsGrid.setPrefWrapLength(500);

        for (ApiClient.ButtonItem button : availableButtons) {
            HBox buttonCard = createButtonCard(button);
            buttonsGrid.getChildren().add(buttonCard);
        }

        selectedButtonsContainer.getChildren().add(buttonsGrid);
    }

    private HBox createButtonCard(ApiClient.ButtonItem button) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10;");
        card.setPrefWidth(220);

        // Check if button is selected
        boolean isSelected = selectedButtons.stream()
                .anyMatch(b -> b.getId() == button.getId());

        if (isSelected) {
            card.setStyle("-fx-background-color: rgba(16,185,129,0.1); " +
                    "-fx-border-color: #10b981; -fx-border-width: 1; -fx-border-radius: 10;");
        }

        // Icon/Emoji
        String emoji = getEmojiForButton(button.getTitle());
        Label iconLabel = new Label(emoji);
        iconLabel.setStyle("-fx-font-size: 20px;");

        // Button info
        VBox infoBox = new VBox(3);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label titleLabel = new Label(button.getTitle());
        titleLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
        titleLabel.setTextFill(isSelected ? ACCENT_COLOR : TEXT_PRIMARY);

        HBox detailsBox = new HBox(8);
        Label priceLabel = new Label("💰 " + (int)button.getPrice() + " تومان");
        Label stockLabel = new Label("📦 " + button.getStock());

        priceLabel.setFont(Font.font("Tahoma", 10));
        priceLabel.setTextFill(isSelected ? ACCENT_COLOR : TEXT_SECONDARY);
        stockLabel.setFont(Font.font("Tahoma", 10));
        stockLabel.setTextFill(isSelected ? ACCENT_COLOR : TEXT_SECONDARY);

        detailsBox.getChildren().addAll(priceLabel, stockLabel);
        infoBox.getChildren().addAll(titleLabel, detailsBox);

        // Selection indicator
        Circle selectionCircle = new Circle(6);
        selectionCircle.setFill(isSelected ? ACCENT_COLOR : Color.TRANSPARENT);
        selectionCircle.setStroke(isSelected ? ACCENT_COLOR : BORDER_COLOR);
        selectionCircle.setStrokeWidth(1);

        card.getChildren().addAll(iconLabel, infoBox, selectionCircle);

        // Click handler
        card.setOnMouseClicked(e -> {
            if (isSelected) {
                selectedButtons.removeIf(b -> b.getId() == button.getId());
            } else {
                selectedButtons.add(button);
            }
            updateButtonsDisplay();
        });

        // Hover effect
        card.setOnMouseEntered(e -> {
            if (!isSelected) {
                card.setStyle("-fx-background-color: rgba(79,70,229,0.1); -fx-background-radius: 10;");
            }
        });

        card.setOnMouseExited(e -> {
            if (!isSelected) {
                card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10;");
            } else {
                card.setStyle("-fx-background-color: rgba(16,185,129,0.1); " +
                        "-fx-border-color: #10b981; -fx-border-width: 1; -fx-border-radius: 10;");
            }
        });

        return card;
    }

    private String getEmojiForButton(String title) {
        if (title.contains("قهوه") || title.contains("اسپرسو") || title.contains("کاپوچینو") || title.contains("لاته")) {
            return "☕";
        } else if (title.contains("چای")) {
            return "🍵";
        } else if (title.contains("نوشابه") || title.contains("کوکا") || title.contains("پپسی")) {
            return "🥤";
        } else if (title.contains("آبمیوه") || title.contains("آب") || title.contains("نوشیدنی")) {
            return "🧃";
        } else if (title.contains("شکلات")) {
            return "🍫";
        } else if (title.contains("کیک") || title.contains("کاپ کیک") || title.contains("پیراشکی")) {
            return "🍰";
        } else if (title.contains("شیر")) {
            return "🥛";
        } else if (title.contains("بستنی")) {
            return "🍦";
        } else if (title.contains("ساندویچ") || title.contains("ساندویچ")) {
            return "🥪";
        }
        return "🖱️";
    }

    private VBox createStep4() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("مرور و تایید نهایی");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 18));
        title.setTextFill(TEXT_PRIMARY);

        // Summary card
        VBox summaryCard = new VBox(15);
        summaryCard.setPadding(new Insets(20));
        summaryCard.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-background-radius: 15;");
        summaryCard.setMaxWidth(500);

        // User info
        VBox userSection = createSummarySection("👤 اطلاعات کاربر",
                "نام: " + fullnameField.getText(),
                "نام کاربری: " + usernameField.getText(),
                "ایمیل: " + emailField.getText(),
                "تلفن: " + phoneField.getText(),
                "محل دستگاه: " + deviceLocationField.getText()
        );

        // Background
        VBox bgSection = createSummarySection("🖼️ عکس پس‌زمینه",
                "عکس انتخاب شده: " + selectedBackground,
                availableBackgrounds.stream()
                        .filter(bg -> bg.filename.equals(selectedBackground))
                        .findFirst()
                        .map(bg -> bg.isDefault ? "(پیش‌فرض سیستم)" : "(آپلود شده)")
                        .orElse("")
        );

        // Buttons
        String buttonStatus = selectedButtons.isEmpty() ?
                "⚠️ هیچ دکمه‌ای انتخاب نشده" :
                "✅ " + selectedButtons.size() + " دکمه انتخاب شده";

        VBox buttonsSection = createSummarySection("🖱️ دکمه‌ها",
                buttonStatus
        );

        // Show selected buttons if any
        if (!selectedButtons.isEmpty()) {
            VBox buttonsList = new VBox(5);
            for (int i = 0; i < Math.min(selectedButtons.size(), 5); i++) {
                ApiClient.ButtonItem btn = selectedButtons.get(i);
                Label btnLabel = new Label("• " + btn.getTitle() + " - " + (int)btn.getPrice() + " تومان");
                btnLabel.setFont(Font.font("Tahoma", 11));
                btnLabel.setTextFill(TEXT_SECONDARY);
                buttonsList.getChildren().add(btnLabel);
            }

            if (selectedButtons.size() > 5) {
                Label moreLabel = new Label("... و " + (selectedButtons.size() - 5) + " دکمه دیگر");
                moreLabel.setFont(Font.font("Tahoma", 11));
                moreLabel.setTextFill(TEXT_SECONDARY);
                buttonsList.getChildren().add(moreLabel);
            }

            buttonsSection.getChildren().add(buttonsList);
        }

        summaryCard.getChildren().addAll(userSection, bgSection, buttonsSection);

        container.getChildren().addAll(title, summaryCard);
        return container;
    }

    private VBox createSummarySection(String title, String... items) {
        VBox section = new VBox(8);
        section.setPadding(new Insets(0, 0, 10, 0));

        Label sectionTitle = new Label(title);
        sectionTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 14));
        sectionTitle.setTextFill(PRIMARY_COLOR);

        VBox itemsBox = new VBox(3);
        for (String item : items) {
            if (item == null || item.trim().isEmpty()) continue;

            Label itemLabel = new Label("• " + item);
            itemLabel.setFont(Font.font("Tahoma", 13));
            itemLabel.setTextFill(TEXT_SECONDARY);
            itemLabel.setWrapText(true);
            itemsBox.getChildren().add(itemLabel);
        }

        section.getChildren().addAll(sectionTitle, itemsBox);
        return section;
    }

    private HBox createNavigation() {
        HBox nav = new HBox(15);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(20, 0, 0, 0));

        prevBtn = createNavButton("⏪ مرحله قبل", SECONDARY_COLOR);
        prevBtn.setOnAction(e -> previousStep());
        prevBtn.setDisable(true);

        nextBtn = createNavButton("مرحله بعد ⏩", PRIMARY_COLOR);
        nextBtn.setOnAction(e -> nextStep());

        saveBtn = createNavButton("💾 ذخیره کاربر", ACCENT_COLOR);
        saveBtn.setOnAction(e -> saveUser());
        saveBtn.setVisible(false);

        nav.getChildren().addAll(prevBtn, nextBtn, saveBtn);
        return nav;
    }

    private void nextStep() {
        if (!validateCurrentStep()) return;

        if (currentStep < totalSteps) {
            currentStep++;
            updateUI();
        }
    }

    private void previousStep() {
        if (currentStep > 1) {
            currentStep--;
            updateUI();
        }
    }

    private void navigateToStep(int step) {
        if (step < 1 || step > totalSteps) return;

        for (int i = 1; i < step; i++) {
            if (!validateStep(i)) {
                showToast("لطفاً مراحل قبلی را تکمیل کنید", ERROR_COLOR);
                return;
            }
        }

        currentStep = step;
        updateUI();
    }

    private void updateUI() {
        // Update stepper
        stepperContainer.getChildren().clear();
        String[] stepNames = {"اطلاعات اصلی", "پس‌زمینه", "دکمه‌ها", "تایید"};
        String[] stepIcons = {"👤", "🖼️", "🖱️", "✅"};

        for (int i = 1; i <= totalSteps; i++) {
            VBox step = createStepBox(i, stepIcons[i-1], stepNames[i-1]);
            stepperContainer.getChildren().add(step);

            if (i < totalSteps) {
                Rectangle connector = new Rectangle(40, 2);
                connector.setFill(i < currentStep ? PRIMARY_COLOR : BORDER_COLOR);
                stepperContainer.getChildren().add(connector);
            }
        }

        // Update content
        loadStepContent(currentStep);

        // Update navigation
        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        prevBtn.setDisable(currentStep == 1);

        if (currentStep == totalSteps) {
            nextBtn.setVisible(false);
            saveBtn.setVisible(true);
        } else {
            nextBtn.setVisible(true);
            saveBtn.setVisible(false);
        }
    }

    private boolean validateCurrentStep() {
        return validateStep(currentStep);
    }

    private boolean validateStep(int step) {
        switch (step) {
            case 1:
                return validateStep1();
            case 2:
                return validateStep2();
            case 3:
                return true; // Buttons can be empty
            case 4:
                return validateAllSteps();
            default:
                return true;
        }
    }

    private boolean validateStep1() {
        StringBuilder errors = new StringBuilder();

        if (fullnameField.getText().trim().isEmpty()) {
            errors.append("• نام کامل را وارد کنید\n");
            highlightField(fullnameField);
        }

        if (usernameField.getText().trim().isEmpty()) {
            errors.append("• نام کاربری را وارد کنید\n");
            highlightField(usernameField);
        }

        if (passwordField.getText().length() < 6) {
            errors.append("• رمز عبور باید حداقل ۶ کاراکتر باشد\n");
            highlightField(passwordField);
        }

        if (!emailField.getText().contains("@") || !emailField.getText().contains(".")) {
            errors.append("• ایمیل معتبر وارد کنید\n");
            highlightField(emailField);
        }

        if (phoneField.getText().trim().isEmpty()) {
            errors.append("• شماره تلفن را وارد کنید\n");
            highlightField(phoneField);
        }

        if (deviceLocationField.getText().trim().isEmpty()) {
            errors.append("• محل دستگاه را مشخص کنید\n");
            highlightField(deviceLocationField);
        }

        if (errors.length() > 0) {
            showToast("خطاها:\n" + errors.toString(), ERROR_COLOR);
            return false;
        }

        return true;
    }

    private boolean validateStep2() {
        // Background is required but we always have at least default
        if (selectedBackground == null || selectedBackground.trim().isEmpty()) {
            showToast("لطفاً یک عکس پس‌زمینه انتخاب کنید", WARNING_COLOR);
            return false;
        }
        return true;
    }

    private boolean validateAllSteps() {
        return validateStep1() && validateStep2();
    }

    private void highlightField(Control field) {
        String originalStyle = field.getStyle();
        field.setStyle(originalStyle + "-fx-border-color: #ef4444 !important;");

        scheduler.schedule(() -> {
            Platform.runLater(() -> field.setStyle(originalStyle));
        }, 3, TimeUnit.SECONDS);
    }

    private void saveUser() {
        if (!validateAllSteps()) return;

        // Show loading
        StackPane loadingOverlay = createLoadingOverlay("در حال ذخیره کاربر...");
        root.getChildren().add(loadingOverlay);
        saveBtn.setDisable(true);

        // ذخیره در Thread جداگانه
        new Thread(() -> {
            try {
                System.out.println("=== شروع فرآیند ذخیره کاربر ===");
                System.out.println("Username: " + usernameField.getText().trim());
                System.out.println("Fullname: " + fullnameField.getText().trim());

                // ✅ بررسی null برای تمام فیلدها
                String companyNameValue = (companyNameField != null) ? companyNameField.getText().trim() : "";
                String ceoNameValue = (ceoNameField != null) ? ceoNameField.getText().trim() : "";
                String ceoPhoneValue = (ceoPhoneField != null) ? ceoPhoneField.getText().trim() : "";
                String ceoEmailValue = (ceoEmailField != null) ? ceoEmailField.getText().trim() : "";
                String addressValue = (addressField != null) ? addressField.getText().trim() : "";

                boolean userSaved = api.addUser(
                        fullnameField.getText().trim(),
                        usernameField.getText().trim(),
                        passwordField.getText().trim(),
                        phoneField.getText().trim(),
                        emailField.getText().trim(),
                        addressValue,
                        companyNameValue,
                        ceoNameValue,
                        ceoPhoneValue,
                        ceoEmailValue,
                        deviceLocationField.getText().trim(),
                        currentAdmin.getUsername()
                );

                System.out.println("User saved result: " + userSaved);

                if (!userSaved) {
                    Platform.runLater(() -> {
                        root.getChildren().remove(loadingOverlay);
                        saveBtn.setDisable(false);
                        showToast("❌ خطا در ثبت کاربر. ممکن است نام کاربری تکراری باشد یا خطای سرور.", ERROR_COLOR);
                    });
                    return;
                }

                // مکث کوتاه برای اطمینان از ثبت در دیتابیس
                Thread.sleep(500);

                // Get the created user ID
                int userId = api.getLastUserId();
                final int createdUserId = userId;

                System.out.println("Last User ID: " + createdUserId);

                if (createdUserId == -1 || createdUserId == 0) {
                    Platform.runLater(() -> {
                        root.getChildren().remove(loadingOverlay);
                        saveBtn.setDisable(false);
                        showToast("❌ خطا در دریافت شناسه کاربر", ERROR_COLOR);
                    });
                    return;
                }

                // Set background image
                boolean bgSet = api.setUserBackground(createdUserId, selectedBackground);
                System.out.println("Background set result: " + bgSet);

                if (!bgSet) {
                    Platform.runLater(() ->
                            showToast("⚠️ خطا در تنظیم پس‌زمینه، اما کاربر ایجاد شد", WARNING_COLOR)
                    );
                }

                // Assign buttons to user
                if (!selectedButtons.isEmpty()) {
                    int successCount = 0;
                    for (ApiClient.ButtonItem button : selectedButtons) {
                        boolean assigned = api.assignButtonToUser(createdUserId, button.getId());
                        System.out.println("Button " + button.getId() + " assigned: " + assigned);
                        if (assigned) {
                            successCount++;
                        }
                        Thread.sleep(100); // مکث کوتاه بین درخواست‌ها
                    }

                    if (successCount < selectedButtons.size()) {
                        final int failedCount = selectedButtons.size() - successCount;
                        Platform.runLater(() ->
                                showToast("⚠️ " + failedCount + " دکمه به کاربر اختصاص داده نشد", WARNING_COLOR)
                        );
                    }
                }

                // Log the action
                try {
                    Logger.log(
                            currentAdmin.getUsername(),
                            "Add User",
                            "User added: " + usernameField.getText() +
                                    ", Background: " + selectedBackground +
                                    ", Buttons: " + selectedButtons.size(),
                            usernameField.getText(),
                            "User",
                            "Info"
                    );
                } catch (Exception e) {
                    System.err.println("Log error: " + e.getMessage());
                }

                Platform.runLater(() -> {
                    root.getChildren().remove(loadingOverlay);
                    saveBtn.setDisable(false);
                    this.createdUserId = createdUserId;
                    showSuccessDialog();
                });

            } catch (Exception e) {
                System.err.println("Error in saveUser thread: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    root.getChildren().remove(loadingOverlay);
                    saveBtn.setDisable(false);
                    showToast("❌ خطا: " + e.getMessage(), ERROR_COLOR);
                });
            }
        }).start();
    }

    private void assignButtonsToUser() {
        if (selectedButtons.isEmpty() || createdUserId == -1) return;

        int successCount = 0;
        for (ApiClient.ButtonItem button : selectedButtons) {
            boolean assigned = api.assignButtonToUser(createdUserId, button.getId());
            if (assigned) {
                successCount++;
            }
        }

        if (successCount < selectedButtons.size()) {
            showToast("⚠️ برخی دکمه‌ها به کاربر اختصاص داده نشدند", WARNING_COLOR);
        }
    }

    private StackPane createLoadingOverlay(String message) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(Pos.CENTER);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setStyle("-fx-progress-color: #10b981;");

        Label loadingLabel = new Label(message);
        loadingLabel.setFont(Font.font("Tahoma", 14));
        loadingLabel.setTextFill(Color.WHITE);

        loadingBox.getChildren().addAll(progress, loadingLabel);
        overlay.getChildren().add(loadingBox);

        return overlay;
    }

    private void showSuccessDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #1e293b; " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: #10b981; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 15;");
        content.setEffect(new DropShadow(20, Color.BLACK));

        // Success icon
        Label successIcon = new Label("✅");
        successIcon.setStyle("-fx-font-size: 40px;");

        // Message
        VBox messageBox = new VBox(10);
        messageBox.setAlignment(Pos.CENTER);

        Label title = new Label("کاربر با موفقیت ایجاد شد!");
        title.setFont(Font.font("Tahoma", FontWeight.BOLD, 18));
        title.setTextFill(TEXT_PRIMARY);

        String detailsText = "نام کاربری: " + usernameField.getText() + "\n" +
                "پس‌زمینه: " + selectedBackground + "\n" +
                "تعداد دکمه‌ها: " + selectedButtons.size() + "\n" +
                "شناسه کاربر: " + createdUserId;

        Label details = new Label(detailsText);
        details.setFont(Font.font("Tahoma", 13));
        details.setTextFill(TEXT_SECONDARY);
        details.setTextAlignment(TextAlignment.CENTER);

        messageBox.getChildren().addAll(title, details);

        // Button
        Button okBtn = createActionButton("✅ بازگشت به پنل", ACCENT_COLOR);
        okBtn.setOnAction(e -> {
            dialog.close();
            AdminPanel panel = new AdminPanel(currentAdmin);
            SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), panel.getRoot());
        });

        content.getChildren().addAll(successIcon, messageBox, okBtn);

        Scene scene = new Scene(content, 400, 300);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showToast(String message, Color color) {
        Platform.runLater(() -> {
            Label toast = new Label(message);
            toast.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
            toast.setTextFill(Color.WHITE);
            toast.setPadding(new Insets(10, 20, 10, 20));
            toast.setAlignment(Pos.CENTER);
            toast.setStyle("-fx-background-color: " + toRgbString(color) + ";" +
                    "-fx-background-radius: 20;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0.5, 0, 3);");

            StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
            StackPane.setMargin(toast, new Insets(0, 0, 20, 0));
            root.getChildren().add(toast);

            // Animate
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
            fadeOut.setDelay(Duration.seconds(3));
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> root.getChildren().remove(toast));

            fadeIn.play();
            fadeOut.play();
        });
    }

    // UI Helper Methods
    private TextField createFormField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(getTextFieldStyle());
        field.setPrefHeight(40);
        field.setPrefWidth(200);
        return field;
    }

    private String getTextFieldStyle() {
        return "-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #334155;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;" +
                "-fx-text-fill: #f8fafc;" +
                "-fx-padding: 10 15;" +
                "-fx-font-size: 13px;";
    }

    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setStyle(getPasswordFieldStyle());
        field.setPrefHeight(40);
        return field;
    }

    private String getPasswordFieldStyle() {
        return "-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #334155;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;" +
                "-fx-text-fill: #f8fafc;" +
                "-fx-padding: 10 15;" +
                "-fx-font-size: 13px;";
    }

    private TextArea createTextAreaField(String prompt, int rows) {
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefRowCount(rows);
        area.setWrapText(true);
        area.setStyle(getTextAreaStyle());
        return area;
    }

    private String getTextAreaStyle() {
        return "-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #334155;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;" +
                "-fx-text-fill: #f8fafc;" +
                "-fx-padding: 10 15;" +
                "-fx-font-size: 13px;" +
                "-fx-control-inner-background: transparent;";
    }

    private VBox createFieldWithLabel(String labelText, Control field) {
        VBox container = new VBox(5);

        Label label = new Label(labelText);
        label.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
        label.setTextFill(TEXT_SECONDARY);

        container.getChildren().addAll(label, field);
        return container;
    }

    private Button createIconButton(String icon, Color color) {
        Button btn = new Button(icon);
        btn.setStyle("-fx-font-size: 16px; " +
                "-fx-background-color: " + toRgbString(color) + ";" +
                "-fx-background-radius: 8;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 8;" +
                "-fx-cursor: hand;");

        return btn;
    }

    private Button createNavButton(String text, Color color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-background-color: " + toRgbString(color) + ";" +
                "-fx-text-fill: white; " +
                "-fx-padding: 10 20; " +
                "-fx-cursor: hand;");

        return btn;
    }

    private Button createActionButton(String text, Color color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-background-color: " + toRgbString(color) + ";" +
                "-fx-text-fill: white; " +
                "-fx-padding: 8 15; " +
                "-fx-cursor: hand;");

        return btn;
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