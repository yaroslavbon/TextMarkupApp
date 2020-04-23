package ua.khpi.markup.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static ua.khpi.markup.util.ControllerUtils.showAlert;

public class SettingsController {

    private MainController controller;

    @FXML
    private TextField mkPrefix;

    @FXML
    private TextField mkSuffix;

    @FXML
    void updateSettings(ActionEvent event) {
        String prefix = mkPrefix.getText();
        String suffix = mkSuffix.getText();

        if (prefix.trim().isEmpty() || suffix.trim().isEmpty()){
            showAlert(Alert.AlertType.ERROR, "Settings saving error",
                    "Can not save empty prefix or suffix");
            return;
        }

        controller.setMarkupPrefix(prefix);
        controller.setMarkupSuffix(suffix);

        showAlert(Alert.AlertType.INFORMATION, "Success", "Markup settings successfully saved");

        closeStage(event);
    }

    public void init(MainController controller){
        mkPrefix.setText(controller.getMarkupPrefix());
        mkSuffix.setText(controller.getMarkupSuffix());
        this.controller = controller;
    }

    private void closeStage(ActionEvent event) {
        Node source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
