package ua.khpi.markup.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class ControllerUtils {

    public static Optional<ButtonType> showAlert(Alert.AlertType alertType, String title, String content) {
        var alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}
