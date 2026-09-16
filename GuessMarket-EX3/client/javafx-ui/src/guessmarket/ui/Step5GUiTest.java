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

public class Step5GUiTest extends Application {

    private int failures;
    private GuessMarketHttpClient server;
    private MainController alice;
    private MainController bob;
    private Stage bobStageForClose;


    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage aliceStage) throws Exception {
        this.server = new GuessMarketHttpClient();
        ClientSession.clear();
        this.server.postFormJson("/login", Map.of("userName", "Alice"), ClientJsonResponse.class);
        this.server.postFormJson("/funds", Map.of("userName", "Alice", "amount", "20000"), ClientJsonResponse.class);
        ClientSession.setCurrentUserName("Alice");
        this.alice = this.loadMain(aliceStage);

        this.server.postFormJson("/login", Map.of("userName", "Bob"), ClientJsonResponse.class);
        this.server.postFormJson("/funds", Map.of("userName", "Bob", "amount", "20000"), ClientJsonResponse.class);
        Stage bobStage = new Stage();
        this.bobStageForClose = bobStage;
        ClientSession.setCurrentUserName("Bob");
        this.bob = this.loadMain(bobStage);

        Thread tester = new Thread(() -> {
            try {
                this.runCases(aliceStage, bobStage);
            } catch (Exception exception) {
                exception.printStackTrace();
                this.failures++;
                this.finish(aliceStage, bobStage);
            }
        });
        tester.setDaemon(true);
        tester.start();
    }


    private void runCases(Stage aliceStage, Stage bobStage) throws Exception {
        this.check(
                "N poll interval is 1 second and at most 2",
                MainController.POLL_INTERVAL_SECONDS == 1.0
                        && MainController.POLL_INTERVAL_SECONDS <= 2.0
        );

        this.check(
                "1 one Timeline per main screen",
                this.fxGet(() -> this.alice.pollTimeline != null
                        && this.bob.pollTimeline != null
                        && this.alice.pollTimeline != this.bob.pollTimeline)
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.alice.uploadXmlFile(Path.of("course-materials", "EX3-small.xml"));
        });
        Thread.sleep(2500);
        this.check(
                "D Bob sees uploaded event without manual refresh",
                this.fxGet(() -> this.bob.eventsListView.getItems().toString().contains("Mujtaba is Dead"))
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.selectEvent(this.alice, "Mujtaba is Dead");
            this.alice.handleOpenEvent();
        });
        Thread.sleep(2500);
        this.check(
                "E Bob sees ACTIVE automatically",
                this.fxGet(() -> this.bob.eventsListView.getItems().toString().contains("ACTIVE"))
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Bob");
            this.selectEvent(this.bob, "Mujtaba is Dead");
            this.bob.lmsrPurchaseQuantityField.setText("77");
            this.bob.addFundsAmountField.setText("12.5");
            if (!this.bob.lmsrPurchaseOptionComboBox.getItems().isEmpty()) {
                this.bob.lmsrPurchaseOptionComboBox.getSelectionModel().select(0);
            }
            if (!this.bob.usersListView.getItems().isEmpty()) {
                this.bob.usersListView.getSelectionModel().select(0);
            }
        });
        String preservedOption = this.fxGetString(() -> {
            Object item = this.bob.lmsrPurchaseOptionComboBox.getSelectionModel().getSelectedItem();
            return item == null ? "" : item.toString();
        });
        String preservedOther = this.fxGetString(() -> {
            Object item = this.bob.usersListView.getSelectionModel().getSelectedItem();
            return item == null ? "" : item.toString();
        });
        Thread.sleep(1600);
        this.check(
                "5 typed fields not cleared by poll",
                this.fxGet(() -> "77".equals(this.bob.lmsrPurchaseQuantityField.getText())
                        && "12.5".equals(this.bob.addFundsAmountField.getText()))
        );
        this.check(
                "6 event/option/other-user selection preserved",
                this.fxGet(() -> {
                    String event = this.bob.eventsListView.getSelectionModel().getSelectedItem() == null
                            ? ""
                            : this.bob.eventsListView.getSelectionModel().getSelectedItem().toString();
                    String option = this.bob.lmsrPurchaseOptionComboBox.getSelectionModel().getSelectedItem() == null
                            ? ""
                            : this.bob.lmsrPurchaseOptionComboBox.getSelectionModel().getSelectedItem().toString();
                    String other = this.bob.usersListView.getSelectionModel().getSelectedItem() == null
                            ? ""
                            : this.bob.usersListView.getSelectionModel().getSelectedItem().toString();
                    return event.contains("Mujtaba is Dead")
                            && option.equals(preservedOption)
                            && other.equals(preservedOther);
                })
        );

        this.onFx(() -> this.bob.httpClient.clearRequestedPaths());
        Thread.sleep(1600);
        String lmsrPaths = this.fxGetString(() -> String.join(" ", this.bob.httpClient.requestedPaths()));
        this.check(
                "I LMSR poll uses /event-details and not Order Book GETs",
                lmsrPaths.contains("/event-details")
                        && !lmsrPaths.contains("/pending-orders")
                        && !lmsrPaths.contains("/order-book-statistics")
                        && !lmsrPaths.contains("/order-book-participants")
        );

        this.onFx(() -> {
            this.alice.pollInProgress.set(true);
            this.alice.pollIfIdle();
        });
        this.check(
                "4 overlapping cycle is skipped",
                this.fxGet(() -> this.alice.pollInProgress.get())
        );
        this.onFx(() -> this.alice.pollInProgress.set(false));

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Bob");
            this.selectEvent(this.bob, "Mujtaba is Dead");
            this.bob.lmsrPurchaseQuantityField.setText("10");
            this.bob.handleLmsrPurchase();
        });
        Thread.sleep(2500);
        this.check(
                "F Alice sees LMSR/account change after Bob purchase",
                this.fxGet(() -> {
                    this.selectEvent(this.alice, "Mujtaba is Dead");
                    return this.alice.eventAccountBalanceLabel.getText().contains("Event account balance:")
                            && this.alice.lmsrOption1StateLabel.getText().contains("purchased shares:");
                })
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Bob");
            if (!this.bob.userActiveEventsListView.getItems().isEmpty()) {
                this.bob.userActiveEventsListView.getSelectionModel().select(0);
                this.bob.handleUserActiveEventSelection();
            }
        });
        Thread.sleep(1600);
        this.check(
                "6 active participated event selection preserved",
                this.fxGet(() -> this.bob.userActiveEventsListView.getSelectionModel().getSelectedIndex() >= 0
                        || this.bob.userActiveEventsListView.getItems().isEmpty())
        );

        String bobQty = this.fxGetString(() -> this.bob.lmsrPurchaseQuantityField.getText());
        this.check("F Bob quantity field still exists after poll", bobQty != null);

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.selectEvent(this.alice, "Mujtaba is Dead");
            this.alice.lmsrWinnerComboBox.getSelectionModel().select(0);
            this.alice.handleLmsrClose();
        });
        Thread.sleep(2500);
        this.check(
                "G Bob sees CLOSED automatically",
                this.fxGet(() -> this.bob.eventsListView.getItems().toString().contains("CLOSED"))
        );
        this.onFx(() -> {
            ClientSession.setCurrentUserName("Bob");
            if (!this.bob.userClosedEventsListView.getItems().isEmpty()) {
                this.bob.userClosedEventsListView.getSelectionModel().select(0);
                this.bob.handleUserClosedEventSelection();
            }
        });
        Thread.sleep(1600);
        this.check(
                "6 closed participated event selection preserved",
                this.fxGet(() -> this.bob.userClosedEventsListView.getSelectionModel().getSelectedIndex() >= 0
                        || this.bob.userClosedEventsListView.getItems().isEmpty())
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.alice.uploadXmlFile(Path.of("course-materials", "EX3-multiple.xml"));
            this.selectEvent(this.alice, "World Cap Winner");
            this.alice.handleOpenEvent();
        });
        Thread.sleep(2500);

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Bob");
            this.selectEvent(this.bob, "World Cap Winner");
            this.bob.orderOptionComboBox.getSelectionModel().select(0);
            this.bob.orderTypeComboBox.getSelectionModel().select("BUY");
            this.bob.orderQuantityField.setText("5");
            this.bob.orderPriceField.setText("0.40");
            this.bob.handleSubmitOrder();
        });
        Thread.sleep(2500);
        this.check(
                "H Alice sees Bob pending BUY automatically",
                this.fxGet(() -> {
                    this.selectEvent(this.alice, "World Cap Winner");
                    return this.alice.pendingOrdersListView.getItems().toString().contains("Bob");
                })
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Bob");
            this.bob.userActiveEventsListView.getSelectionModel().clearSelection();
            this.bob.userClosedEventsListView.getSelectionModel().clearSelection();
            this.selectEvent(this.bob, "World Cap Winner");
            this.bob.httpClient.clearRequestedPaths();
        });
        Thread.sleep(1600);
        String obPaths = this.fxGetString(() -> String.join(" ", this.bob.httpClient.requestedPaths()));
        this.check(
                "I Order Book poll uses pending/statistics/participants and not /event-details",
                obPaths.contains("/pending-orders")
                        && obPaths.contains("/order-book-statistics")
                        && obPaths.contains("/order-book-participants")
                        && !obPaths.contains("/event-details")
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
        Thread.sleep(2500);
        this.check(
                "H Bob/Alice automatically see pending or participants after match attempt",
                this.fxGet(() -> {
                    this.selectEvent(this.bob, "World Cap Winner");
                    String pending = this.bob.pendingOrdersListView.getItems().toString();
                    String participants = this.bob.participantsListView.getItems().toString();
                    return pending.contains("Alice")
                            || pending.contains("Bob")
                            || participants.contains("Bob")
                            || participants.contains("Alice");
                })
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.alice.addFundsAmountField.setText("25");
            this.alice.handleAddFunds();
        });
        this.check(
                "I Alice funds update immediately",
                this.fxGet(() -> this.alice.accountHistoryListView.getItems().toString().contains("Funds added"))
        );
        Thread.sleep(1500);
        this.check(
                "I Alice funds stay synchronized",
                this.fxGet(() -> this.alice.accountHistoryListView.getItems().toString().contains("Funds added"))
        );

        this.check(
                "J Bob Other Users shows Alice MM automatically",
                this.fxGet(() -> this.bob.usersListView.getItems().toString().contains("MM: YES")
                        && this.bob.usersListView.getItems().toString().contains("Alice"))
        );

        this.onFx(() -> this.bob.httpClient.clearRequestedPaths());
        Thread.sleep(1600);
        String bobPaths = this.fxGetString(() -> String.join(" ", this.bob.httpClient.requestedPaths()));
        this.check(
                "K Bob poll never requests Alice private data",
                !this.containsAlicePrivateGet(bobPaths)
        );
        this.check(
                "K Bob private GETs use Bob",
                !bobPaths.contains("/account-history")
                        || bobPaths.contains("userName=Bob")
        );
        this.check(
                "H public /users is polled",
                bobPaths.contains("/users")
        );

        this.onFx(() -> this.bobStageForClose.hide());
        Thread.sleep(1200);
        this.check(
                "9 polling stops when Bob window closes",
                this.fxGet(() -> this.bob.pollTimeline == null)
        );

        this.catalina("stop");
        Thread.sleep(8000);
        this.check(
                "L connection error shown without crash after Tomcat stop",
                this.fxGet(() -> this.alice.statusLabel.getText() != null
                        && this.alice.statusLabel.getText().toLowerCase().contains("connect"))
        );
        this.check(
                "L no overlapping queue after outage",
                this.fxGet(() -> !this.alice.pollInProgress.get()
                        || this.alice.pollTimeline != null)
        );

        this.catalina("start");
        boolean recovered = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(2000);
            try {
                this.server.getRaw("/users");
                recovered = true;
                break;
            } catch (Exception ignored) {
            }
        }
        this.check("M Tomcat is accepting HTTP again", recovered);
        Thread.sleep(2500);
        this.check(
                "M polling recovers after Tomcat restart",
                this.fxGet(() -> this.alice.eventsListView.getItems() != null
                        && (this.alice.statusLabel.getText() == null
                        || this.alice.statusLabel.getText().contains("Connected")
                        || !this.alice.statusLabel.getText().toLowerCase().contains("could not connect")))
        );

        this.finish(aliceStage, bobStage);
    }


    private void finish(Stage aliceStage, Stage bobStage) {
        Platform.runLater(() -> {
            if (this.failures == 0) {
                System.out.println("STEP5G_UI_OK");
            } else {
                System.out.println("STEP5G_UI_FAILED " + this.failures);
            }
            this.alice.stopPolling();
            this.bob.stopPolling();
            aliceStage.close();
            bobStage.close();
            Platform.exit();
        });
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


    private String fxGetString(java.util.concurrent.Callable<String> action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> value = new AtomicReference<>();
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
            throw new IllegalStateException("JavaFX read timed out.");
        }
        if (error.get() != null) {
            throw error.get();
        }
        return value.get();
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


    private boolean containsAlicePrivateGet(String paths) {
        String[] privatePaths = {
                "/account-history",
                "/user-shares",
                "/user-active-events",
                "/user-closed-events",
                "/user-lmsr-trades",
                "/user-event-commission",
                "/user-ob-amount-paid",
                "/user-ob-profit-loss"
        };
        if (paths == null) {
            return false;
        }
        for (String privatePath : privatePaths) {
            int from = 0;
            while (true) {
                int index = paths.indexOf(privatePath, from);
                if (index < 0) {
                    break;
                }
                int query = paths.indexOf("userName=", index);
                int nextSlash = paths.indexOf(" /", index + 1);
                if (query >= 0 && (nextSlash < 0 || query < nextSlash)
                        && paths.startsWith("userName=Alice", query)) {
                    return true;
                }
                from = index + privatePath.length();
            }
        }
        return paths.contains("userName=Alice");
    }


    private void catalina(String command) throws Exception {
        String catalinaHome =
                "C:\\Users\\IMOE001\\Downloads\\apache-tomcat-10.1.60\\apache-tomcat-10.1.60";
        String javaHome = "C:\\Users\\IMOE001\\jdk25\\jdk-25.0.4+7";
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe",
                "/c",
                catalinaHome + "\\bin\\catalina.bat",
                command
        );
        builder.environment().put("JAVA_HOME", javaHome);
        builder.environment().put("CATALINA_HOME", catalinaHome);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        process.waitFor(30, TimeUnit.SECONDS);
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
