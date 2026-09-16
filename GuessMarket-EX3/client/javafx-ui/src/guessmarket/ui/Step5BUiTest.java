package guessmarket.ui;

import guessmarket.client.ClientSession;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Step5BUiTest extends Application {

    private int failures;


    public static void main(
            String[] args) {

        launch(args);
    }


    @Override
    public void start(
            Stage firstStage)
            throws Exception {

        ClientSession.clear();

        FXMLLoader firstLoader =
                new FXMLLoader(
                        GuessMarketApplication.class.getResource(
                                "/login-view.fxml"
                        )
                );

        Parent firstRoot =
                firstLoader.load();

        LoginController firstLogin =
                firstLoader.getController();

        firstStage.setTitle("Guess Market Login");
        firstStage.setScene(
                new Scene(
                        firstRoot,
                        420,
                        240
                )
        );
        firstStage.show();

        boolean loginScreenShown =
                firstRoot.lookup("#userNameField")
                        !=
                        null
                        &&
                        firstRoot.lookup("#loginButton")
                                !=
                                null
                                &&
                                firstRoot.lookup("#statusLabel")
                                        !=
                                        null
                                        &&
                                        firstRoot.lookup("#usersListView")
                                                ==
                                                null;

        check(
                "B login screen appears first",
                loginScreenShown
        );

        firstLogin.userNameField.setText("   ");
        firstLogin.handleLogin();

        check(
                "C blank name shows an error",
                "User name cannot be empty.".equals(
                        firstLogin.statusLabel.getText()
                )
        );

        check(
                "C blank name does not navigate",
                firstStage.getScene().getRoot()
                        ==
                        firstRoot
        );

        check(
                "C blank name does not store a user",
                ClientSession.getCurrentUserName()
                        ==
                        null
        );

        firstLogin.userNameField.setText("Alice");
        firstLogin.handleLogin();

        Parent firstAfterLogin =
                firstStage.getScene().getRoot();
        firstAfterLogin.applyCss();
        firstAfterLogin.layout();

        check(
                "D Alice login stores currentUserName",
                "Alice".equals(
                        ClientSession.getCurrentUserName()
                )
        );

        check(
                "D Alice login opens the main screen",
                "Guess Market".equals(
                        firstStage.getTitle()
                )
                        &&
                        firstAfterLogin
                                !=
                                firstRoot
                        &&
                        firstAfterLogin.lookup("#titleLabel")
                                !=
                                null
        );

        if (firstAfterLogin.lookup("#titleLabel")
                instanceof
                Label titleAfterAlice) {

            System.out.println(
                    "D main title: "
                            + titleAfterAlice.getText()
            );
        } else {

            System.out.println(
                    "D scene root: "
                            + firstAfterLogin.getClass().getName()
            );
        }

        Stage secondStage =
                new Stage();

        FXMLLoader secondLoader =
                new FXMLLoader(
                        GuessMarketApplication.class.getResource(
                                "/login-view.fxml"
                        )
                );

        Parent secondRoot =
                secondLoader.load();

        LoginController secondLogin =
                secondLoader.getController();

        secondStage.setTitle("Guess Market Login");
        secondStage.setScene(
                new Scene(
                        secondRoot,
                        420,
                        240
                )
        );
        secondStage.show();

        secondLogin.userNameField.setText("Alice");
        secondLogin.handleLogin();

        String duplicateMessage =
                secondLogin.statusLabel.getText();

        check(
                "E duplicate Alice stays on login",
                secondStage.getScene().getRoot()
                        ==
                        secondRoot
        );

        check(
                "E duplicate Alice shows a server error",
                duplicateMessage != null
                        &&
                        !duplicateMessage.isBlank()
                        &&
                        !duplicateMessage.equals(
                                "User name cannot be empty."
                        )
        );

        System.out.println(
                "E duplicate Alice message: "
                        + duplicateMessage
        );

        secondLogin.userNameField.setText("Bob");
        secondLogin.handleLogin();

        Parent secondAfterLogin =
                secondStage.getScene().getRoot();
        secondAfterLogin.applyCss();
        secondAfterLogin.layout();

        check(
                "F Bob login stores currentUserName",
                "Bob".equals(
                        ClientSession.getCurrentUserName()
                )
        );

        check(
                "F Bob login opens the main screen",
                "Guess Market".equals(
                        secondStage.getTitle()
                )
                        &&
                        secondAfterLogin
                                !=
                                secondRoot
                        &&
                        secondAfterLogin.lookup("#titleLabel")
                                !=
                                null
        );

        if (failures
                ==
                0) {

            System.out.println(
                    "STEP5B_UI_OK"
            );

        } else {

            System.out.println(
                    "STEP5B_UI_FAILED "
                            + failures
            );
        }

        firstStage.close();
        secondStage.close();
        Platform.exit();
    }


    private void check(
            String name,
            boolean ok) {

        if (ok) {

            System.out.println(
                    "PASS "
                            + name
            );

        } else {

            failures++;

            System.out.println(
                    "FAIL "
                            + name
            );
        }
    }
}
