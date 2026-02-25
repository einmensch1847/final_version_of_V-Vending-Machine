module com.example.vwm {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.vwm to javafx.fxml;
    exports com.example.vwm;
}