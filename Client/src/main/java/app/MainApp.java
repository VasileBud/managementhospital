package app;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.init(stage);
        stage.setTitle("Hospital Management");
        stage.setWidth(1000);
        stage.setHeight(864);
        stage.setMinWidth(1000);
        stage.setMinHeight(864);
        stage.setMaxWidth(1000);
        stage.setMaxHeight(864);
        SceneNavigator.navigateTo(AppScene.PUBLIC);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
