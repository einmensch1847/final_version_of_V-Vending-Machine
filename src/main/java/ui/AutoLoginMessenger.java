package ui;

import api.SecureApiClientV2;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;
import java.util.*;

public class AutoLoginMessenger {

    private Stage primaryStage;
    private BorderPane root;
    private SecureApiClientV2 secureApi;
    private ProgressIndicator autoLoginProgress;
    private boolean autoLoginAttempted = false;

    // پالت رنگ آبی آسمانی حرفه‌ای
    private static final Color PRIMARY_BLUE = Color.web("#1e88e5");
    private static final Color LIGHT_BLUE = Color.web("#42a5f5");
    private static final Color SKY_BLUE = Color.web("#90caf9");
    private static final Color BACKGROUND = Color.web("#f5f7fa");
    private static final Color CARD_WHITE = Color.web("#ffffff");
    private static final Color TEXT_PRIMARY = Color.web("#2c3e50");
    private static final Color TEXT_SECONDARY = Color.web("#546e7a");
    private static final Color SUCCESS = Color.web("#4caf50");
    private static final Color WARNING = Color.web("#ff9800");
    private static final Color ERROR = Color.web("#f44336");
    private static final Color BORDER = Color.web("#e0e0e0");

    public AutoLoginMessenger(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.secureApi = new SecureApiClientV2();
        initUI();

        // تلاش برای لاگین خودکار بعد از تأخیر کوتاه
        Timeline autoLoginCheck = new Timeline(
                new KeyFrame(Duration.seconds(1.5), e -> attemptAutoLogin())
        );
        autoLoginCheck.play();
    }

    private void initUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + toHex(BACKGROUND) + ";");

        // هدر تمیز و حرفه‌ای
        HBox header = createHeader();
        root.setTop(header);

        // کانتینر اصلی با سایه ملایم
        StackPane mainContainer = new StackPane();
        mainContainer.setPadding(new Insets(20));

        // کارت لاگین
        VBox loginCard = createLoginCard();
        loginCard.setVisible(false);

        // صفحه استارت اپ
        VBox startupScreen = createStartupScreen();

        mainContainer.getChildren().addAll(startupScreen, loginCard);
        root.setCenter(mainContainer);

        // فوتر با لینک‌های مفید
        HBox footer = createFooter();
        root.setBottom(footer);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(20, 40, 20, 40));
        header.setStyle("-fx-background-color: white; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");
        header.setAlignment(Pos.CENTER_LEFT);

        // لوگوی حرفه‌ای
        HBox logoBox = new HBox(12);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        // دایره لوگو
        StackPane logoContainer = new StackPane();
        Circle logoCircle = new Circle(24);
        logoCircle.setFill(PRIMARY_BLUE);

        Label logoText = new Label("VW");
        logoText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        logoText.setTextFill(Color.WHITE);

        logoContainer.getChildren().addAll(logoCircle, logoText);

        // متن لوگو
        VBox logoTextContainer = new VBox(2);
        Label appName = new Label("VWM Messenger");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        appName.setTextFill(TEXT_PRIMARY);

        Label appTagline = new Label("ارتباط امن، بدون مرز");
        appTagline.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        appTagline.setTextFill(TEXT_SECONDARY);

        logoTextContainer.getChildren().addAll(appName, appTagline);
        logoBox.getChildren().addAll(logoContainer, logoTextContainer);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // وضعیت امنیت
        HBox securityBadge = createSecurityBadge();

        header.getChildren().addAll(logoBox, spacer, securityBadge);
        return header;
    }

    private HBox createSecurityBadge() {
        HBox badge = new HBox(8);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-background-color: #e8f5e9; " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 6 15;");

        Circle dot = new Circle(4);
        dot.setFill(SUCCESS);

        Label status = new Label("امنیت فعال");
        status.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        status.setTextFill(SUCCESS);

        badge.getChildren().addAll(dot, status);
        return badge;
    }

    private VBox createStartupScreen() {
        VBox screen = new VBox(30);
        screen.setAlignment(Pos.CENTER);
        screen.setMaxWidth(600);
        screen.setPadding(new Insets(50, 40, 50, 40));
        screen.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 25, 0, 0, 5);");

        // آیکون متحرک
        StackPane iconContainer = new StackPane();

        // دایره‌های متحدالمرکز
        Circle outerCircle = new Circle(80);
        outerCircle.setFill(SKY_BLUE);
        outerCircle.setOpacity(0.2);

        Circle middleCircle = new Circle(60);
        middleCircle.setFill(LIGHT_BLUE);
        middleCircle.setOpacity(0.4);

        Circle innerCircle = new Circle(40);
        innerCircle.setFill(PRIMARY_BLUE);

        // آیکون پیام
        Label messageIcon = new Label("💬");
        messageIcon.setFont(Font.font(36));
        messageIcon.setTextFill(Color.WHITE);

        iconContainer.getChildren().addAll(outerCircle, middleCircle, innerCircle, messageIcon);

        // انیمیشن پالس
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), outerCircle);
        pulse.setFromX(1); pulse.setFromY(1);
        pulse.setToX(1.1); pulse.setToY(1.1);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        // متن خوش‌آمدگویی
        VBox textContainer = new VBox(15);
        textContainer.setAlignment(Pos.CENTER);

        Label welcome = new Label("به پیام‌رسان VWM خوش آمدید");
        welcome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        welcome.setTextFill(TEXT_PRIMARY);

        Label subtitle = new Label("سیستم در حال بررسی ورود خودکار است...");
        subtitle.setFont(Font.font("Segoe UI", 16));
        subtitle.setTextFill(TEXT_SECONDARY);

        textContainer.getChildren().addAll(welcome, subtitle);

        // نوار پیشرفت
        autoLoginProgress = new ProgressIndicator();
        autoLoginProgress.setPrefSize(50, 50);
        autoLoginProgress.setStyle("-fx-progress-color: " + toHex(PRIMARY_BLUE) + ";");

        // دکمه‌های کنترلی
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        Button manualLoginBtn = createPrimaryButton("ورود دستی");
        manualLoginBtn.setOnAction(e -> showLoginCard());

        Button exploreBtn = createSecondaryButton("آشنایی با ویژگی‌ها");
        exploreBtn.setOnAction(e -> showFeatures());

        buttonContainer.getChildren().addAll(manualLoginBtn, exploreBtn);

        screen.getChildren().addAll(iconContainer, textContainer,
                autoLoginProgress, buttonContainer);
        return screen;
    }

    private VBox createLoginCard() {
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);
        card.setPadding(new Insets(40, 35, 40, 35));
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 4); " +
                "-fx-border-color: " + toHex(BORDER) + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 16;");

        // هدر کارت
        VBox header = new VBox(12);
        header.setAlignment(Pos.CENTER);

        Label loginIcon = new Label("🔐");
        loginIcon.setFont(Font.font(32));

        Label title = new Label("ورود به حساب کاربری");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(TEXT_PRIMARY);

        header.getChildren().addAll(loginIcon, title);

        // فرم ورود
        VBox form = createLoginForm();

        // چک‌باکس‌ها
        HBox options = new HBox();
        options.setAlignment(Pos.CENTER_LEFT);

        CheckBox rememberMe = new CheckBox("مرا به خاطر بسپار");
        styleCheckBox(rememberMe);

        Hyperlink forgotPass = new Hyperlink("رمز عبور را فراموش کرده‌ام");
        forgotPass.setStyle("-fx-text-fill: " + toHex(PRIMARY_BLUE) + "; " +
                "-fx-font-size: 13px; " +
                "-fx-border-color: transparent; " +
                "-fx-cursor: hand;");
        forgotPass.setOnAction(e -> showForgotPassword());

        options.getChildren().addAll(rememberMe, new Region(), forgotPass);
        HBox.setHgrow(options.getChildren().get(1), Priority.ALWAYS);

        // دکمه ورود
        Button loginBtn = createPrimaryButton("ورود به حساب");
        loginBtn.setPrefWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(48);

        // خط جداکننده - اینجا مشکل داشت
        Node separator = createTextSeparator("یا");

        // دکمه‌های جایگزین
        HBox altButtons = new HBox(10);
        altButtons.setAlignment(Pos.CENTER);

        Button guestBtn = createOutlineButton("ورود مهمان");
        guestBtn.setOnAction(e -> showGuestModeInfo());

        Button signupBtn = createSecondaryButton("ایجاد حساب");
        signupBtn.setOnAction(e -> showRegistration());

        altButtons.getChildren().addAll(guestBtn, signupBtn);

        // لینک قوانین
        Hyperlink termsLink = new Hyperlink("با ورود، قوانین و شرایط را می‌پذیرید");
        termsLink.setStyle("-fx-text-fill: " + toHex(TEXT_SECONDARY) + "; " +
                "-fx-font-size: 12px; " +
                "-fx-border-color: transparent; " +
                "-fx-cursor: hand;");
        termsLink.setOnAction(e -> showTermsAndConditions());

        // اضافه کردن همه childها به کارت
        card.getChildren().addAll(
                header,
                form,
                options,
                loginBtn,
                separator,
                altButtons,
                termsLink
        );

        return card;
    }

    private Node createTextSeparator(String text) {
        HBox separatorBox = new HBox(10);
        separatorBox.setAlignment(Pos.CENTER);
        separatorBox.setPrefHeight(30);

        Separator leftSep = new Separator();
        leftSep.setPrefWidth(80);
        leftSep.setStyle("-fx-background-color: " + toHex(BORDER) + ";");

        Label sepText = new Label(text);
        sepText.setFont(Font.font("Segoe UI", 11));
        sepText.setTextFill(TEXT_SECONDARY);

        Separator rightSep = new Separator();
        rightSep.setPrefWidth(80);
        rightSep.setStyle("-fx-background-color: " + toHex(BORDER) + ";");

        separatorBox.getChildren().addAll(leftSep, sepText, rightSep);
        return separatorBox;
    }

    private VBox createLoginForm() {
        VBox form = new VBox(18);
        form.setPadding(new Insets(5, 0, 0, 0));

        // فیلد ایمیل/نام کاربری
        VBox usernameField = createFormField("👤", "ایمیل یا شماره موبایل", false);

        // فیلد رمز عبور
        VBox passwordField = createFormField("🔒", "رمز عبور", true);

        form.getChildren().addAll(usernameField, passwordField);
        return form;
    }

    private VBox createFormField(String icon, String placeholder, boolean isPassword) {
        VBox container = new VBox(6);

        HBox fieldContainer = new HBox(12);
        fieldContainer.setAlignment(Pos.CENTER_LEFT);
        fieldContainer.setStyle("-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: " + toHex(BORDER) + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-padding: 0 15;");
        fieldContainer.setPrefHeight(50);

        // آیکون
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(16));
        iconLabel.setTextFill(TEXT_SECONDARY);

        // فیلد
        TextField field;
        if (isPassword) {
            PasswordField passwordField = new PasswordField();
            passwordField.setStyle("-fx-background-color: transparent; " +
                    "-fx-border-color: transparent; " +
                    "-fx-padding: 15 0; " +
                    "-fx-font-size: 14px; " +
                    "-fx-text-fill: " + toHex(TEXT_PRIMARY) + ";");
            passwordField.setPromptText(placeholder);
            passwordField.setPrefWidth(280);
            field = passwordField;
        } else {
            TextField textField = new TextField();
            textField.setStyle("-fx-background-color: transparent; " +
                    "-fx-border-color: transparent; " +
                    "-fx-padding: 15 0; " +
                    "-fx-font-size: 14px; " +
                    "-fx-text-fill: " + toHex(TEXT_PRIMARY) + ";");
            textField.setPromptText(placeholder);
            textField.setPrefWidth(280);
            field = textField;
        }

        fieldContainer.getChildren().addAll(iconLabel, field);
        container.getChildren().add(fieldContainer);
        return container;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(20, 40, 20, 40));
        footer.setStyle("-fx-background-color: white; " +
                "-fx-border-color: " + toHex(BORDER) + "; " +
                "-fx-border-width: 1 0 0 0;");
        footer.setAlignment(Pos.CENTER);

        HBox links = new HBox(25);
        links.setAlignment(Pos.CENTER);

        Hyperlink privacyLink = new Hyperlink("حریم خصوصی");
        styleFooterLink(privacyLink);
        privacyLink.setOnAction(e -> showPrivacyPolicy());

        Hyperlink termsLink = new Hyperlink("قوانین استفاده");
        styleFooterLink(termsLink);
        termsLink.setOnAction(e -> showTermsAndConditions());

        Hyperlink helpLink = new Hyperlink("راهنما");
        styleFooterLink(helpLink);
        helpLink.setOnAction(e -> showHelp());

        Hyperlink contactLink = new Hyperlink("تماس با ما");
        styleFooterLink(contactLink);
        contactLink.setOnAction(e -> showContact());

        Label copyright = new Label("© 2024 VWM Messenger. All rights reserved.");
        copyright.setFont(Font.font("Segoe UI", 11));
        copyright.setTextFill(TEXT_SECONDARY);

        links.getChildren().addAll(privacyLink, termsLink, helpLink, contactLink,
                new Region(), copyright);
        HBox.setHgrow(links.getChildren().get(4), Priority.ALWAYS);

        footer.getChildren().add(links);
        return footer;
    }

    // ===== متدهای کمکی برای استایل =====

    private Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 14px; " +
                "-fx-font-weight: 600; " +
                "-fx-background-color: " + toHex(PRIMARY_BLUE) + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 12 30; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(30,136,229,0.3), 8, 0, 0, 2);");

        btn.setOnMouseEntered(e -> {
            btn.setStyle("-fx-font-size: 14px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-background-color: " + toHex(Color.web("#1976d2")) + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 10; " +
                    "-fx-padding: 12 30; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(30,136,229,0.3), 8, 0, 0, 2);");
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle("-fx-font-size: 14px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-background-color: " + toHex(PRIMARY_BLUE) + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 10; " +
                    "-fx-padding: 12 30; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(30,136,229,0.3), 8, 0, 0, 2);");
        });

        return btn;
    }

    private Button createSecondaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 14px; " +
                "-fx-font-weight: 500; " +
                "-fx-background-color: " + toHex(Color.web("#e3f2fd")) + "; " +
                "-fx-text-fill: " + toHex(PRIMARY_BLUE) + "; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 12 25; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: " + toHex(LIGHT_BLUE) + "; " +
                "-fx-border-width: 1;");
        return btn;
    }

    private Button createOutlineButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 14px; " +
                "-fx-font-weight: 500; " +
                "-fx-background-color: transparent; " +
                "-fx-text-fill: " + toHex(TEXT_SECONDARY) + "; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 12 25; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: " + toHex(BORDER) + "; " +
                "-fx-border-width: 1;");
        return btn;
    }

    private void styleCheckBox(CheckBox checkbox) {
        checkbox.setStyle("-fx-text-fill: " + toHex(TEXT_PRIMARY) + "; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: 500;");
    }

    private void styleFooterLink(Hyperlink link) {
        link.setStyle("-fx-text-fill: " + toHex(TEXT_SECONDARY) + "; " +
                "-fx-font-size: 12px; " +
                "-fx-border-color: transparent; " +
                "-fx-cursor: hand;");

        link.setOnMouseEntered(e -> {
            link.setStyle("-fx-text-fill: " + toHex(PRIMARY_BLUE) + "; " +
                    "-fx-font-size: 12px; " +
                    "-fx-border-color: transparent; " +
                    "-fx-cursor: hand;");
        });

        link.setOnMouseExited(e -> {
            link.setStyle("-fx-text-fill: " + toHex(TEXT_SECONDARY) + "; " +
                    "-fx-font-size: 12px; " +
                    "-fx-border-color: transparent; " +
                    "-fx-cursor: hand;");
        });
    }

    // ===== منطق برنامه =====

    private void attemptAutoLogin() {
        if (autoLoginAttempted || !secureApi.hasCachedLogin()) {
            showLoginCard();
            return;
        }

        autoLoginAttempted = true;

        // تغییر حالت پیشرفت
        autoLoginProgress.setProgress(-1);

        // تغییر متن
        StackPane parent = (StackPane) root.getCenter();
        VBox startupScreen = (VBox) parent.getChildren().get(0);

        Label subtitle = (Label) ((VBox) startupScreen.getChildren().get(1)).getChildren().get(1);
        subtitle.setText("در حال بررسی اطلاعات ورود...");

        // تأخیر برای شبیه‌سازی فرآیند
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            SecureApiClientV2.LoginResult result = secureApi.loginWithCache();

            if (result.isSuccess()) {
                showLoginSuccess(result.getUserData());
            } else {
                showLoginCard();
                showToast("اطلاعات ذخیره شده معتبر نیستند", WARNING);
            }
        });
        delay.play();
    }

    private void showLoginCard() {
        StackPane parent = (StackPane) root.getCenter();
        parent.getChildren().get(0).setVisible(false);
        parent.getChildren().get(1).setVisible(true);
    }

    private void showLoginSuccess(Map<String, String> userData) {
        // نمایش انیمیشن موفقیت
        StackPane parent = (StackPane) root.getCenter();
        VBox startupScreen = (VBox) parent.getChildren().get(0);

        StackPane iconContainer = (StackPane) startupScreen.getChildren().get(0);
        iconContainer.getChildren().clear();

        Circle successCircle = new Circle(60);
        successCircle.setFill(SUCCESS);

        Label checkIcon = new Label("✓");
        checkIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        checkIcon.setTextFill(Color.WHITE);

        iconContainer.getChildren().addAll(successCircle, checkIcon);

        // انیمیشن
        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.5), successCircle);
        scale.setFromX(0); scale.setFromY(0);
        scale.setToX(1); scale.setToY(1);
        scale.play();

        // تغییر متن
        VBox textContainer = (VBox) startupScreen.getChildren().get(1);
        Label welcome = (Label) textContainer.getChildren().get(0);
        Label subtitle = (Label) textContainer.getChildren().get(1);

        welcome.setText("خوش آمدید!");
        subtitle.setText(userData.getOrDefault("fullname", "کاربر") + "، در حال ورود به سیستم...");

        // مخفی کردن پیشرفت
        autoLoginProgress.setVisible(false);

        // ورود به سیستم بعد از تأخیر
        PauseTransition enterDelay = new PauseTransition(Duration.seconds(1.5));
        enterDelay.setOnFinished(e -> enterMessenger(userData));
        enterDelay.play();
    }

    private void enterMessenger(Map<String, String> userData) {
        // TODO: پیاده‌سازی صفحه اصلی پیام‌رسان
        System.out.println("ورود به پیام‌رسان با کاربر: " +
                userData.getOrDefault("fullname", "کاربر"));

        // نمایش پیام موقت
        showToast("ورود موفقیت‌آمیز بود!", SUCCESS);
    }

    // ===== دیالوگ‌های اطلاعاتی =====

    private void showTermsAndConditions() {
        Stage termsStage = new Stage();
        termsStage.initModality(Modality.APPLICATION_MODAL);
        termsStage.setTitle("قوانین و شرایط استفاده");
        termsStage.setResizable(false);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white;");

        // هدر
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("⚖️");
        icon.setFont(Font.font(24));

        VBox headerText = new VBox(5);
        Label title = new Label("قوانین و شرایط استفاده");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(TEXT_PRIMARY);

        Label subtitle = new Label("لطفاً قبل از استفاده، این شرایط را مطالعه کنید");
        subtitle.setFont(Font.font("Segoe UI", 12));
        subtitle.setTextFill(TEXT_SECONDARY);

        headerText.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(icon, headerText);

        // محتوای قوانین
        TextArea termsText = new TextArea(
                "قوانین و شرایط استفاده از پیام‌رسان VWM:\n\n" +
                        "۱. احترام به حریم خصوصی دیگران:\n" +
                        "   • از ارسال پیام‌های نامناسب خودداری کنید\n" +
                        "   • اطلاعات خصوصی دیگران را افشا نکنید\n" +
                        "   • حریم شخصی کاربران را محترم بشمارید\n\n" +
                        "۲. امنیت سیستم:\n" +
                        "   • از سیستم برای فعالیت‌های غیرقانونی استفاده نکنید\n" +
                        "   • در امنیت حساب کاربری خود کوشا باشید\n" +
                        "   • از اشتراک‌گذاری اطلاعات احراز هویت خودداری کنید\n\n" +
                        "۳. محتوای مناسب:\n" +
                        "   • محتوای توهین‌آمیز، تبعیض‌آمیز یا غیراخلاقی ممنوع است\n" +
                        "   • انتشار محتوای کپی‌رایت دار بدون اجازه ممنوع است\n" +
                        "   • تبلیغات غیرمجاز و اسپم پذیرفته نیست\n\n" +
                        "۴. تعهدات کاربر:\n" +
                        "   • کاربر مسئول تمام فعالیت‌های حساب خود است\n" +
                        "   • در صورت مشاهده فعالیت مشکوک، به پشتیبانی گزارش دهید\n" +
                        "   • از سیستم به صورت مسئولانه استفاده کنید\n\n" +
                        "با استفاده از پیام‌رسان VWM، این شرایط را می‌پذیرید."
        );
        termsText.setEditable(false);
        termsText.setWrapText(true);
        termsText.setPrefHeight(400);
        termsText.setPrefWidth(500);
        termsText.setStyle("-fx-font-size: 13px; " +
                "-fx-font-family: 'Segoe UI'; " +
                "-fx-background-color: #f8f9fa; " +
                "-fx-border-color: " + toHex(BORDER) + ";");

        // دکمه تأیید
        Button acceptBtn = createPrimaryButton("درک کردم و می‌پذیرم");
        acceptBtn.setOnAction(e -> termsStage.close());
        acceptBtn.setPrefWidth(Double.MAX_VALUE);

        content.getChildren().addAll(header, termsText, acceptBtn);

        Scene scene = new Scene(content, 550, 600);
        termsStage.setScene(scene);
        termsStage.showAndWait();
    }

    private void showPrivacyPolicy() {
        showToast("صفحه حریم خصوصی به زودی اضافه خواهد شد", PRIMARY_BLUE);
    }

    private void showFeatures() {
        showToast("ویژگی‌های پیام‌رسان به زودی نمایش داده می‌شود", LIGHT_BLUE);
    }

    private void showForgotPassword() {
        showToast("سیستم بازیابی رمز عبور به زودی فعال می‌شود", WARNING);
    }

    private void showGuestModeInfo() {
        showToast("حالت مهمان: پیام‌ها موقتاً ذخیره می‌شوند", TEXT_SECONDARY);
    }

    private void showRegistration() {
        showToast("سیستم ثبت‌نام به زودی راه‌اندازی می‌شود", PRIMARY_BLUE);
    }

    private void showHelp() {
        showToast("مرکز راهنمای کاربری در حال توسعه است", TEXT_SECONDARY);
    }

    private void showContact() {
        showToast("اطلاعات تماس به زودی اضافه خواهد شد", TEXT_SECONDARY);
    }

    private void showToast(String message, Color color) {
        Label toast = new Label(message);
        toast.setStyle("-fx-background-color: " + toHex(color) + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: 500; " +
                "-fx-padding: 12 25; " +
                "-fx-background-radius: 8;");

        StackPane toastContainer = new StackPane(toast);
        toastContainer.setAlignment(Pos.BOTTOM_CENTER);
        toastContainer.setPadding(new Insets(0, 0, 30, 0));

        // اضافه کردن toast به root
        StackPane rootContainer = new StackPane(root, toastContainer);
        Scene scene = primaryStage.getScene();
        if (scene != null) {
            scene.setRoot(rootContainer);
        }

        // مخفی کردن toast بعد از 3 ثانیه
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            if (scene != null) {
                scene.setRoot(root);
            }
        });
        delay.play();
    }

    // ===== متدهای کمکی =====

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    public Parent getRoot() {
        return root;
    }
}