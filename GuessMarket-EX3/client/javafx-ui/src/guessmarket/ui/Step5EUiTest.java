package guessmarket.ui;

import guessmarket.client.ClientSession;
import guessmarket.client.http.ClientJsonResponse;
import guessmarket.client.http.GuessMarketHttpClient;
import guessmarket.client.http.UserSharesResponse;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.Map;

public class Step5EUiTest extends Application {

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
        check("C users list contains Alice", alice.usersListView.getItems().contains("Alice"));
        check(
                "C Alice balance comes from the server",
                alice.selectedUserNameLabel.getText().contains("Alice")
                        &&
                        alice.selectedUserCashLabel.getText().contains("Balance:")
        );

        alice.addFundsAmountField.setText("1000");
        alice.handleAddFunds();
        String aliceUsersJson = this.server.getRaw("/users");
        check(
                "D add 1000 updates server, UI, and history",
                aliceUsersJson.contains("\"name\":\"Alice\"")
                        &&
                        aliceUsersJson.contains("\"balance\":1000.0")
                        &&
                        alice.selectedUserCashLabel.getText().contains("1000")
                        &&
                        alice.accountHistoryListView.getItems().toString().contains("Funds added")
        );

        alice.uploadXmlFile(Path.of("course-materials", "EX3-small.xml"));
        this.selectEvent(alice, "Mujtaba is Dead");
        alice.handleOpenEvent();
        check("E Alice opened LMSR event", alice.eventStatusLabel.getText().contains("ACTIVE"));

        this.server.postFormJson("/login", Map.of("userName", "Bob"), ClientJsonResponse.class);
        ClientSession.setCurrentUserName("Bob");
        Stage bobStage = new Stage();
        MainController bob = this.loadMain(bobStage);
        bob.addFundsAmountField.setText("20000");
        bob.handleAddFunds();
        this.selectEvent(bob, "Mujtaba is Dead");
        bob.lmsrPurchaseQuantityField.setText("10");
        bob.handleLmsrPurchase();
        bob.refreshLoggedInUserAccount();
        this.selectActive(bob, "Mujtaba");

        UserSharesResponse bobShares =
                this.server.getJson("/user-shares?userName=Bob&eventId=1", UserSharesResponse.class);
        check(
                "G Bob private LMSR panel is Bob's",
                bob.userActiveEventsListView.getItems().toString().contains("Mujtaba")
                        &&
                        bobShares.getShares().get(0) == 10
                        &&
                        bob.userLmsrTradesListView.getItems().toString().contains("Shares: 10")
                        &&
                        bob.userEventCommissionLabel.getText().contains("Total commission paid")
        );

        ClientSession.setCurrentUserName("Alice");
        this.selectEvent(alice, "Mujtaba is Dead");
        alice.lmsrWinnerComboBox.getSelectionModel().select(0);
        alice.handleLmsrClose();

        ClientSession.setCurrentUserName("Bob");
        bob.refreshLoggedInUserAccount();
        check(
                "H Bob active/closed lists and payout history",
                !bob.userActiveEventsListView.getItems().toString().contains("Mujtaba")
                        &&
                        bob.userClosedEventsListView.getItems().toString().contains("Mujtaba")
                        &&
                        bob.accountHistoryListView.getItems().toString().contains("Account deposit")
        );

        ClientSession.setCurrentUserName("Alice");
        alice.uploadXmlFile(Path.of("course-materials", "EX3-multiple.xml"));
        this.selectEvent(alice, "World Cap Winner");
        alice.handleOpenEvent();
        this.selectEvent(alice, "World Cap Winner");
        alice.orderOptionComboBox.getSelectionModel().select(0);
        alice.orderTypeComboBox.getSelectionModel().select("BUY");
        alice.orderQuantityField.setText("10");
        alice.orderPriceField.setText("0.50");
        alice.handleSubmitOrder();

        ClientSession.setCurrentUserName("Bob");
        bob.loadEventsToList();
        this.selectEvent(bob, "World Cap Winner");
        bob.orderOptionComboBox.getSelectionModel().select(1);
        bob.orderTypeComboBox.getSelectionModel().select("BUY");
        bob.orderQuantityField.setText("10");
        bob.orderPriceField.setText("0.50");
        bob.handleSubmitOrder();

        this.selectEvent(bob, "World Cap Winner");
        bob.orderOptionComboBox.getSelectionModel().select(0);
        bob.orderTypeComboBox.getSelectionModel().select("BUY");
        bob.orderQuantityField.setText("5");
        bob.orderPriceField.setText("0.40");
        bob.handleSubmitOrder();

        ClientSession.setCurrentUserName("Alice");
        this.selectEvent(alice, "World Cap Winner");
        alice.orderOptionComboBox.getSelectionModel().select(0);
        alice.orderTypeComboBox.getSelectionModel().select("SELL");
        alice.orderQuantityField.setText("5");
        alice.orderPriceField.setText("0.40");
        alice.handleSubmitOrder();

        ClientSession.setCurrentUserName("Bob");
        bob.refreshLoggedInUserAccount();
        this.selectActive(bob, "World Cap");
        check(
                "J Bob private OB panel shows amount paid and shares",
                bob.userObHoldingsListView.getItems().toString().contains("Amount paid:")
                        &&
                        bob.userObHoldingsListView.getItems().toString().contains("Shares:")
        );

        ClientSession.setCurrentUserName("Alice");
        this.selectEvent(alice, "World Cap Winner");
        alice.winnerComboBox.getSelectionModel().select(0);
        alice.handleCloseEvent();

        ClientSession.setCurrentUserName("Bob");
        bob.refreshLoggedInUserAccount();
        this.selectClosed(bob, "World Cap");
        check(
                "K closed OB shows profit/loss and closed list",
                bob.userClosedEventsListView.getItems().toString().contains("World Cap")
                        &&
                        bob.userObProfitLossLabel.getText().contains("Profit/Loss:")
        );

        String bobName = bob.selectedUserNameLabel.getText();
        String bobBalance = bob.selectedUserCashLabel.getText();
        String bobHistory = bob.accountHistoryListView.getItems().toString();
        bob.usersListView.getSelectionModel().select("Alice");
        bob.handleUserSelection();
        check(
                "L selecting Alice does not switch Bob's private data",
                bob.selectedUserNameLabel.getText().equals(bobName)
                        &&
                        bob.selectedUserCashLabel.getText().equals(bobBalance)
                        &&
                        bob.accountHistoryListView.getItems().toString().equals(bobHistory)
                        &&
                        bob.selectedUserNameLabel.getText().contains("Bob")
        );

        GuessMarketHttpClient live = bob.httpClient;
        bob.httpClient = new GuessMarketHttpClient("http://127.0.0.1:1/guess-market");
        boolean crashed = false;
        try {
            bob.refreshLoggedInUserAccount();
            bob.addFundsAmountField.setText("10");
            bob.handleAddFunds();
        } catch (RuntimeException exception) {
            crashed = true;
            exception.printStackTrace();
        }
        check("M connection failure does not crash", !crashed);
        check(
                "M connection failure shows an error",
                bob.statusLabel.getText() != null
                        &&
                        bob.statusLabel.getText().toLowerCase().contains("connect")
        );
        bob.httpClient = live;

        if (this.failures == 0) {
            System.out.println("STEP5E_UI_OK");
        } else {
            System.out.println("STEP5E_UI_FAILED " + this.failures);
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


    private void selectEvent(MainController controller, String text) {
        for (int i = 0; i < controller.eventsListView.getItems().size(); i++) {
            if (controller.eventsListView.getItems().get(i).toString().contains(text)) {
                controller.eventsListView.getSelectionModel().select(i);
                controller.handleEventSelection();
                return;
            }
        }
    }


    private void selectActive(MainController controller, String text) {
        for (int i = 0; i < controller.userActiveEventsListView.getItems().size(); i++) {
            if (controller.userActiveEventsListView.getItems().get(i).toString().contains(text)) {
                controller.userActiveEventsListView.getSelectionModel().select(i);
                controller.handleUserActiveEventSelection();
                return;
            }
        }
    }


    private void selectClosed(MainController controller, String text) {
        for (int i = 0; i < controller.userClosedEventsListView.getItems().size(); i++) {
            if (controller.userClosedEventsListView.getItems().get(i).toString().contains(text)) {
                controller.userClosedEventsListView.getSelectionModel().select(i);
                controller.handleUserClosedEventSelection();
                return;
            }
        }
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
