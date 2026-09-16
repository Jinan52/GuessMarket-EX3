package guessmarket.ui;

import guessmarket.client.ClientSession;
import guessmarket.client.http.ClientJsonResponse;
import guessmarket.client.http.GuessMarketHttpClient;
import guessmarket.client.http.GuessMarketHttpException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class LoginController {

    private final GuessMarketHttpClient httpClient =
            new GuessMarketHttpClient();

    @FXML
    TextField userNameField;

    @FXML
    Button loginButton;

    @FXML
    Label statusLabel;


    @FXML
    void handleLogin() {

        String enteredName =
                userNameField.getText();

        if (enteredName == null
                ||
                enteredName.trim().isEmpty()) {

            statusLabel.setText(
                    "User name cannot be empty."
            );

            return;
        }

        String trimmedName =
                enteredName.trim();

        try {

            ClientJsonResponse response =
                    httpClient.postFormJson(
                            "/login",
                            Map.of(
                                    "userName",
                                    trimmedName
                            ),
                            ClientJsonResponse.class
                    );

            if (!response.isSuccess()) {

                statusLabel.setText(
                        response.getMessage()
                );

                return;
            }

            ClientSession.setCurrentUserName(
                    trimmedName
            );

            openMainScreen();

        } catch (GuessMarketHttpException exception) {

            statusLabel.setText(
                    exception.getMessage()
            );
        }
    }


    private void openMainScreen() {

        try {

            Stage stage =
                    (Stage) loginButton
                            .getScene()
                            .getWindow();

            FXMLLoader fxmlLoader =
                    new FXMLLoader(
                            GuessMarketApplication.class.getResource(
                                    "/main-view.fxml"
                            )
                    );

            Scene scene =
                    new Scene(
                            fxmlLoader.load(),
                            900,
                            600
                    );

            stage.setTitle("Guess Market");
            stage.setScene(scene);

        } catch (IOException exception) {

            statusLabel.setText(
                    "The main screen could not be opened."
            );
        }
    }
}
