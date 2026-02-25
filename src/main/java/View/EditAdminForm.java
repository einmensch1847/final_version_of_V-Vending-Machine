package View;

import api.ApiClient;
import api.Logger;
import com.example.vwm.Main;
import com.example.vwm.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class EditAdminForm {

    private VBox root;
    private TextField fullnameField;
    private PasswordField passwordField;
    private TextField emailField;
    private TextField phoneField;
    private ComboBox<String> levelCombo;
    private Label statusLabel;

    private ApiClient api = new ApiClient();
    private Admin currentAdmin;   // ادمین لاگین‌شده
    private Admin selectedAdmin;  // ادمین انتخاب‌شده برای ویرایش

    public EditAdminForm(Admin selectedAdmin, Admin currentAdmin) {
        this.selectedAdmin = selectedAdmin;
        this.currentAdmin = currentAdmin;
        createUI();
        loadAdminDetails();
    }

    private void createUI() {
        root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: rgba(40,40,40,0.95);");

        Label title = new Label("ویرایش ادمین انتخاب‌شده");
        title.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold;");

        fullnameField = new TextField();
        fullnameField.setPromptText("نام و نام خانوادگی");
        styleField(fullnameField);

        emailField = new TextField();
        emailField.setPromptText("ایمیل");
        styleField(emailField);

        phoneField = new TextField();
        phoneField.setPromptText("تلفن");
        styleField(phoneField);

        passwordField = new PasswordField();
        passwordField.setPromptText("پسورد جدید (در صورت نیاز)");
        styleField(passwordField);

        levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("superadmin", "manager", "operator");
        levelCombo.setPromptText("سطح دسترسی");
        levelCombo.setStyle("-fx-font-size: 20px; -fx-background-radius: 18; -fx-padding: 10;");

        Button saveBtn = new Button("ذخیره تغییرات");
        styleButton(saveBtn);
        saveBtn.setOnAction(e -> handleSaveAdmin());

        Button backToSelectedBtn = new Button("بازگشت به پنل ادمین انتخاب‌شده");
        styleButton(backToSelectedBtn);
        backToSelectedBtn.setOnAction(e -> {
            AdminPanel panel = new AdminPanel(selectedAdmin);
            SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), panel.getRoot());
        });

        Button backToLoginBtn = new Button("بازگشت به پنل ادمین لاگین‌شده");
        styleButton(backToLoginBtn);
        backToLoginBtn.setOnAction(e -> {
            AdminPanel panel = new AdminPanel(currentAdmin);
            SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), panel.getRoot());
        });

        HBox btnBox = new HBox(20, saveBtn, backToSelectedBtn, backToLoginBtn);
        btnBox.setAlignment(Pos.CENTER);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 16px;");

        root.getChildren().addAll(
                title,
                fullnameField,
                emailField,
                phoneField,
                passwordField,
                levelCombo,
                btnBox,
                statusLabel
        );
    }

    private void loadAdminDetails() {
        if (selectedAdmin != null) {
            fullnameField.setText(selectedAdmin.getFullname());
            emailField.setText(selectedAdmin.getEmail());
            phoneField.setText(selectedAdmin.getPhone());
            levelCombo.setValue(selectedAdmin.getLevel());
        }
    }

    private void handleSaveAdmin() {
        if (selectedAdmin == null) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("هیچ ادمینی انتخاب نشده است.");
            return;
        }

        String fullname = fullnameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String level = levelCombo.getValue();
        String password = passwordField.getText().trim();

        if (fullname.isEmpty() || email.isEmpty() || phone.isEmpty() || level == null) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("تمام فیلدها باید پر شوند.");
            return;
        }

        boolean ok = api.updateAdmin(
                selectedAdmin.getUsername(),
                fullname,
                email,
                phone,
                level,
                password   // اگر خالی باشد، PHP پسورد قبلی حفظ می‌کند
        );

        if (ok) {
            // لاگ قبل از بروزرسانی اطلاعات محلی برای مقایسه دقیق تغییرات
            String desc = "Edit Admin: " + selectedAdmin.getUsername()
                    + ", Fullname: " + selectedAdmin.getFullname() + " -> " + fullname
                    + ", Email: " + selectedAdmin.getEmail() + " -> " + email
                    + ", Phone: " + selectedAdmin.getPhone() + " -> " + phone
                    + ", Level: " + selectedAdmin.getLevel() + " -> " + level;
            if (!password.isEmpty()) {
                desc += ", Password: [CHANGED]";
            }

            Logger.log(currentAdmin.getUsername(), "Edit Admin", desc, selectedAdmin.getUsername(), "Admin", "Info");

            // بروزرسانی اطلاعات محلی
            selectedAdmin.setFullname(fullname);
            selectedAdmin.setEmail(email);
            selectedAdmin.setPhone(phone);
            selectedAdmin.setLevel(level);

            statusLabel.setTextFill(Color.LIGHTGREEN);
            statusLabel.setText("تغییرات با موفقیت ذخیره شد!");

        } else {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("خطا در ذخیره تغییرات.");
        }

    }

    private void styleField(TextField field) {
        field.setStyle("-fx-font-size: 20px;" +
                "-fx-background-color: rgba(255,255,255,0.10);" +
                "-fx-border-color: rgba(255,255,255,0.25);" +
                "-fx-border-width: 1.5;" +
                "-fx-padding: 12 20;" +
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: rgba(255,255,255,0.45);");
        field.setPrefWidth(400);
    }

    private void styleButton(Button btn) {
        btn.setStyle("-fx-font-size: 18px; -fx-background-color: #4e9cff; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 15;");
    }

    public Parent getRoot() {
        return root;
    }
}
