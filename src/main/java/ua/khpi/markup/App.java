package ua.khpi.markup;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ua.khpi.markup.controller.MainController;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("File markup application");
        primaryStage.setScene(new Scene(root, 800, 500));
        MainController controller = loader.getController();
        controller.init();
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
