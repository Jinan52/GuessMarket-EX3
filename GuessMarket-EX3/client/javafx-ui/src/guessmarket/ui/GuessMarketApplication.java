package guessmarket.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GuessMarketApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader =
                new FXMLLoader(GuessMarketApplication.class.getResource("/login-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 420, 240);

        stage.setTitle("Guess Market Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}