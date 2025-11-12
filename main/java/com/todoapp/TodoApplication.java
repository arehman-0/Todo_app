ackage com.todoapp;

import com.todoapp.util.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class TodoApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database
        DatabaseInitializer.initialize();

        FXMLLoader fxmlLoader = new FXMLLoader(
                TodoApplication.class.getResource("/com/todoapp/view/main-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);

        // Load CSS
        scene.getStylesheets().add(
                TodoApplication.class.getResource("/com/todoapp/css/styles.css").toExternalForm()
        );

        stage.setTitle("My ToDo - Microsoft ToDo Clone");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}