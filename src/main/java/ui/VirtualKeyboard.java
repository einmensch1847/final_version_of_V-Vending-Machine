package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

public class VirtualKeyboard extends VBox {

    private final TextField targetTextField;
    private final PasswordField targetPasswordField;
    private final boolean isPassword;

    private boolean upperCase = true;
    private boolean symbolMode = false;

    // رنگ‌های جدید برای هماهنگی با تم
    private static final String KEY_COLOR = "#4e9cff";
    private static final String KEY_HOVER_COLOR = "#1c72ff";
    private static final String SPECIAL_KEY_COLOR = "#6c5ce7";
    private static final String SPECIAL_HOVER_COLOR = "#5b4bd4";
    private static final String BACKGROUND_COLOR = "rgba(22, 33, 62, 0.9)";

    public VirtualKeyboard(TextField textField) {
        this.targetTextField = textField;
        this.targetPasswordField = null;
        this.isPassword = false;
        init();
    }

    public VirtualKeyboard(PasswordField passwordField) {
        this.targetTextField = null;
        this.targetPasswordField = passwordField;
        this.isPassword = true;
        init();
    }

    private void init() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: " + BACKGROUND_COLOR + "; " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: rgba(78, 156, 255, 0.3); " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 15;");

        drawKeyboard();
    }

    private void drawKeyboard() {
        this.getChildren().clear();

        if (symbolMode)
            drawSymbols();
        else
            drawLetters();

        drawBottomRow();
    }

    // ================= LETTERS =================
    private void drawLetters() {
        addRow("QWERTYUIOP", 0);
        addRow("ASDFGHJKL", 20);

        HBox row3 = createRow(0);
        Button shift = specialKey("Caps", "حروف بزرگ/کوچک");
        shift.setOnAction(e -> {
            upperCase = !upperCase;
            drawKeyboard();
        });

        row3.getChildren().add(shift);
        addKeysToRow(row3, "ZXCVBNM");

        Button back = specialKey("⌫", "حذف");
        back.setOnAction(e -> deleteChar());
        row3.getChildren().add(back);

        this.getChildren().add(row3);
    }

    // ================= SYMBOLS =================
    private void drawSymbols() {
        addRow("1234567890", 0);
        addRow("@#$%&*-+()", 0);
        addRow("!\"':;/?.,", 0);
    }

    // ================= BOTTOM =================
    private void drawBottomRow() {
        HBox bottom = createRow(0);

        Button mode = specialKey(symbolMode ? "ABC" : "?123",
                symbolMode ? "حروف" : "نمادها");
        mode.setOnAction(e -> {
            symbolMode = !symbolMode;
            drawKeyboard();
        });

        Button space = specialKey("Space", "فاصله");
        space.setPrefWidth(250);
        space.setOnAction(e -> append(" "));

        bottom.getChildren().addAll(mode, space);
        this.getChildren().add(bottom);
    }

    // ================= HELPERS =================
    private void addRow(String keys, int offset) {
        HBox row = createRow(offset);
        addKeysToRow(row, keys);
        this.getChildren().add(row);
    }

    private HBox createRow(int leftPadding) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 0, 0, leftPadding));
        return row;
    }

    private void addKeysToRow(HBox row, String keys) {
        for (char c : keys.toCharArray()) {
            String text = upperCase ? String.valueOf(c) : String.valueOf(c).toLowerCase();
            Button btn = key(text);
            row.getChildren().add(btn);
        }
    }

    private Button key(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(60, 60);
        btn.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-background-color: " + KEY_COLOR + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-effect: dropshadow(gaussian, rgba(78,156,255,0.5), 5, 0.3, 0, 2); " +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                    "-fx-font-size: 18px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 10; " +
                            "-fx-background-color: " + KEY_HOVER_COLOR + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-effect: dropshadow(gaussian, rgba(28,114,255,0.7), 8, 0.4, 0, 3); " +
                            "-fx-cursor: hand; " +
                            "-fx-scale-x: 1.05; " +
                            "-fx-scale-y: 1.05;"
            );
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(
                    "-fx-font-size: 18px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 10; " +
                            "-fx-background-color: " + KEY_COLOR + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-effect: dropshadow(gaussian, rgba(78,156,255,0.5), 5, 0.3, 0, 2); " +
                            "-fx-cursor: hand;"
            );
        });

        btn.setOnAction(e -> append(text));
        return btn;
    }

    private Button specialKey(String text, String tooltipText) {
        Button btn = new Button(text);
        btn.setPrefSize(90, 60);

        // Tooltip for accessibility
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: rgba(22,33,62,0.9); " +
                "-fx-text-fill: white; -fx-border-color: rgba(78,156,255,0.5);");
        btn.setTooltip(tooltip);

        btn.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-background-color: " + SPECIAL_KEY_COLOR + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-effect: dropshadow(gaussian, rgba(108,92,231,0.5), 5, 0.3, 0, 2); " +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                    "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 10; " +
                            "-fx-background-color: " + SPECIAL_HOVER_COLOR + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-effect: dropshadow(gaussian, rgba(91,75,212,0.7), 8, 0.4, 0, 3); " +
                            "-fx-cursor: hand; " +
                            "-fx-scale-x: 1.05; " +
                            "-fx-scale-y: 1.05;"
            );
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(
                    "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 10; " +
                            "-fx-background-color: " + SPECIAL_KEY_COLOR + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-effect: dropshadow(gaussian, rgba(108,92,231,0.5), 5, 0.3, 0, 2); " +
                            "-fx-cursor: hand;"
            );
        });

        return btn;
    }

    // Overload method for backward compatibility
    private Button specialKey(String text) {
        return specialKey(text, text);
    }

    private void append(String value) {
        if (isPassword) targetPasswordField.appendText(value);
        else targetTextField.appendText(value);
    }

    private void deleteChar() {
        if (isPassword) {
            String t = targetPasswordField.getText();
            if (!t.isEmpty())
                targetPasswordField.setText(t.substring(0, t.length() - 1));
        } else {
            String t = targetTextField.getText();
            if (!t.isEmpty())
                targetTextField.setText(t.substring(0, t.length() - 1));
        }
    }
}