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

import java.util.ArrayList;

public class ManageAdminsPage {

    private VBox root;
    private ListView<String> adminList;
    private Label status;
    private ApiClient api = new ApiClient();
    private Admin currentAdmin; // ادمین لاگین‌شده

    public ManageAdminsPage(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        createUI();
        loadAdmins();
    }

    private void createUI() {
        root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #222;");

        Label title = new Label("مدیریت ادمین‌ها");
        title.setStyle("-fx-font-size: 28px; -fx-text-fill: white;");

        adminList = new ListView<>();
        adminList.setPrefHeight(300);
        adminList.setStyle("-fx-font-size: 20px;");

        Button editBtn = new Button("ویرایش");
        Button deleteBtn = new Button("حذف");
        Button backBtn = new Button("بازگشت به پنل ادمین لاگین‌شده");

        styleButton(editBtn);
        styleButton(deleteBtn);
        styleButton(backBtn);

        editBtn.setOnAction(e -> goToEdit());
        deleteBtn.setOnAction(e -> deleteAdmin());
        backBtn.setOnAction(e -> {
            // بازگشت به پنل ادمین لاگین‌شده
            AdminPanel panel = new AdminPanel(currentAdmin);
            SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), panel.getRoot());
        });

        HBox actionBox = new HBox(20, editBtn, deleteBtn, backBtn);
        actionBox.setAlignment(Pos.CENTER);

        status = new Label();
        status.setStyle("-fx-font-size: 18px;");

        root.getChildren().addAll(title, adminList, actionBox, status);
    }

    private void loadAdmins() {
        ArrayList<String> admins = api.getAdminList();
        adminList.getItems().clear();
        adminList.getItems().addAll(admins);
    }

    private void goToEdit() {
        String selectedUsername = adminList.getSelectionModel().getSelectedItem();
        if (selectedUsername == null) {
            status.setTextFill(Color.RED);
            status.setText("یک ادمین انتخاب کنید.");
            return;
        }

        Admin selectedAdmin = api.getAdminDetails(selectedUsername);
        if (selectedAdmin == null) {
            status.setTextFill(Color.RED);
            status.setText("اطلاعات ادمین پیدا نشد.");
            return;
        }

        // باز کردن فرم ویرایش با ادمین انتخاب‌شده و ادمین لاگین‌شده
        EditAdminForm form = new EditAdminForm(selectedAdmin, currentAdmin);
        SceneManager.switchSceneWithFadeTransition(Main.getInstance().getPrimaryStage(), form.getRoot());
    }

    private void deleteAdmin() {
        String selectedUsername = adminList.getSelectionModel().getSelectedItem();
        if (selectedUsername == null) {
            status.setTextFill(Color.RED);
            status.setText("یک ادمین انتخاب کنید.");
            return;
        }

        // تایید حذف داخل پنل
        VBox confirmBox = new VBox(15);
        confirmBox.setStyle("-fx-background-color: rgba(50,50,50,0.95); -fx-padding: 20; -fx-background-radius: 10;");
        confirmBox.setAlignment(Pos.CENTER);

        Label confirmLabel = new Label("آیا مطمئن هستید که می‌خواهید این ادمین را حذف کنید؟");
        confirmLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-wrap-text: true;");

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER);

        Button yesBtn = new Button("بله");
        Button noBtn = new Button("خیر");
        styleButton(yesBtn);
        styleButton(noBtn);

        btnBox.getChildren().addAll(yesBtn, noBtn);
        confirmBox.getChildren().addAll(confirmLabel, btnBox);

        root.getChildren().add(confirmBox);

//        yesBtn.setOnAction(e -> {
//            boolean ok = api.deleteAdmin(selectedUsername);
//            if (ok) {
//                status.setTextFill(Color.LIGHTGREEN);
//                status.setText("ادمین حذف شد.");
//                loadAdmins();
//            } else {
//                status.setTextFill(Color.RED);
//                status.setText("خطا در حذف ادمین.");
//            }
//            root.getChildren().remove(confirmBox);
//        });
        yesBtn.setOnAction(e -> {
            boolean ok = api.deleteAdmin(selectedUsername);

            if (ok) {
                status.setTextFill(Color.LIGHTGREEN);
                status.setText("ادمین حذف شد.");
                loadAdmins();

                // ✅ لاگ حذف ادمین
                String desc = "Admin deleted: " + selectedUsername;

                Logger.log(
                        currentAdmin.getUsername(), // انجام‌دهنده
                        "Delete",                  // نوع عملیات
                        desc,                      // توضیح
                        selectedUsername,          // هدف
                        "Admin",                  // نوع هدف
                        "Warning"                 // سطح اهمیت
                );

            } else {
                status.setTextFill(Color.RED);
                status.setText("خطا در حذف ادمین.");
            }

            root.getChildren().remove(confirmBox);
        });


        noBtn.setOnAction(e -> root.getChildren().remove(confirmBox));
    }

    private void styleButton(Button btn) {
        btn.setStyle("-fx-font-size: 18px; -fx-background-color: #4e9cff; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 15;");
    }

    public Parent getRoot() {
        return root;
    }
}
