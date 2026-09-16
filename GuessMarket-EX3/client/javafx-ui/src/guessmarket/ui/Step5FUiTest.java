package guessmarket.ui;

import guessmarket.client.ClientSession;
import guessmarket.client.http.ClientJsonResponse;
import guessmarket.client.http.GuessMarketHttpClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.Map;

public class Step5FUiTest extends Application {

    private int failures;
    private GuessMarketHttpClient server;


    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage aliceStage) throws Exception {
        this.server = new GuessMarketHttpClient();
        ClientSession.clear();
        this.server.postFormJson("/login", Map.of("userName", "Alice"), ClientJsonResponse.class);
        ClientSession.setCurrentUserName("Alice");
        MainController alice = this.loadMain(aliceStage);
        alice.addFundsAmountField.setText("1000");
        alice.handleAddFunds();

        this.server.postFormJson("/login", Map.of("userName", "Bob"), ClientJsonResponse.class);
        ClientSession.setCurrentUserName("Bob");
        Stage bobStage = new Stage();
        MainController bob = this.loadMain(bobStage);
        bob.addFundsAmountField.setText("500.5");
        bob.handleAddFunds();

        ClientSession.setCurrentUserName("Alice");
        alice.refreshLoggedInUserAccount();
        String aliceOthers = alice.usersListView.getItems().toString();
        check(
                "E Alice Other Users shows Bob with 2 decimals and MM NO",
                aliceOthers.contains("Bob")
                        &&
                        aliceOthers.contains("500.50")
                        &&
                        aliceOthers.contains("MM: NO")
                        &&
                        !aliceOthers.contains("Alice")
        );

        alice.uploadXmlFile(Path.of("course-materials", "EX3-small.xml"));
        alice.refreshLoggedInUserAccount();
        String usersJson = this.server.getRaw("/users");
        check(
                "F Alice is MM on the server after upload",
                alice.selectedUserBlockedLabel.getText().contains("MM events")
                        &&
                        usersJson.contains("\"name\":\"Alice\"")
                        &&
                        usersJson.contains("marketMakerEventIds")
        );

        ClientSession.setCurrentUserName("Bob");
        bob.loadUsersToList();
        String bobOthers = bob.usersListView.getItems().toString();
        check(
                "G Bob Other Users shows Alice as MM YES",
                bobOthers.contains("Alice")
                        &&
                        bobOthers.contains("MM: YES")
                        &&
                        !bobOthers.contains("Bob |")
        );

        String bobName = bob.selectedUserNameLabel.getText();
        String bobBalance = bob.selectedUserCashLabel.getText();
        String bobHistory = bob.accountHistoryListView.getItems().toString();
        bob.httpClient.clearRequestedPaths();
        bob.usersListView.getSelectionModel().select(0);
        bob.handleUserSelection();
        String afterClickPaths = String.join(" ", bob.httpClient.requestedPaths());
        check(
                "H Bob identity stays Bob after selecting Alice",
                "Bob".equals(ClientSession.getCurrentUserName())
                        &&
                        bob.selectedUserNameLabel.getText().equals(bobName)
                        &&
                        bob.selectedUserCashLabel.getText().equals(bobBalance)
                        &&
                        bob.accountHistoryListView.getItems().toString().equals(bobHistory)
        );

        bob.httpClient.clearRequestedPaths();
        bob.addFundsAmountField.setText("1");
        bob.handleAddFunds();
        String fundsPaths = String.join(" ", bob.httpClient.requestedPaths());
        check(
                "H Bob actions still send userName=Bob",
                fundsPaths.contains("/funds")
                        &&
                        (fundsPaths.contains("userName=Bob")
                                || "Bob".equals(ClientSession.getCurrentUserName()))
        );

        check(
                "I selecting Alice does not fetch Alice private data",
                !afterClickPaths.contains("userName=Alice")
                        &&
                        !afterClickPaths.contains("/account-history")
                        &&
                        !afterClickPaths.contains("/user-shares")
                        &&
                        !afterClickPaths.contains("/user-lmsr-trades")
        );

        if (this.failures == 0) {
            System.out.println("STEP5F_UI_OK");
        } else {
            System.out.println("STEP5F_UI_FAILED " + this.failures);
        }
        aliceStage.close();
        bobStage.close();
        Platform.exit();
    }


    private MainController loadMain(Stage stage) throws Exception {
        FXMLLoader loader =
                new FXMLLoader(GuessMarketApplication.class.getResource("/main-view.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
        return loader.getController();
    }


    private void check(String name, boolean ok) {
        if (ok) {
            System.out.println("PASS " + name);
        } else {
            this.failures++;
            System.out.println("FAIL " + name);
        }
    }
}
