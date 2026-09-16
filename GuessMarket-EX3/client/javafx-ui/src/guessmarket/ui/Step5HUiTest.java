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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Step5HUiTest extends Application {

    private int failures;
    private GuessMarketHttpClient server;
    private MainController alice;
    private MainController bob;


    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage aliceStage) throws Exception {
        this.server = new GuessMarketHttpClient();
        ClientSession.clear();

        boolean aliceReady = false;
        try {
            ClientJsonResponse firstLogin =
                    this.server.postFormJson("/login", Map.of("userName", "Alice"), ClientJsonResponse.class);
            aliceReady = firstLogin != null && firstLogin.isSuccess();
        } catch (guessmarket.client.http.GuessMarketHttpException exception) {
            aliceReady =
                    exception.getMessage() != null
                            && exception.getMessage().toLowerCase().contains("already");
        }
        boolean duplicateRejected = false;
        try {
            this.server.postFormJson("/login", Map.of("userName", "Alice"), ClientJsonResponse.class);
        } catch (guessmarket.client.http.GuessMarketHttpException exception) {
            duplicateRejected =
                    exception.getMessage() != null
                            && exception.getMessage().toLowerCase().contains("already");
        }
        this.check("A Alice login available", aliceReady);
        this.check("unique-name retry: second Alice login is rejected", duplicateRejected);

        this.server.postFormJson("/funds", Map.of("userName", "Alice", "amount", "20000"), ClientJsonResponse.class);
        ClientSession.setCurrentUserName("Alice");
        this.alice = this.loadMain(aliceStage);

        Thread tester = new Thread(() -> {
            try {
                this.runCases(aliceStage);
            } catch (Exception exception) {
                exception.printStackTrace();
                this.failures++;
                this.finish(aliceStage);
            }
        });
        tester.setDaemon(true);
        tester.start();
    }


    private void runCases(Stage aliceStage) throws Exception {
        this.check("A Alice main screen", this.fxGet(() -> this.alice.eventsListView != null));
        this.check(
                "one Timeline / 1s poll",
                this.fxGet(() -> this.alice.pollTimeline != null)
                        && MainController.POLL_INTERVAL_SECONDS == 1.0
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.alice.addFundsAmountField.setText("50");
            this.alice.handleAddFunds();
        });
        this.check(
                "B Alice add funds",
                this.fxGet(() -> this.alice.accountHistoryListView.getItems().toString().contains("Funds added"))
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.alice.uploadXmlFile(Path.of("course-materials", "EX3-small.xml"));
        });
        Thread.sleep(2200);
        this.check(
                "C Alice uploaded LMSR",
                this.fxGet(() -> this.alice.eventsListView.getItems().toString().contains("Mujtaba is Dead"))
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.selectEvent(this.alice, "Mujtaba is Dead");
            this.alice.handleOpenEvent();
        });
        this.check(
                "D Alice opened LMSR",
                this.fxGet(() -> this.alice.eventStatusLabel.getText().contains("ACTIVE"))
        );

        try {
            this.server.postFormJson("/login", Map.of("userName", "Bob"), ClientJsonResponse.class);
        } catch (guessmarket.client.http.GuessMarketHttpException ignored) {
        }
        this.server.postFormJson("/funds", Map.of("userName", "Bob", "amount", "20000"), ClientJsonResponse.class);
        this.bob = this.fxGetController(() -> {
            ClientSession.setCurrentUserName("Bob");
            return this.loadMain(new Stage());
        });
        this.check("E Bob second client", this.bob != null && this.bob.eventsListView != null);

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Bob");
            this.bob.addFundsAmountField.setText("25");
            this.bob.handleAddFunds();
        });
        this.check(
                "F Bob add funds",
                this.fxGet(() -> this.bob.accountHistoryListView.getItems().toString().contains("Funds added"))
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Bob");
            this.selectEvent(this.bob, "Mujtaba is Dead");
            this.bob.lmsrPurchaseQuantityField.setText("10");
            this.bob.handleLmsrPurchase();
        });
        Thread.sleep(2200);
        this.check(
                "G/H Alice auto-sees Bob LMSR purchase",
                this.fxGet(() -> {
                    this.selectEvent(this.alice, "Mujtaba is Dead");
                    return this.alice.lmsrOption1StateLabel.getText().contains("purchased shares:");
                })
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.alice.uploadXmlFile(Path.of("course-materials", "EX3-multiple.xml"));
            this.selectEvent(this.alice, "World Cap Winner");
            this.alice.handleOpenEvent();
        });
        Thread.sleep(2200);
        this.check(
                "I Bob auto-sees Order Book ACTIVE",
                this.fxGet(() -> this.bob.eventsListView.getItems().toString().contains("World Cap Winner")
                        && this.bob.eventsListView.getItems().toString().contains("ACTIVE"))
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Bob");
            this.selectEvent(this.bob, "World Cap Winner");
            this.bob.orderOptionComboBox.getSelectionModel().select(0);
            this.bob.orderTypeComboBox.getSelectionModel().select("BUY");
            this.bob.orderQuantityField.setText("5");
            this.bob.orderPriceField.setText("0.40");
            this.bob.handleSubmitOrder();
        });
        Thread.sleep(2200);
        this.check(
                "J Alice auto-sees Bob pending BUY",
                this.fxGet(() -> {
                    this.selectEvent(this.alice, "World Cap Winner");
                    return this.alice.pendingOrdersListView.getItems().toString().contains("Bob");
                })
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.selectEvent(this.alice, "World Cap Winner");
            this.alice.orderOptionComboBox.getSelectionModel().select(0);
            this.alice.orderTypeComboBox.getSelectionModel().select("SELL");
            this.alice.orderQuantityField.setText("5");
            this.alice.orderPriceField.setText("0.40");
            this.alice.handleSubmitOrder();
        });
        Thread.sleep(2200);
        this.check(
                "J Order Book still visible after match attempt",
                this.fxGet(() -> this.bob.pendingOrdersListView.getItems() != null)
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.selectEvent(this.alice, "Mujtaba is Dead");
            this.alice.lmsrWinnerComboBox.getSelectionModel().select(0);
            this.alice.handleLmsrClose();
        });
        Thread.sleep(2200);
        this.check(
                "K Bob auto-sees CLOSED",
                this.fxGet(() -> this.bob.eventsListView.getItems().toString().contains("CLOSED"))
        );

        this.check(
                "L Alice private history still present",
                this.fxGet(() -> this.alice.accountHistoryListView.getItems().toString().contains("Funds added")
                        && this.alice.selectedUserNameLabel.getText().contains("Alice"))
        );
        this.check(
                "M Bob Other Users shows Alice",
                this.fxGet(() -> this.bob.usersListView.getItems().toString().contains("Alice")
                        && this.bob.usersListView.getItems().toString().contains("MM:"))
        );

        GuessMarketHttpClient live = this.alice.httpClient;
        this.onFx(() -> this.alice.httpClient =
                new GuessMarketHttpClient("http://127.0.0.1:1/guess-market"));
        Thread.sleep(7000);
        this.check(
                "N no crash on temporary server failure",
                this.fxGet(() -> this.alice.statusLabel.getText() != null
                        && this.alice.statusLabel.getText().toLowerCase().contains("connect"))
        );
        this.onFx(() -> this.alice.httpClient = live);

        this.onFx(() -> {
            this.alice.stopPolling();
            if (this.bob != null) {
                this.bob.stopPolling();
            }
        });
        this.finish(aliceStage);
    }


    private void finish(Stage aliceStage) {
        Platform.runLater(() -> {
            if (this.failures == 0) {
                System.out.println("STEP5H_UI_OK");
            } else {
                System.out.println("STEP5H_UI_FAILED " + this.failures);
            }
            aliceStage.close();
            Platform.exit();
        });
    }


    private MainController loadMain(Stage stage) throws Exception {
        FXMLLoader loader =
                new FXMLLoader(GuessMarketApplication.class.getResource("/main-view.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 900, 600));
        stage.setMinWidth(700);
        stage.setMinHeight(480);
        stage.show();
        return loader.getController();
    }


    private MainController fxGetController(java.util.concurrent.Callable<MainController> action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<MainController> value = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                value.set(action.call());
            } catch (Exception exception) {
                error.set(exception);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(20, TimeUnit.SECONDS)) {
            throw new IllegalStateException("loadMain timed out.");
        }
        if (error.get() != null) {
            throw error.get();
        }
        return value.get();
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


    private void onFx(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Exception exception) {
                error.set(exception);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(20, TimeUnit.SECONDS)) {
            throw new IllegalStateException("JavaFX action timed out.");
        }
        if (error.get() != null) {
            throw error.get();
        }
    }


    private boolean fxGet(java.util.concurrent.Callable<Boolean> action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean value = new AtomicBoolean(false);
        AtomicReference<Exception> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Boolean result = action.call();
                value.set(result != null && result);
            } catch (Exception exception) {
                error.set(exception);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(20, TimeUnit.SECONDS)) {
            throw new IllegalStateException("JavaFX read timed out.");
        }
        if (error.get() != null) {
            throw error.get();
        }
        return value.get();
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
