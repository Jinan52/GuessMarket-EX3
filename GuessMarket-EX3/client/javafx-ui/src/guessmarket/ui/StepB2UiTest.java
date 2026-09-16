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

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class StepB2UiTest extends Application {

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
        ClientSession.setCurrentUserName("Alice");
        this.alice = this.loadMain(aliceStage);

        this.server.postFormJson("/login", Map.of("userName", "Bob"), ClientJsonResponse.class);
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
        this.check("A Alice and Bob windows opened", this.alice != null && this.bob != null);
        this.check(
                "B/C chat controls exist after login",
                this.fxGet(() -> this.alice.chatListView != null
                        && this.alice.chatMessageField != null
                        && this.bob.chatListView != null)
        );
        this.check(
                "D both chat lists initially empty",
                this.fxGet(() -> this.alice.chatListView.getItems().isEmpty()
                        && this.bob.chatListView.getItems().isEmpty())
        );
        this.check(
                "M one Timeline per window at 1s",
                this.fxGet(() -> this.alice.pollTimeline != null
                        && this.bob.pollTimeline != null
                        && this.alice.pollTimeline != this.bob.pollTimeline
                        && this.alice.pollTimeline.getKeyFrames().size() == 1)
                        && MainController.POLL_INTERVAL_SECONDS == 1.0
        );

        this.onFx(() -> this.alice.httpClient.clearRequestedPaths());
        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.alice.chatMessageField.setText("Hello Bob");
            this.alice.handleChatSend();
        });
        boolean aliceSawOwn = false;
        for (int i = 0; i < 8; i++) {
            if (this.fxGet(() -> this.aliceChatContains("Alice")
                    && this.aliceChatContains("Hello Bob"))) {
                aliceSawOwn = true;
                break;
            }
            Thread.sleep(200);
        }
        String aliceSendPaths =
                this.fxGetString(() -> String.join(" ", this.alice.httpClient.requestedPaths()));
        this.check(
                "E POST /chat-send from Alice",
                aliceSendPaths.contains("/chat-send")
        );
        this.check(
                "E Alice sees own Hello Bob immediately",
                aliceSawOwn
        );
        this.check(
                "E Alice input cleared after send",
                this.fxGet(() -> this.alice.chatMessageField.getText() == null
                        || this.alice.chatMessageField.getText().isEmpty())
        );

        boolean bobSawAlice = false;
        for (int i = 0; i < 8; i++) {
            Thread.sleep(400);
            if (this.fxGet(() -> this.bobChatContains("Alice")
                    && this.bobChatContains("Hello Bob"))) {
                bobSawAlice = true;
                break;
            }
        }
        this.check("E Bob sees Alice automatically", bobSawAlice);

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Bob");
            this.bob.chatMessageField.setText("Hi Alice");
            this.bob.handleChatSend();
        });
        boolean bothOrdered = false;
        for (int i = 0; i < 8; i++) {
            Thread.sleep(400);
            if (this.fxGet(() -> this.orderedTwoMessages(this.alice)
                    && this.orderedTwoMessages(this.bob))) {
                bothOrdered = true;
                break;
            }
        }
        this.check("F both clients show Alice then Bob", bothOrdered);

        this.onFx(() -> this.alice.httpClient.clearRequestedPaths());
        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.alice.chatMessageField.setText("   ");
            this.alice.handleChatSend();
        });
        String blankPaths =
                this.fxGetString(() -> String.join(" ", this.alice.httpClient.requestedPaths()));
        this.check(
                "G blank message blocked locally",
                !blankPaths.contains("/chat-send")
                        && this.fxGet(() -> this.alice.chatStatusLabel.getText() != null
                        && this.alice.chatStatusLabel.getText().toLowerCase().contains("empty"))
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            if (!this.alice.usersListView.getItems().isEmpty()) {
                this.alice.usersListView.getSelectionModel().select(0);
                this.alice.handleUserSelection();
            }
            this.alice.chatMessageField.setText("Still Alice");
            this.alice.handleChatSend();
        });
        boolean stillAlice = false;
        for (int i = 0; i < 8; i++) {
            Thread.sleep(400);
            if (this.fxGet(() -> this.aliceChatContains("Still Alice")
                    && this.aliceChatContains("Alice")
                    && this.bobChatContains("Still Alice"))) {
                stillAlice = true;
                break;
            }
        }
        this.check("H Other Users selection does not change sender", stillAlice);
        this.check(
                "H Still Alice is from Alice not Bob",
                this.fxGet(() -> {
                    for (Object item : this.alice.chatListView.getItems()) {
                        String line = item.toString();
                        if (line.contains("Still Alice") && line.contains("Alice:")
                                && !line.contains("Bob: Still Alice")) {
                            return true;
                        }
                    }
                    return false;
                })
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.alice.chatMessageField.setText("unsent draft");
        });
        this.onFx(() -> this.alice.httpClient.clearRequestedPaths());
        Thread.sleep(2500);
        String pollPaths =
                this.fxGetString(() -> String.join(" ", this.alice.httpClient.requestedPaths()));
        this.check(
                "I typed text preserved across polls",
                this.fxGet(() -> "unsent draft".equals(this.alice.chatMessageField.getText()))
        );
        this.check(
                "I polling GETs /chat and does not POST send",
                pollPaths.contains("/chat") && !pollPaths.contains("/chat-send")
        );

        this.catalina("stop");
        boolean outageShown = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(1000);
            if (this.fxGet(() -> {
                String status = this.alice.statusLabel.getText();
                if (status == null) {
                    return false;
                }
                String lower = status.toLowerCase();
                return lower.contains("connect")
                        || lower.contains("failed")
                        || lower.contains("refused");
            })) {
                outageShown = true;
                break;
            }
        }
        this.check(
                "J typed text remains after Tomcat stop",
                this.fxGet(() -> "unsent draft".equals(this.alice.chatMessageField.getText()))
        );
        this.check(
                "J connection error without crash",
                outageShown
                        && this.fxGet(() -> this.alice.chatListView != null
                        && this.alice.pollTimeline != null)
        );

        this.catalina("start");
        boolean recovered = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(2000);
            try {
                this.server.getRaw("/ping");
                recovered = true;
                break;
            } catch (Exception ignored) {
            }
        }
        this.check("K Tomcat accepting HTTP again", recovered);
        this.server.postFormJson("/login", Map.of("userName", "Alice"), ClientJsonResponse.class);
        this.server.postFormJson("/login", Map.of("userName", "Bob"), ClientJsonResponse.class);
        Thread.sleep(2500);
        this.check(
                "K chat empty after restart",
                this.fxGet(() -> this.alice.chatListView.getItems().isEmpty()
                        && this.bob.chatListView.getItems().isEmpty())
        );
        this.check(
                "K typed unsent text still present",
                this.fxGet(() -> "unsent draft".equals(this.alice.chatMessageField.getText()))
        );

        this.onFx(() -> {
            ClientSession.setCurrentUserName("Alice");
            this.alice.chatMessageField.setText("After restart");
            this.alice.handleChatSend();
        });
        boolean afterRestart = false;
        for (int i = 0; i < 8; i++) {
            Thread.sleep(400);
            if (this.fxGet(() -> this.aliceChatContains("After restart")
                    && this.bobChatContains("After restart"))) {
                afterRestart = true;
                break;
            }
        }
        this.check("L send works after restart", afterRestart);

        this.check(
                "M still exactly one poll Timeline on Alice",
                this.fxGet(() -> this.alice.pollTimeline != null
                        && this.alice.pollTimeline.getKeyFrames().size() == 1)
        );

        this.finish(aliceStage, bobStage);
    }


    private boolean aliceChatContains(String text) {
        return this.alice.chatListView.getItems().toString().contains(text);
    }


    private boolean bobChatContains(String text) {
        return this.bob.chatListView.getItems().toString().contains(text);
    }


    private boolean orderedTwoMessages(MainController controller) {
        if (controller.chatListView.getItems().size() < 2) {
            return false;
        }
        String first = controller.chatListView.getItems().get(0).toString();
        String second = controller.chatListView.getItems().get(1).toString();
        return first.contains("Alice:")
                && first.contains("Hello Bob")
                && second.contains("Bob:")
                && second.contains("Hi Alice");
    }


    private void finish(Stage aliceStage, Stage bobStage) {
        Platform.runLater(() -> {
            if (this.failures == 0) {
                System.out.println("BONUS_B2_UI_OK");
            } else {
                System.out.println("BONUS_B2_UI_FAILED " + this.failures);
            }
            this.alice.stopPolling();
            this.bob.stopPolling();
            aliceStage.close();
            if (bobStage != null) {
                bobStage.close();
            }
            Platform.exit();
        });
    }


    private MainController loadMain(Stage stage) throws Exception {
        FXMLLoader loader =
                new FXMLLoader(GuessMarketApplication.class.getResource("/main-view.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1000, 700));
        stage.setTitle("Guess Market");
        stage.show();
        return loader.getController();
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
