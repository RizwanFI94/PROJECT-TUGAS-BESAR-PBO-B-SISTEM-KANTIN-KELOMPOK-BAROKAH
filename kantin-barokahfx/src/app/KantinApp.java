package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.Database;  

public class KantinApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        Database.initSchemaIfNeeded();

        setRoot("view/login-view.fxml", "Kantin Barokah - Login");
        primaryStage.show();
    }

    public static void setRoot(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    KantinApp.class.getResource("/" + fxmlPath));
            Parent root = loader.load();
            primaryStage.setTitle(title);
            primaryStage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Running Program

    public static void main(String[] args) {
        launch(args);
    }
}