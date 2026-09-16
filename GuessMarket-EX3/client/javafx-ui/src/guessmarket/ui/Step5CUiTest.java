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

public class Step5CUiTest extends Application {

    private int failures;
    private GuessMarketHttpClient server;


    public static void main(
            String[] args) {

        launch(args);
    }


    @Override
    public void start(
            Stage stage)
            throws Exception {

        this.server =
                new GuessMarketHttpClient();

        ClientSession.clear();
        this.server.postFormJson(
                "/login",
                Map.of("userName", "Alice"),
                ClientJsonResponse.class
        );
        ClientSession.setCurrentUserName("Alice");

        FXMLLoader loader =
                new FXMLLoader(
                        GuessMarketApplication.class.getResource(
                                "/main-view.fxml"
                        )
                );
        Parent root = loader.load();
        MainController controller = loader.getController();
        stage.setScene(new Scene(root, 900, 600));
        stage.show();

        check(
                "C main screen opens",
                root.lookup("#eventsListView") != null
        );
        check(
                "D empty event list does not crash",
                controller.eventsListView.getItems().isEmpty()
        );

        this.server.postFormJson(
                "/funds",
                Map.of(
                        "userName", "Alice",
                        "amount", "20000"
                ),
                ClientJsonResponse.class
        );
        this.server.postMultipartFile(
                "/upload",
                Map.of("userName", "Alice"),
                "file",
                Path.of("course-materials", "EX3-small.xml")
        );
        this.server.postFormJson(
                "/open-event",
                Map.of(
                        "userName", "Alice",
                        "eventId", "1"
                ),
                ClientJsonResponse.class
        );

        controller.loadEventsToList();
        check(
                "F LMSR event appears after server upload",
                !controller.eventsListView.getItems().isEmpty()
                        &&
                        controller.eventsListView.getItems().get(0).toString().contains(
                                "Mujtaba is Dead"
                        )
        );

        controller.httpClient.clearRequestedPaths();
        controller.eventsListView.getSelectionModel().select(0);
        controller.handleEventSelection();

        String lmsrPaths =
                String.join(
                        " ",
                        controller.httpClient.requestedPaths()
                );
        check(
                "G summary comes from GET /events",
                controller.eventNameLabel.getText().contains(
                        "Mujtaba is Dead"
                )
        );
        check(
                "G account comes from /event-account",
                controller.eventAccountBalanceLabel.getText().startsWith(
                        "Event account balance:"
                )
                        &&
                        lmsrPaths.contains("/event-account")
        );
        check(
                "G LMSR details come from /event-details",
                lmsrPaths.contains("/event-details")
                        &&
                        controller.lmsrOption1StateLabel.getText().contains(
                                "current value:"
                        )
        );
        check(
                "G Alice shares come from /user-shares",
                lmsrPaths.contains("/user-shares")
                        &&
                        controller.selectedUserHoldingsLabel.getText().contains(
                                "shares"
                        )
        );

        this.server.postFormJson(
                "/lmsr-purchase",
                Map.of(
                        "userName", "Alice",
                        "eventId", "1",
                        "optionIndex", "0",
                        "quantity", "1"
                ),
                ClientJsonResponse.class
        );
        controller.handleEventSelection();
        String aliceHoldings =
                controller.selectedUserHoldingsLabel.getText();
        check(
                "G Alice holdings include a purchased share",
                aliceHoldings.contains("1 shares")
                        ||
                        aliceHoldings.contains(": 1 share")
        );

        this.server.postMultipartFile(
                "/upload",
                Map.of("userName", "Alice"),
                "file",
                Path.of("course-materials", "EX3-multiple.xml")
        );
        this.server.postFormJson(
                "/open-event",
                Map.of(
                        "userName", "Alice",
                        "eventId", "2"
                ),
                ClientJsonResponse.class
        );

        controller.loadEventsToList();
        int orderBookIndex = -1;
        for (int i = 0; i < controller.eventsListView.getItems().size(); i++) {
            if (controller.eventsListView.getItems().get(i).toString().contains(
                    "ORDER_BOOK"
            )) {
                orderBookIndex = i;
                break;
            }
        }

        check(
                "H Order Book event is listed",
                orderBookIndex >= 0
        );

        controller.httpClient.clearRequestedPaths();
        controller.eventsListView.getSelectionModel().select(orderBookIndex);
        controller.handleEventSelection();
        String obPaths =
                String.join(
                        " ",
                        controller.httpClient.requestedPaths()
                );

        check(
                "I Order Book does not call /event-details",
                !obPaths.contains("/event-details")
        );
        check(
                "I Order Book uses account/pending/statistics/participants/shares",
                obPaths.contains("/event-account")
                        &&
                        obPaths.contains("/pending-orders")
                        &&
                        obPaths.contains("/order-book-statistics")
                        &&
                        obPaths.contains("/order-book-participants")
                        &&
                        obPaths.contains("/user-shares")
        );
        check(
                "I LMSR details box is hidden for Order Book",
                !controller.lmsrDetailsBox.isVisible()
        );
        check(
                "I pending/statistics widgets are populated",
                !controller.pendingOrdersListView.getItems().isEmpty()
                        &&
                        ("N/A".equals(controller.lastPriceLabel.getText())
                                || controller.lastPriceLabel.getText() != null)
        );

        this.server.postFormJson(
                "/login",
                Map.of("userName", "Bob"),
                ClientJsonResponse.class
        );
        ClientSession.setCurrentUserName("Bob");
        controller.showSelectedUserHoldings();
        String bobHoldings =
                controller.selectedUserHoldingsLabel.getText();
        check(
                "J Bob sees Bob shares, not Alice's",
                bobHoldings.contains("0 shares")
                        &&
                        !bobHoldings.equals(aliceHoldings)
        );

        GuessMarketHttpClient liveClient =
                controller.httpClient;
        controller.httpClient =
                new GuessMarketHttpClient(
                        "http://127.0.0.1:1/guess-market"
                );
        boolean crashed = false;
        try {
            controller.loadEventsToList();
        } catch (RuntimeException exception) {
            crashed = true;
            exception.printStackTrace();
        }
        check(
                "K connection failure does not crash",
                !crashed
        );
        check(
                "K connection failure shows an error",
                controller.statusLabel.getText() != null
                        &&
                        !controller.statusLabel.getText().isBlank()
                        &&
                        (controller.statusLabel.getText().toLowerCase().contains(
                                "connect"
                        )
                                ||
                                controller.statusLabel.getText().toLowerCase().contains(
                                        "failed"
                                )
                                ||
                                controller.statusLabel.getText().toLowerCase().contains(
                                        "http"
                                ))
        );
        controller.httpClient = liveClient;

        if (this.failures == 0) {
            System.out.println("STEP5C_UI_OK");
        } else {
            System.out.println("STEP5C_UI_FAILED " + this.failures);
        }

        stage.close();
        Platform.exit();
    }


    private void check(
            String name,
            boolean ok) {

        if (ok) {
            System.out.println("PASS " + name);
        } else {
            this.failures++;
            System.out.println("FAIL " + name);
        }
    }
}
