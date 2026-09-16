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

public class Step5DUiTest extends Application {

    private int failures;
    private GuessMarketHttpClient server;


    public static void main(
            String[] args) {

        launch(args);
    }


    @Override
    public void start(
            Stage aliceStage)
            throws Exception {

        this.server = new GuessMarketHttpClient();

        ClientSession.clear();
        this.server.postFormJson(
                "/login",
                Map.of("userName", "Alice"),
                ClientJsonResponse.class
        );
        this.server.postFormJson(
                "/funds",
                Map.of("userName", "Alice", "amount", "20000"),
                ClientJsonResponse.class
        );
        ClientSession.setCurrentUserName("Alice");

        MainController alice = this.loadMain(aliceStage);
        check("B Alice main screen opens", alice.eventsListView != null);

        alice.uploadXmlFile(Path.of("course-materials", "EX3-small.xml"));
        check(
                "C uploaded LMSR event appears",
                this.rowContains(alice, "Mujtaba is Dead")
        );

        this.selectRow(alice, "Mujtaba is Dead");
        alice.handleOpenEvent();
        check(
                "D Alice opened the LMSR event",
                alice.eventStatusLabel.getText().contains("ACTIVE")
                        &&
                        alice.statusLabel.getText().toLowerCase().contains("open")
        );

        this.server.postFormJson(
                "/login",
                Map.of("userName", "Bob"),
                ClientJsonResponse.class
        );
        this.server.postFormJson(
                "/funds",
                Map.of("userName", "Bob", "amount", "20000"),
                ClientJsonResponse.class
        );

        Stage bobStage = new Stage();
        ClientSession.setCurrentUserName("Bob");
        MainController bob = this.loadMain(bobStage);
        check("E Bob main screen opens", bob.eventsListView != null);

        this.selectRow(bob, "Mujtaba is Dead");
        bob.lmsrPurchaseQuantityField.setText("10");
        bob.handleLmsrPurchase();

        UserSharesResponse bobShares =
                this.server.getJson(
                        "/user-shares?userName=Bob&eventId=1",
                        UserSharesResponse.class
                );
        check(
                "G Bob bought 10 LMSR shares",
                bob.statusLabel.getText().toLowerCase().contains("purchase")
                        &&
                        bobShares.getShares() != null
                        &&
                        bobShares.getShares().get(0) == 10
                        &&
                        bob.selectedUserHoldingsLabel.getText().contains("10 shares")
                        &&
                        bob.eventAccountBalanceLabel.getText().contains("Event account balance:")
        );

        bob.handleOpenEvent();
        String bobOpenError = bob.statusLabel.getText();
        bob.lmsrWinnerComboBox.getSelectionModel().select(0);
        bob.handleLmsrClose();
        String bobCloseError = bob.statusLabel.getText();
        check(
                "H Bob cannot open/close Alice's event",
                bobOpenError != null
                        &&
                        !bobOpenError.toLowerCase().contains("successfully")
                        &&
                        bobCloseError != null
                        &&
                        !bobCloseError.toLowerCase().contains("successfully")
        );

        ClientSession.setCurrentUserName("Alice");
        alice.uploadXmlFile(Path.of("course-materials", "EX3-multiple.xml"));
        this.selectRow(alice, "World Cap Winner");
        alice.handleOpenEvent();
        check(
                "J Alice opened the Order Book event",
                alice.eventStatusLabel.getText().contains("ACTIVE")
                        &&
                        this.rowContains(alice, "World Cap Winner")
        );

        this.selectRow(alice, "World Cap Winner");
        alice.orderOptionComboBox.getSelectionModel().select(0);
        alice.orderTypeComboBox.getSelectionModel().select("BUY");
        alice.orderQuantityField.setText("10");
        alice.orderPriceField.setText("0.50");
        alice.handleSubmitOrder();

        ClientSession.setCurrentUserName("Bob");
        bob.loadEventsToList();
        this.selectRow(bob, "World Cap Winner");
        bob.orderOptionComboBox.getSelectionModel().select(1);
        bob.orderTypeComboBox.getSelectionModel().select("BUY");
        bob.orderQuantityField.setText("10");
        bob.orderPriceField.setText("0.50");
        bob.handleSubmitOrder();

        this.selectRow(bob, "World Cap Winner");
        bob.orderOptionComboBox.getSelectionModel().select(0);
        bob.orderTypeComboBox.getSelectionModel().select("BUY");
        bob.orderQuantityField.setText("5");
        bob.orderPriceField.setText("0.40");
        bob.handleSubmitOrder();
        boolean bobBuyPending =
                bob.pendingOrdersListView.getItems().toString().contains("Bob")
                        &&
                        bob.pendingOrdersListView.getItems().toString().contains("BUY");
        check("K Bob BUY is visible in the book", bobBuyPending);

        ClientSession.setCurrentUserName("Alice");
        alice.loadEventsToList();
        this.selectRow(alice, "World Cap Winner");
        alice.orderOptionComboBox.getSelectionModel().select(0);
        alice.orderTypeComboBox.getSelectionModel().select("SELL");
        alice.orderQuantityField.setText("5");
        alice.orderPriceField.setText("0.40");
        alice.handleSubmitOrder();
        String pendingAfterMatch =
                alice.pendingOrdersListView.getItems().toString();
        check(
                "L Alice matching SELL refreshed book/shares",
                alice.statusLabel.getText() != null
                        &&
                        !alice.statusLabel.getText().toLowerCase().contains("connect")
                        &&
                        !alice.participantsListView.getItems().isEmpty()
                        &&
                        alice.selectedUserHoldingsLabel.getText().contains("shares")
                        &&
                        !pendingAfterMatch.contains("Exception")
        );

        this.selectRow(alice, "Mujtaba is Dead");
        alice.lmsrWinnerComboBox.getSelectionModel().select(0);
        alice.handleLmsrClose();
        check(
                "M LMSR close shows CLOSED and winner",
                alice.eventStatusLabel.getText().contains("CLOSED")
                        &&
                        alice.statusLabel.getText().contains("Winner:")
        );

        this.selectRow(alice, "World Cap Winner");
        alice.winnerComboBox.getSelectionModel().select(0);
        alice.handleCloseEvent();
        check(
                "N Order Book close shows CLOSED and winner",
                alice.eventStatusLabel.getText().contains("CLOSED")
                        &&
                        alice.statusLabel.getText().contains("Winner:")
        );

        GuessMarketHttpClient live = alice.httpClient;
        alice.httpClient = new GuessMarketHttpClient("http://127.0.0.1:1/guess-market");
        boolean crashed = false;
        try {
            alice.handleOpenEvent();
        } catch (RuntimeException exception) {
            crashed = true;
            exception.printStackTrace();
        }
        check("O connection failure does not crash", !crashed);
        check(
                "O connection failure shows an error",
                alice.statusLabel.getText() != null
                        &&
                        alice.statusLabel.getText().toLowerCase().contains("connect")
        );
        alice.httpClient = live;

        if (this.failures == 0) {
            System.out.println("STEP5D_UI_OK");
        } else {
            System.out.println("STEP5D_UI_FAILED " + this.failures);
        }

        aliceStage.close();
        bobStage.close();
        Platform.exit();
    }


    private MainController loadMain(Stage stage) throws Exception {
        FXMLLoader loader =
                new FXMLLoader(
                        GuessMarketApplication.class.getResource("/main-view.fxml")
                );
        Parent root = loader.load();
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
        return loader.getController();
    }


    private boolean rowContains(MainController controller, String text) {
        for (Object item : controller.eventsListView.getItems()) {
            if (item.toString().contains(text)) {
                return true;
            }
        }
        return false;
    }


    private void selectRow(MainController controller, String text) {
        for (int i = 0; i < controller.eventsListView.getItems().size(); i++) {
            if (controller.eventsListView.getItems().get(i).toString().contains(text)) {
                controller.eventsListView.getSelectionModel().select(i);
                controller.handleEventSelection();
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
