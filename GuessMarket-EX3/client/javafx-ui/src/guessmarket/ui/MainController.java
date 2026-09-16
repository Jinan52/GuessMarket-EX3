package guessmarket.ui;

import com.google.gson.reflect.TypeToken;
import guessmarket.client.ClientSession;
import guessmarket.client.http.ChatMessageResponse;
import guessmarket.client.http.ClientJsonResponse;
import guessmarket.client.http.CloseEventResponse;
import guessmarket.client.http.EventAccountResponse;
import guessmarket.client.http.GuessMarketHttpClient;
import guessmarket.client.http.GuessMarketHttpException;
import guessmarket.client.http.LmsrPurchaseResponse;
import guessmarket.client.http.UserEventCommissionResponse;
import guessmarket.client.http.UserObAmountPaidResponse;
import guessmarket.client.http.UserObProfitLossResponse;
import guessmarket.client.http.UserSharesResponse;
import guessmarket.engine.dto.AccountHistoryRow;
import guessmarket.engine.dto.EventDetails;
import guessmarket.engine.dto.EventSummary;
import guessmarket.engine.dto.OptionState;
import guessmarket.engine.dto.OrderBookParticipantInfo;
import guessmarket.engine.dto.OrderBookStatistics;
import guessmarket.engine.dto.PendingOrderInfo;
import guessmarket.engine.dto.TradeView;
import guessmarket.engine.dto.UserSummary;
import guessmarket.engine.model.CommissionType;
import guessmarket.engine.model.EventStatus;
import guessmarket.engine.model.MarketMethodType;
import guessmarket.engine.model.UserLmsrTrade;
import java.io.File;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;

public class MainController {
   @FXML
   private Label titleLabel;
   @FXML
   private Button loadFileButton;
   @FXML
   private CheckBox enableStartupAnimationsCheckBox;
   @FXML
   private Label filePathLabel;
   @FXML
   Label statusLabel;
   @FXML
   private ProgressBar progressBar;
   @FXML
   ListView usersListView;
   @FXML
   ListView chatListView;
   @FXML
   TextField chatMessageField;
   @FXML
   private Button chatSendButton;
   @FXML
   Label chatStatusLabel;
   @FXML
   Label selectedUserNameLabel;
   @FXML
   Label selectedUserCashLabel;
   @FXML
   Label selectedUserHoldingsLabel;
   @FXML
   TextField addFundsAmountField;
   @FXML
   private Button addFundsButton;
   @FXML
   ListView accountHistoryListView;
   @FXML
   ListView userActiveEventsListView;
   @FXML
   Label userEventDetailsTitleLabel;
   @FXML
   Label userEventCommissionLabel;
   @FXML
   private VBox userLmsrDetailsBox;
   @FXML
   ListView userLmsrTradesListView;
   @FXML
   private Label userLmsrClosedInfoLabel;
   @FXML
   private VBox userObDetailsBox;
   @FXML
   ListView userObHoldingsListView;
   @FXML
   Label userObProfitLossLabel;
   private final List userActiveEvents = new ArrayList();
   @FXML
   ListView userClosedEventsListView;
   private final List userClosedEvents = new ArrayList();
   @FXML
   ListView eventsListView;
   private final List displayedEvents = new ArrayList();
   @FXML
   private ToggleButton filterMethodAllButton;
   @FXML
   private ToggleButton filterMethodLmsrButton;
   @FXML
   private ToggleButton filterMethodOrderBookButton;
   @FXML
   private ToggleButton filterStatusAllButton;
   @FXML
   private ToggleButton filterStatusNotStartedButton;
   @FXML
   private ToggleButton filterStatusActiveButton;
   @FXML
   private ToggleButton filterStatusClosedButton;
   @FXML
   private ToggleButton filterCommissionAllButton;
   @FXML
   private ToggleButton filterCommissionOnCloseButton;
   @FXML
   private ToggleButton filterCommissionOnPurchaseButton;
   @FXML
   private Label eventIdLabel;
   @FXML
   Label eventNameLabel;
   @FXML
   Label eventStatusLabel;
   @FXML
   private Label eventDescriptionLabel;
   @FXML
   private Label eventCommissionLabel;
   @FXML
   Label eventAccountBalanceLabel;
   @FXML
   private Label eventOptionsLabel;
   @FXML
   private Label eventMethodLabel;
   @FXML
   private Label marketParam1Label;
   @FXML
   private Label marketParam2Label;
   @FXML
   private Label marketParam3Label;
   @FXML
   VBox lmsrDetailsBox;
   @FXML
   Label lmsrOption1StateLabel;
   @FXML
   private Label lmsrOption2StateLabel;
   @FXML
   private Label lmsrCollectedCommissionLabel;
   @FXML
   private ListView lmsrTradeHistoryListView;
   @FXML
   Label lmsrClosedSummaryLabel;
   @FXML
   ComboBox lmsrPurchaseOptionComboBox;
   @FXML
   TextField lmsrPurchaseQuantityField;
   @FXML
   private Button lmsrPurchaseButton;
   @FXML
   ComboBox lmsrWinnerComboBox;
   @FXML
   private Button lmsrCloseButton;
   @FXML
   private Button openEventButton;
   @FXML
   ComboBox winnerComboBox;
   @FXML
   private Button closeEventButton;
   @FXML
   ComboBox orderOptionComboBox;
   @FXML
   ComboBox orderTypeComboBox;
   @FXML
   TextField orderQuantityField;
   @FXML
   TextField orderPriceField;
   @FXML
   private Button submitOrderButton;
   @FXML
   ListView pendingOrdersListView;
   @FXML
   ListView participantsListView;
   @FXML
   Label lastPriceLabel;
   @FXML
   private Label bidPriceLabel;
   @FXML
   private Label askPriceLabel;
   @FXML
   private Label midPriceLabel;
   @FXML
   private Label spreadLabel;
   @FXML
   Label selectedUserBlockedLabel;
   GuessMarketHttpClient httpClient = new GuessMarketHttpClient();
   static final double POLL_INTERVAL_SECONDS = 1.0;
   Timeline pollTimeline;
   final AtomicBoolean pollInProgress = new AtomicBoolean(false);
   private boolean pollConnectionError;
   private boolean applyingPoll;
   private String loggedInUserName;
   private long lastChatSendNanos;
   private static final Type EVENT_LIST_TYPE =
           new TypeToken<List<EventSummary>>() {
           }.getType();
   private static final Type PENDING_ORDERS_TYPE =
           new TypeToken<List<PendingOrderInfo>>() {
           }.getType();
   private static final Type PARTICIPANTS_TYPE =
           new TypeToken<List<OrderBookParticipantInfo>>() {
           }.getType();
   private static final Type USER_LIST_TYPE =
           new TypeToken<List<UserSummary>>() {
           }.getType();
   private static final Type CHAT_LIST_TYPE =
           new TypeToken<List<ChatMessageResponse>>() {
           }.getType();
   private static final DateTimeFormatter CHAT_TIME_FORMAT =
           DateTimeFormatter.ofPattern("HH:mm:ss")
                   .withZone(ZoneId.systemDefault());
   private static final Type ACCOUNT_HISTORY_TYPE =
           new TypeToken<List<AccountHistoryRow>>() {
           }.getType();
   private static final Type USER_LMSR_TRADES_TYPE =
           new TypeToken<List<UserLmsrTrade>>() {
           }.getType();
   private FadeTransition titleFadeTransition;
   private FadeTransition loadFileFadeTransition;
   private FadeTransition progressBarFadeTransition;

   @FXML
   private void initialize() {
      this.orderTypeComboBox.getItems().add("BUY");
      this.orderTypeComboBox.getItems().add("SELL");
      this.disableOrderControls();
      this.winnerComboBox.setDisable(true);
      this.closeEventButton.setDisable(true);
      this.clearOrderBookStatistics();
      this.hideLmsrTradingStatus();
      this.hideUserEventTypeBoxes();
      this.userActiveEventsListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> this.handleUserActiveEventSelection());
      this.userClosedEventsListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> this.handleUserClosedEventSelection());
      this.lmsrPurchaseButton.setOnAction((actionEvent) -> this.handleLmsrPurchase());
      this.lmsrCloseButton.setOnAction((actionEvent) -> this.handleLmsrClose());
      this.filterMethodAllButton.setOnAction((actionEvent) -> this.handleEventFilterChanged());
      this.filterMethodLmsrButton.setOnAction((actionEvent) -> this.handleEventFilterChanged());
      this.filterMethodOrderBookButton.setOnAction((actionEvent) -> this.handleEventFilterChanged());
      this.filterStatusAllButton.setOnAction((actionEvent) -> this.handleEventFilterChanged());
      this.filterStatusNotStartedButton.setOnAction((actionEvent) -> this.handleEventFilterChanged());
      this.filterStatusActiveButton.setOnAction((actionEvent) -> this.handleEventFilterChanged());
      this.filterStatusClosedButton.setOnAction((actionEvent) -> this.handleEventFilterChanged());
      this.filterCommissionAllButton.setOnAction((actionEvent) -> this.handleEventFilterChanged());
      this.filterCommissionOnCloseButton.setOnAction((actionEvent) -> this.handleEventFilterChanged());
      this.filterCommissionOnPurchaseButton.setOnAction((actionEvent) -> this.handleEventFilterChanged());
      this.titleLabel.setOpacity((double)1.0F);
      this.loadFileButton.setOpacity((double)1.0F);
      this.progressBar.setOpacity((double)1.0F);
      this.enableStartupAnimationsCheckBox.setOnAction((actionEvent) -> this.handleStartupAnimationsToggle());
      this.loggedInUserName = ClientSession.getCurrentUserName();
      this.loadUsersToList();
      this.refreshLoggedInUserAccount();
      this.loadEventsToList();
      this.refreshChatFromServer();
      this.startPolling();
   }

   private void handleStartupAnimationsToggle() {
      if (this.enableStartupAnimationsCheckBox.isSelected()) {
         this.playStartupAnimations();
      } else {
         this.stopStartupAnimationsAndReset();
      }

   }

   private void playStartupAnimations() {
      if (this.progressBar.progressProperty().isBound()) {
         this.stopStartupAnimationsAndReset();
      } else {
         this.stopStartupAnimationsAndReset();
         this.titleLabel.setOpacity((double)0.0F);
         this.loadFileButton.setOpacity((double)0.0F);
         this.progressBar.setOpacity((double)0.0F);
         this.titleFadeTransition = new FadeTransition(Duration.seconds((double)1.0F), this.titleLabel);
         this.titleFadeTransition.setFromValue((double)0.0F);
         this.titleFadeTransition.setToValue((double)1.0F);
         this.loadFileFadeTransition = new FadeTransition(Duration.seconds((double)1.0F), this.loadFileButton);
         this.loadFileFadeTransition.setFromValue((double)0.0F);
         this.loadFileFadeTransition.setToValue((double)1.0F);
         this.progressBarFadeTransition = new FadeTransition(Duration.seconds((double)1.0F), this.progressBar);
         this.progressBarFadeTransition.setFromValue((double)0.0F);
         this.progressBarFadeTransition.setToValue((double)1.0F);
         this.titleFadeTransition.setOnFinished((event) -> {
            if (this.enableStartupAnimationsCheckBox.isSelected()) {
               this.loadFileFadeTransition.playFromStart();
            }
         });
         this.loadFileFadeTransition.setOnFinished((event) -> {
            if (this.enableStartupAnimationsCheckBox.isSelected()) {
               if (this.progressBar.progressProperty().isBound()) {
                  this.progressBar.setOpacity((double)1.0F);
               } else {
                  this.progressBarFadeTransition.playFromStart();
               }
            }
         });
         this.titleFadeTransition.playFromStart();
      }
   }

   private void stopStartupAnimationsAndReset() {
      if (this.titleFadeTransition != null) {
         this.titleFadeTransition.stop();
      }

      if (this.loadFileFadeTransition != null) {
         this.loadFileFadeTransition.stop();
      }

      if (this.progressBarFadeTransition != null) {
         this.progressBarFadeTransition.stop();
      }

      this.titleLabel.setOpacity((double)1.0F);
      this.loadFileButton.setOpacity((double)1.0F);
      this.progressBar.setOpacity((double)1.0F);
   }

   @FXML
   private void handleOrderOptionSelection() {
      if (this.applyingPoll) {
         return;
      }
      EventSummary selectedEvent = this.getSelectedDisplayedEvent();
      if (selectedEvent != null) {
         this.showPendingOrders(selectedEvent);
         this.showOrderBookStatistics(selectedEvent);
         this.showParticipants(selectedEvent);
      }
   }

   @FXML
   void handleLoadFile() {
      String userName = this.requireLoggedInUserName();
      if (userName == null) {
         return;
      }

      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Choose Guess Market XML File");
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", new String[]{"*.xml"}));
      Window window = null;
      if (this.loadFileButton != null && this.loadFileButton.getScene() != null) {
         window = this.loadFileButton.getScene().getWindow();
      }

      File selectedFile = fileChooser.showOpenDialog(window);
      if (selectedFile != null) {
         this.uploadXmlFile(selectedFile.toPath());
      }
   }

   void uploadXmlFile(Path xmlFile) {
      String userName = this.requireLoggedInUserName();
      if (userName == null) {
         return;
      }

      if (xmlFile == null) {
         this.statusLabel.setText("XML file is required.");
         return;
      }

      try {
         String body = this.httpClient.postMultipartFile(
                 "/upload",
                 Map.of("userName", userName),
                 "file",
                 xmlFile
         );
         ClientJsonResponse response =
                 this.httpClient.gson().fromJson(body, ClientJsonResponse.class);
         this.loadEventsToList();
         if (response != null && response.getMessage() != null && !response.getMessage().isBlank()) {
            this.statusLabel.setText(response.getMessage());
         } else {
            this.statusLabel.setText("XML uploaded successfully.");
         }
      } catch (GuessMarketHttpException exception) {
         this.statusLabel.setText(exception.getMessage());
      }
   }

   void loadUsersToList() {
      this.usersListView.getItems().clear();

      try {
         String currentUserName = ClientSession.getCurrentUserName();
         List<UserSummary> users = this.fetchUsers();
         for (UserSummary user : users) {
            if (currentUserName != null && currentUserName.equals(user.getName())) {
               continue;
            }
            this.usersListView.getItems().add(this.formatOtherUserRow(user));
         }
      } catch (GuessMarketHttpException exception) {
         this.statusLabel.setText(exception.getMessage());
      }
   }

   private String formatOtherUserRow(UserSummary user) {
      boolean marketMaker =
              user.getMarketMakerEventIds() != null
                      && !user.getMarketMakerEventIds().isEmpty();
      return user.getName()
              + " | "
              + String.format("%.2f", user.getBalance())
              + " | MM: "
              + (marketMaker ? "YES" : "NO");
   }

   void loadEventsToList() {
      Integer previouslySelectedEventId = null;
      EventSummary previouslySelectedEvent = this.getSelectedDisplayedEvent();
      if (previouslySelectedEvent != null) {
         previouslySelectedEventId = previouslySelectedEvent.id();
      }

      this.eventsListView.getItems().clear();
      this.displayedEvents.clear();
      int indexToSelect = -1;

      try {
         List<EventSummary> events = this.fetchEvents();

         for(EventSummary event : events) {
            if (this.eventMatchesCurrentFilters(event)) {
               this.displayedEvents.add(event);
               this.eventsListView.getItems().add(this.formatEventOverviewRow(event));
               if (previouslySelectedEventId != null && event.id() == previouslySelectedEventId) {
                  indexToSelect = this.displayedEvents.size() - 1;
               }
            }
         }

         if (indexToSelect >= 0) {
            this.eventsListView.getSelectionModel().select(indexToSelect);
         } else {
            this.eventsListView.getSelectionModel().clearSelection();
            this.clearEventDetails();
         }

         if (this.statusLabel.getText() == null
                 || this.statusLabel.getText().startsWith("Loading events")
                 || this.statusLabel.getText().isEmpty()) {
            this.statusLabel.setText("Events loaded from the server.");
         }
      } catch (GuessMarketHttpException exception) {
         this.clearEventDetails();
         this.statusLabel.setText(exception.getMessage());
      }
   }

   private String formatEventOverviewRow(EventSummary event) {
      String accountText = "unavailable";

      try {
         accountText = String.format("%.2f", this.fetchEventAccount(event.id()));
      } catch (GuessMarketHttpException exception) {
         this.statusLabel.setText(exception.getMessage());
      }

      return event.name() + " | " + String.valueOf(event.status()) + " | " + String.valueOf(event.marketMethodType()) + " | " + event.commissionType().xmlValue() + " " + event.commissionPercent() + "% | Account: " + accountText;
   }

   private EventSummary getSelectedDisplayedEvent() {
      int selectedIndex = this.eventsListView.getSelectionModel().getSelectedIndex();
      if (selectedIndex >= 0 && selectedIndex < this.displayedEvents.size()) {
         return (EventSummary)this.displayedEvents.get(selectedIndex);
      } else {
         return null;
      }
   }

   private void handleEventFilterChanged() {
      if (!this.filterMethodAllButton.isSelected() && !this.filterMethodLmsrButton.isSelected() && !this.filterMethodOrderBookButton.isSelected()) {
         this.filterMethodAllButton.setSelected(true);
      }

      if (!this.filterStatusAllButton.isSelected() && !this.filterStatusNotStartedButton.isSelected() && !this.filterStatusActiveButton.isSelected() && !this.filterStatusClosedButton.isSelected()) {
         this.filterStatusAllButton.setSelected(true);
      }

      if (!this.filterCommissionAllButton.isSelected() && !this.filterCommissionOnCloseButton.isSelected() && !this.filterCommissionOnPurchaseButton.isSelected()) {
         this.filterCommissionAllButton.setSelected(true);
      }

      this.loadEventsToList();
   }

   private boolean eventMatchesCurrentFilters(EventSummary event) {
      if (this.filterMethodLmsrButton.isSelected()) {
         if (event.marketMethodType() != MarketMethodType.LMSR) {
            return false;
         }
      } else if (this.filterMethodOrderBookButton.isSelected() && event.marketMethodType() != MarketMethodType.ORDER_BOOK) {
         return false;
      }

      if (this.filterStatusNotStartedButton.isSelected()) {
         if (event.status() != EventStatus.NOT_STARTED) {
            return false;
         }
      } else if (this.filterStatusActiveButton.isSelected()) {
         if (event.status() != EventStatus.ACTIVE) {
            return false;
         }
      } else if (this.filterStatusClosedButton.isSelected() && event.status() != EventStatus.CLOSED) {
         return false;
      }

      if (this.filterCommissionOnCloseButton.isSelected()) {
         if (event.commissionType() != CommissionType.ON_CLOSE) {
            return false;
         }
      } else if (this.filterCommissionOnPurchaseButton.isSelected() && event.commissionType() != CommissionType.ON_PURCHASE) {
         return false;
      }

      return true;
   }

   @FXML
   void handleUserSelection() {
      /*
       * Other Users is public directory data only.
       * Clicking a row must not change the logged-in
       * identity or load another user's private data.
       */
   }


   @FXML
   void handleChatSend() {
      String userName = ClientSession.getCurrentUserName();
      if (userName == null || userName.trim().isEmpty()) {
         this.chatStatusLabel.setText("Please log in first.");
         return;
      }

      String message =
              this.chatMessageField.getText() == null
                      ? ""
                      : this.chatMessageField.getText().trim();

      if (message.isEmpty()) {
         this.chatStatusLabel.setText("Message cannot be empty.");
         return;
      }

      try {
         ClientJsonResponse response =
                 this.httpClient.postFormJson(
                         "/chat-send",
                         Map.of(
                                 "userName", userName.trim(),
                                 "message", message
                         ),
                         ClientJsonResponse.class
                 );
         this.lastChatSendNanos = System.nanoTime();
         this.chatMessageField.clear();
         if (response != null
                 && response.getMessage() != null
                 && !response.getMessage().isBlank()) {
            this.chatStatusLabel.setText(response.getMessage());
         } else {
            this.chatStatusLabel.setText("Message sent successfully.");
         }
         this.refreshChatFromServer();
      } catch (GuessMarketHttpException exception) {
         this.chatStatusLabel.setText(exception.getMessage());
      }
   }


   void refreshChatFromServer() {
      if (this.chatListView == null) {
         return;
      }

      try {
         this.applyChatMessages(this.fetchChat());
      } catch (GuessMarketHttpException exception) {
         this.chatStatusLabel.setText(exception.getMessage());
      }
   }


   private void applyChatFromSnapshot(PollSnapshot snapshot) {
      int incomingSize =
              snapshot.chatMessages == null ? 0 : snapshot.chatMessages.size();
      int currentSize =
              this.chatListView == null ? 0 : this.chatListView.getItems().size();
      if (snapshot.chatCollectedAtNanos < this.lastChatSendNanos
              && incomingSize < currentSize) {
         return;
      }
      this.applyChatMessages(snapshot.chatMessages);
   }


   private void applyChatMessages(List<ChatMessageResponse> messages) {
      if (this.chatListView == null) {
         return;
      }

      List<String> lines = new ArrayList();
      if (messages != null) {
         for (ChatMessageResponse chatMessage : messages) {
            lines.add(this.formatChatLine(chatMessage));
         }
      }

      if (this.chatListView.getItems().equals(lines)) {
         return;
      }

      this.chatListView.getItems().setAll(lines);
      if (!lines.isEmpty()) {
         this.chatListView.scrollTo(lines.size() - 1);
      }
   }


   private String formatChatLine(ChatMessageResponse chatMessage) {
      String sender =
              chatMessage == null || chatMessage.getUserName() == null
                      ? ""
                      : chatMessage.getUserName();
      String text =
              chatMessage == null || chatMessage.getMessage() == null
                      ? ""
                      : chatMessage.getMessage();
      return "["
              + this.formatChatTimestamp(
              chatMessage == null ? null : chatMessage.getTimestamp()
      )
              + "] "
              + sender
              + ": "
              + text;
   }


   private String formatChatTimestamp(String timestamp) {
      if (timestamp == null || timestamp.isBlank()) {
         return "";
      }

      try {
         return CHAT_TIME_FORMAT.format(Instant.parse(timestamp));
      } catch (RuntimeException exception) {
         return timestamp;
      }
   }

   void showLoggedInUserDetails() {
      String userName = this.requireLoggedInUserName();
      if (userName == null) {
         return;
      }

      try {
         UserSummary currentUser = this.findCurrentUser(userName);
         if (currentUser == null) {
            this.statusLabel.setText("The logged-in user was not found on the server.");
            return;
         }

         this.selectedUserNameLabel.setText("Name: " + currentUser.getName());
         this.selectedUserCashLabel.setText("Balance: " + currentUser.getBalance());
         String blockedText = "Blocked: " + currentUser.isBlocked();
         if (currentUser.getMarketMakerEventIds() != null
                 && !currentUser.getMarketMakerEventIds().isEmpty()) {
            blockedText = blockedText + " | MM events: " + currentUser.getMarketMakerEventIds();
         }
         this.selectedUserBlockedLabel.setText(blockedText);
      } catch (GuessMarketHttpException exception) {
         this.statusLabel.setText(exception.getMessage());
      }
   }

   @FXML
   void handleAddFunds() {
      String userName = this.requireLoggedInUserName();
      if (userName == null) {
         return;
      }

      String amountText =
              this.addFundsAmountField.getText() == null
                      ? ""
                      : this.addFundsAmountField.getText().trim();

      double amount;
      try {
         amount = Double.parseDouble(amountText);
      } catch (NumberFormatException exception) {
         this.statusLabel.setText("Amount must be a number.");
         return;
      }

      if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
         this.statusLabel.setText("Amount must be greater than 0.");
         return;
      }

      try {
         ClientJsonResponse response =
                 this.httpClient.postFormJson(
                         "/funds",
                         Map.of(
                                 "userName", userName,
                                 "amount", String.valueOf(amount)
                         ),
                         ClientJsonResponse.class
                 );
         this.refreshLoggedInUserAccount();
         this.addFundsAmountField.clear();
         if (response != null && response.getMessage() != null && !response.getMessage().isBlank()) {
            this.statusLabel.setText(response.getMessage());
         } else {
            this.statusLabel.setText("Funds added successfully.");
         }
      } catch (GuessMarketHttpException exception) {
         this.statusLabel.setText(exception.getMessage());
      }
   }

   @FXML
   void handleEventSelection() {
      if (this.applyingPoll) {
         return;
      }
      EventSummary event = this.getSelectedDisplayedEvent();
      if (event != null) {
         this.showEventDetails(event);
         this.showSelectedUserHoldings();
      }
   }

   @FXML
   void handleOpenEvent() {
      String userName = this.requireLoggedInUserName();
      EventSummary selectedEvent = this.getSelectedDisplayedEvent();
      if (userName == null) {
         return;
      } else if (selectedEvent == null) {
         this.statusLabel.setText("Please select an event.");
      } else {
         try {
            ClientJsonResponse response =
                    this.httpClient.postFormJson(
                            "/open-event",
                            Map.of(
                                    "userName", userName,
                                    "eventId", String.valueOf(selectedEvent.id())
                            ),
                            ClientJsonResponse.class
                    );
            this.refreshSelectedEventFromServer();
            if (response != null && response.getMessage() != null && !response.getMessage().isBlank()) {
               this.statusLabel.setText(response.getMessage());
            } else {
               this.statusLabel.setText("Event opened successfully.");
            }
         } catch (GuessMarketHttpException exception) {
            this.statusLabel.setText(exception.getMessage());
         }
      }
   }

   @FXML
   void handleLmsrPurchase() {
      String userName = this.requireLoggedInUserName();
      EventSummary event = this.getSelectedDisplayedEvent();
      if (userName == null) {
         return;
      } else if (event == null) {
         this.statusLabel.setText("Please select an event.");
      } else {
         if (event.marketMethodType() != MarketMethodType.LMSR) {
            this.statusLabel.setText("This purchase is available only for LMSR events.");
         } else {
            String selectedOption = (String)this.lmsrPurchaseOptionComboBox.getSelectionModel().getSelectedItem();
            if (selectedOption == null) {
               this.statusLabel.setText("Please select an option.");
            } else {
               int optionIndex = this.findOptionIndex(event, selectedOption);
               if (optionIndex == -1) {
                  this.statusLabel.setText("Invalid option selection.");
               } else {
                  int quantity;
                  try {
                     quantity = Integer.parseInt(this.lmsrPurchaseQuantityField.getText().trim());
                  } catch (NumberFormatException var11) {
                     this.statusLabel.setText("Quantity must be a whole number.");
                     return;
                  }

                  if (quantity <= 0) {
                     this.statusLabel.setText("Quantity must be a positive whole number.");
                  } else {
                     try {
                        Map<String, String> fields = new HashMap();
                        fields.put("userName", userName);
                        fields.put("eventId", String.valueOf(event.id()));
                        fields.put("optionIndex", String.valueOf(optionIndex));
                        fields.put("quantity", String.valueOf(quantity));
                        LmsrPurchaseResponse response =
                                this.httpClient.postFormJson(
                                        "/lmsr-purchase",
                                        fields,
                                        LmsrPurchaseResponse.class
                                );
                        this.refreshSelectedEventFromServer();
                        this.lmsrPurchaseQuantityField.clear();
                        if (response != null && response.getMessage() != null && !response.getMessage().isBlank()) {
                           this.statusLabel.setText(response.getMessage());
                        } else {
                           this.statusLabel.setText("Purchase completed successfully.");
                        }
                     } catch (GuessMarketHttpException exception) {
                        this.statusLabel.setText(exception.getMessage());
                     }
                  }
               }
            }
         }
      }
   }

   @FXML
   void handleLmsrClose() {
      EventSummary event = this.getSelectedDisplayedEvent();
      if (this.requireLoggedInUserName() == null) {
         return;
      } else if (event == null) {
         this.statusLabel.setText("Please select an event.");
      } else {
         String winningOption = (String)this.lmsrWinnerComboBox.getSelectionModel().getSelectedItem();
         if (winningOption == null) {
            this.statusLabel.setText("Please select the winning option.");
         } else {
            if (event.marketMethodType() != MarketMethodType.LMSR) {
               this.statusLabel.setText("This close is available only for LMSR events.");
            } else {
               int winningOptionIndex = this.findOptionIndex(event, winningOption);
               if (winningOptionIndex == -1) {
                  this.statusLabel.setText("The winning option could not be found.");
               } else {
                  this.closeEventOnServer(event.id(), winningOptionIndex);
               }
            }
         }
      }
   }

   @FXML
   void handleCloseEvent() {
      EventSummary event = this.getSelectedDisplayedEvent();
      String winningOption = (String)this.winnerComboBox.getSelectionModel().getSelectedItem();
      if (this.requireLoggedInUserName() == null) {
         return;
      } else if (event == null) {
         this.statusLabel.setText("Please select an event.");
      } else if (winningOption == null) {
         this.statusLabel.setText("Please select the winning option.");
      } else {
         if (event.marketMethodType() != MarketMethodType.ORDER_BOOK) {
            this.statusLabel.setText("Closing from this screen is currently available only for Order Book events.");
         } else {
            int winningOptionIndex = this.findOptionIndex(event, winningOption);
            if (winningOptionIndex == -1) {
               this.statusLabel.setText("The winning option could not be found.");
            } else {
               this.closeEventOnServer(event.id(), winningOptionIndex);
            }
         }
      }
   }

   @FXML
   void handleSubmitOrder() {
      String userName = this.requireLoggedInUserName();
      EventSummary event = this.getSelectedDisplayedEvent();
      if (userName == null) {
         return;
      } else if (event == null) {
         this.statusLabel.setText("Please select an event.");
      } else {
            String selectedOption = (String)this.orderOptionComboBox.getSelectionModel().getSelectedItem();
            if (selectedOption == null) {
               this.statusLabel.setText("Please select an option.");
            } else {
               String selectedType = (String)this.orderTypeComboBox.getSelectionModel().getSelectedItem();
               if (selectedType == null) {
                  this.statusLabel.setText("Please select BUY or SELL.");
               } else {
                  int optionIndex = this.findOptionIndex(event, selectedOption);
                  if (optionIndex == -1) {
                     this.statusLabel.setText("Invalid option selection.");
                  } else {
                     int quantity;
                     try {
                        quantity = Integer.parseInt(this.orderQuantityField.getText().trim());
                     } catch (NumberFormatException var16) {
                        this.statusLabel.setText("Quantity must be a whole number.");
                        return;
                     }

                     double price;
                     try {
                        price = Double.parseDouble(this.orderPriceField.getText().trim());
                     } catch (NumberFormatException var15) {
                        this.statusLabel.setText("Price must be a number.");
                        return;
                     }

                     if (!selectedType.equals("BUY") && !selectedType.equals("SELL")) {
                        this.statusLabel.setText("Please select BUY or SELL.");
                        return;
                     }

                     try {
                        Map<String, String> fields = new HashMap();
                        fields.put("userName", userName);
                        fields.put("eventId", String.valueOf(event.id()));
                        fields.put("optionIndex", String.valueOf(optionIndex));
                        fields.put("type", selectedType);
                        fields.put("quantity", String.valueOf(quantity));
                        fields.put("price", String.valueOf(price));
                        ClientJsonResponse response =
                                this.httpClient.postFormJson(
                                        "/submit-order",
                                        fields,
                                        ClientJsonResponse.class
                                );
                        this.refreshSelectedEventFromServer();
                        this.orderQuantityField.clear();
                        this.orderPriceField.clear();
                        if (response != null && response.getMessage() != null && !response.getMessage().isBlank()) {
                           this.statusLabel.setText(response.getMessage());
                        } else {
                           this.statusLabel.setText("Order submitted successfully.");
                        }
                     } catch (GuessMarketHttpException exception) {
                        this.statusLabel.setText(exception.getMessage());
                     }

                  }
               }
            }
      }
   }

   private void showPendingOrders(EventSummary event) {
      this.pendingOrdersListView.getItems().clear();
      if (event != null && event.marketMethodType() == MarketMethodType.ORDER_BOOK) {
         String selectedOption = (String)this.orderOptionComboBox.getSelectionModel().getSelectedItem();
         if (selectedOption != null) {
            int optionIndex = this.findOptionIndex(event, selectedOption);
            if (optionIndex != -1) {
               try {
                  List<PendingOrderInfo> orders = this.fetchPendingOrders(event.id(), optionIndex);

                  for(PendingOrderInfo order : orders) {
                     String var10000 = order.getUserName();
                     String text = var10000 + " | " + String.valueOf(order.getType()) + " | " + order.getQuantity() + " shares | " + order.getPrice();
                     this.pendingOrdersListView.getItems().add(text);
                  }

                  if (orders.isEmpty()) {
                     this.pendingOrdersListView.getItems().add("No pending orders");
                  }
               } catch (GuessMarketHttpException exception) {
                  this.statusLabel.setText(exception.getMessage());
               }
            }
         }
      }
   }

   private void showParticipants(EventSummary event) {
      this.participantsListView.getItems().clear();
      if (event != null && event.marketMethodType() == MarketMethodType.ORDER_BOOK) {
         try {
            List<OrderBookParticipantInfo> participants = this.fetchOrderBookParticipants(event.id());

            for(OrderBookParticipantInfo participant : participants) {
               String text = participant.getUserName();

               for(int optionIndex = 0; optionIndex < event.options().size(); ++optionIndex) {
                  int shares = (Integer)participant.getSharesPerOption().get(optionIndex);
                  Double value = (Double)participant.getValuePerOption().get(optionIndex);
                  text = text + " | " + (String)event.options().get(optionIndex) + ": " + shares + " shares";
                  if (value == null) {
                     text = text + " | value: N/A";
                  } else {
                     text = text + " | value: " + String.format("%.2f", value);
                  }
               }

               this.participantsListView.getItems().add(text);
            }

            if (participants.isEmpty()) {
               this.participantsListView.getItems().add("No participants");
            }
         } catch (GuessMarketHttpException exception) {
            this.statusLabel.setText(exception.getMessage());
         }
      }
   }

   private void showOrderBookStatistics(EventSummary event) {
      if (event != null && event.marketMethodType() == MarketMethodType.ORDER_BOOK) {
         String selectedOption = (String)this.orderOptionComboBox.getSelectionModel().getSelectedItem();
         if (selectedOption == null) {
            this.clearOrderBookStatistics();
         } else {
            int optionIndex = this.findOptionIndex(event, selectedOption);
            if (optionIndex == -1) {
               this.clearOrderBookStatistics();
            } else {
               try {
                  OrderBookStatistics statistics = this.fetchOrderBookStatistics(event.id(), optionIndex);
                  this.lastPriceLabel.setText(this.formatStatistic(statistics.getLast()));
                  this.bidPriceLabel.setText(this.formatStatistic(statistics.getBid()));
                  this.askPriceLabel.setText(this.formatStatistic(statistics.getAsk()));
                  this.midPriceLabel.setText(this.formatStatistic(statistics.getMid()));
                  this.spreadLabel.setText(this.formatStatistic(statistics.getSpread()));
               } catch (GuessMarketHttpException exception) {
                  this.clearOrderBookStatistics();
                  this.statusLabel.setText(exception.getMessage());
               }
            }
         }
      } else {
         this.clearOrderBookStatistics();
      }
   }

   private String formatStatistic(Double value) {
      return value == null ? "N/A" : String.format("%.2f", value);
   }

   private void clearOrderBookStatistics() {
      this.lastPriceLabel.setText("N/A");
      this.bidPriceLabel.setText("N/A");
      this.askPriceLabel.setText("N/A");
      this.midPriceLabel.setText("N/A");
      this.spreadLabel.setText("N/A");
   }

   private int findOptionIndex(EventSummary event, String optionName) {
      for(int i = 0; i < event.options().size(); ++i) {
         if (((String)event.options().get(i)).equals(optionName)) {
            return i;
         }
      }

      return -1;
   }

   void showSelectedUserHoldings() {
      String loggedInUserName = ClientSession.getCurrentUserName();
      EventSummary event = this.getSelectedDisplayedEvent();
      if (loggedInUserName != null && event != null) {
         try {
            UserSharesResponse sharesResponse = this.fetchUserShares(loggedInUserName, event.id());
            String holdingsText = "Holdings in " + event.name() + ":\n";
            List<String> optionNames = sharesResponse.getOptionNames();
            List<Integer> shares = sharesResponse.getShares();
            if (optionNames == null) {
               optionNames = event.options();
            }

            for(int optionIndex = 0; optionIndex < optionNames.size(); ++optionIndex) {
               int shareCount = 0;
               if (shares != null && optionIndex < shares.size() && shares.get(optionIndex) != null) {
                  shareCount = shares.get(optionIndex);
               }

               holdingsText = holdingsText + optionNames.get(optionIndex) + ": " + shareCount + " shares";
               if (optionIndex < optionNames.size() - 1) {
                  holdingsText = holdingsText + "\n";
               }
            }

            this.selectedUserHoldingsLabel.setText(holdingsText);
         } catch (GuessMarketHttpException exception) {
            this.selectedUserHoldingsLabel.setText("");
            this.statusLabel.setText(exception.getMessage());
         }
      } else {
         this.selectedUserHoldingsLabel.setText("");
      }
   }

   private void showEventDetails(EventSummary event) {
      this.eventIdLabel.setText("ID: " + event.id());
      this.eventNameLabel.setText("Name: " + event.name());
      this.eventStatusLabel.setText("Status: " + String.valueOf(event.status()));
      if (event.status() == EventStatus.NOT_STARTED) {
         this.openEventButton.setDisable(false);
      } else {
         this.openEventButton.setDisable(true);
      }

      this.eventDescriptionLabel.setText("Description: " + event.description());
      Label var10000 = this.eventCommissionLabel;
      int var10001 = event.commissionPercent();
      var10000.setText("Commission: " + var10001 + " (" + String.valueOf(event.commissionType()) + ")");
      try {
         double accountBalance = this.fetchEventAccount(event.id());
         var10000 = this.eventAccountBalanceLabel;
         Object[] var10002 = new Object[]{accountBalance};
         var10000.setText("Event account balance: " + String.format("%.2f", var10002));
      } catch (GuessMarketHttpException exception) {
         this.eventAccountBalanceLabel.setText("Event account balance: unavailable");
         this.statusLabel.setText(exception.getMessage());
      }
      String options = "";

      for(String option : event.options()) {
         if (!options.isEmpty()) {
            options = options + " | ";
         }

         options = options + option;
      }

      this.eventOptionsLabel.setText("Options: " + options);
      if (event.marketMethodType() == MarketMethodType.LMSR) {
         this.eventMethodLabel.setText("Market method: LMSR");
         this.marketParam1Label.setText("b: " + event.liquidityParameter());
         this.marketParam2Label.setText("");
         this.marketParam3Label.setText("");
         this.orderOptionComboBox.getItems().clear();
         this.winnerComboBox.getItems().clear();
         this.pendingOrdersListView.getItems().clear();
         this.participantsListView.getItems().clear();
         this.disableOrderControls();
         this.winnerComboBox.setDisable(true);
         this.closeEventButton.setDisable(true);
         this.clearOrderBookStatistics();
         this.showLmsrTradingStatus(event);
      } else {
         this.hideLmsrTradingStatus();
         this.eventMethodLabel.setText("Market method: Order Book");
         this.marketParam1Label.setText("Initial: " + event.orderBookInitial());
         this.marketParam2Label.setText("d: " + event.orderBookD());
         this.marketParam3Label.setText("Allow mint: " + event.allowMint());
         this.orderOptionComboBox.getItems().clear();
         this.winnerComboBox.getItems().clear();

         for(String option : event.options()) {
            this.orderOptionComboBox.getItems().add(option);
            this.winnerComboBox.getItems().add(option);
         }

         if (!event.options().isEmpty()) {
            this.orderOptionComboBox.getSelectionModel().select(0);
            this.winnerComboBox.getSelectionModel().select(0);
         }

         if (event.status() == EventStatus.ACTIVE) {
            this.enableOrderControls();
            this.winnerComboBox.setDisable(false);
            this.closeEventButton.setDisable(false);
         } else {
            this.disableOrderControls();
            this.winnerComboBox.setDisable(true);
            this.closeEventButton.setDisable(true);
         }

         this.showPendingOrders(event);
         this.showOrderBookStatistics(event);
         this.showParticipants(event);
      }
   }

   private void enableOrderControls() {
      this.orderOptionComboBox.setDisable(false);
      this.orderTypeComboBox.setDisable(false);
      this.orderQuantityField.setDisable(false);
      this.orderPriceField.setDisable(false);
      this.submitOrderButton.setDisable(false);
   }

   private void disableOrderControls() {
      this.orderOptionComboBox.setDisable(true);
      this.orderTypeComboBox.setDisable(true);
      this.orderQuantityField.setDisable(true);
      this.orderPriceField.setDisable(true);
      this.submitOrderButton.setDisable(true);
   }

   private void hideUserEventTypeBoxes() {
      this.userLmsrTradesListView.getItems().clear();
      this.userLmsrClosedInfoLabel.setText("");
      this.userLmsrDetailsBox.setVisible(false);
      this.userLmsrDetailsBox.setManaged(false);
      this.userObHoldingsListView.getItems().clear();
      this.userObProfitLossLabel.setText("");
      this.userObDetailsBox.setVisible(false);
      this.userObDetailsBox.setManaged(false);
   }

   private void clearUserActiveEventSection() {
      this.userActiveEvents.clear();
      this.userActiveEventsListView.getItems().clear();
      this.userActiveEventsListView.getSelectionModel().clearSelection();
      this.userClosedEvents.clear();
      this.userClosedEventsListView.getItems().clear();
      this.userClosedEventsListView.getSelectionModel().clearSelection();
      this.userEventDetailsTitleLabel.setText("");
      this.userEventCommissionLabel.setText("");
      this.hideUserEventTypeBoxes();
   }

   private void loadUserActiveEvents() {
      String userName = ClientSession.getCurrentUserName();
      this.clearUserActiveEventSection();
      if (userName == null || userName.trim().isEmpty()) {
         return;
      }

      try {
         List<EventSummary> events = this.fetchUserActiveEvents(userName.trim());
         for (EventSummary event : events) {
            this.userActiveEvents.add(event);
            this.userActiveEventsListView.getItems().add(event.id() + " - " + event.name());
         }
         this.loadUserClosedEvents(userName.trim());
      } catch (GuessMarketHttpException exception) {
         this.statusLabel.setText(exception.getMessage());
      }
   }

   private void loadUserClosedEvents(String userName) {
      this.userClosedEvents.clear();
      this.userClosedEventsListView.getItems().clear();

      try {
         List<EventSummary> events = this.fetchUserClosedEvents(userName);
         for (EventSummary event : events) {
            this.userClosedEvents.add(event);
            this.userClosedEventsListView.getItems().add(event.id() + " - " + event.name());
         }
      } catch (GuessMarketHttpException exception) {
         this.statusLabel.setText(exception.getMessage());
      }
   }

   void handleUserActiveEventSelection() {
      if (this.applyingPoll) {
         return;
      }
      int selectedIndex = this.userActiveEventsListView.getSelectionModel().getSelectedIndex();
      if (selectedIndex >= 0) {
         if (selectedIndex < this.userActiveEvents.size()) {
            this.userClosedEventsListView.getSelectionModel().clearSelection();
            this.showUserParticipatedEventDetails((EventSummary)this.userActiveEvents.get(selectedIndex));
         }
      }
   }

   void handleUserClosedEventSelection() {
      if (this.applyingPoll) {
         return;
      }
      int selectedIndex = this.userClosedEventsListView.getSelectionModel().getSelectedIndex();
      if (selectedIndex >= 0) {
         if (selectedIndex < this.userClosedEvents.size()) {
            this.userActiveEventsListView.getSelectionModel().clearSelection();
            this.showUserParticipatedEventDetails((EventSummary)this.userClosedEvents.get(selectedIndex));
         }
      }
   }

   private void showUserParticipatedEventDetails(EventSummary event) {
      String userName = this.requireLoggedInUserName();
      if (userName == null) {
         this.userEventDetailsTitleLabel.setText("");
         this.userEventCommissionLabel.setText("");
         this.hideUserEventTypeBoxes();
      } else {
         this.userEventDetailsTitleLabel.setText(event.name());
         try {
            UserEventCommissionResponse commissionResponse =
                    this.fetchUserEventCommission(userName, event.id());
            this.userEventCommissionLabel.setText(
                    "Total commission paid for this event: "
                            + String.format("%.2f", commissionResponse.getCommission())
            );
            if (event.marketMethodType() == MarketMethodType.LMSR) {
               this.showUserLmsrDetails(userName, event);
            } else {
               this.showUserObDetails(userName, event);
            }
         } catch (GuessMarketHttpException exception) {
            this.statusLabel.setText(exception.getMessage());
         }
      }
   }

   private void showUserLmsrDetails(String userName, EventSummary event) {
      this.userObDetailsBox.setVisible(false);
      this.userObDetailsBox.setManaged(false);
      this.userLmsrDetailsBox.setVisible(true);
      this.userLmsrDetailsBox.setManaged(true);
      this.userLmsrTradesListView.getItems().clear();
      try {
         List<UserLmsrTrade> trades = this.fetchUserLmsrTrades(userName, event.id());
         for (int i = 0; i < trades.size(); ++i) {
            UserLmsrTrade trade = (UserLmsrTrade)trades.get(i);
            this.userLmsrTradesListView.getItems().add(
                    "Option: " + trade.getOptionName()
                            + " | Shares: " + trade.getQuantity()
                            + " | Payment: " + String.format("%.2f", trade.getSharesPrice())
                            + " | Commission: " + String.format("%.2f", trade.getCommission())
            );
         }

         if (event.status() == EventStatus.CLOSED) {
            EventDetails details = this.fetchEventDetails(event.id());
            UserSharesResponse sharesResponse = this.fetchUserShares(userName, event.id());
            String closedText = "Winning option: "
                    + (details == null ? "" : details.winningOption());
            List<String> optionNames = sharesResponse.getOptionNames();
            List<Integer> shares = sharesResponse.getShares();
            if (optionNames == null) {
               optionNames = event.options();
            }
            for (int optionIndex = 0; optionIndex < optionNames.size(); ++optionIndex) {
               int shareCount = 0;
               if (shares != null && optionIndex < shares.size() && shares.get(optionIndex) != null) {
                  shareCount = shares.get(optionIndex);
               }
               closedText = closedText + "\n" + optionNames.get(optionIndex) + ": " + shareCount + " shares";
            }
            this.userLmsrClosedInfoLabel.setText(closedText);
         } else {
            this.userLmsrClosedInfoLabel.setText("");
         }
      } catch (GuessMarketHttpException exception) {
         this.statusLabel.setText(exception.getMessage());
      }
   }

   private void showUserObDetails(String userName, EventSummary event) {
      this.userLmsrDetailsBox.setVisible(false);
      this.userLmsrDetailsBox.setManaged(false);
      this.userObDetailsBox.setVisible(true);
      this.userObDetailsBox.setManaged(true);
      this.userObHoldingsListView.getItems().clear();
      try {
         UserObAmountPaidResponse amountPaidResponse =
                 this.fetchUserObAmountPaid(userName, event.id());
         UserSharesResponse sharesResponse = this.fetchUserShares(userName, event.id());
         List<Double> amountPaid = amountPaidResponse.getAmountPaid();
         List<String> options = event.options();
         if (sharesResponse.getOptionNames() != null && !sharesResponse.getOptionNames().isEmpty()) {
            options = sharesResponse.getOptionNames();
         }

         for (int i = 0; i < options.size(); ++i) {
            int shares = 0;
            if (sharesResponse.getShares() != null
                    && i < sharesResponse.getShares().size()
                    && sharesResponse.getShares().get(i) != null) {
               shares = sharesResponse.getShares().get(i);
            }
            double paid = 0.0;
            if (amountPaid != null && i < amountPaid.size() && amountPaid.get(i) != null) {
               paid = amountPaid.get(i);
            }
            this.userObHoldingsListView.getItems().add(
                    options.get(i) + " | Shares: " + shares + " | Amount paid: " + String.format("%.2f", paid)
            );
         }

         if (event.status() == EventStatus.CLOSED) {
            UserObProfitLossResponse profitLossResponse =
                    this.fetchUserObProfitLoss(userName, event.id());
            this.userObProfitLossLabel.setText(
                    "Profit/Loss: " + String.format("%.2f", profitLossResponse.getProfitLoss())
            );
         } else {
            this.userObProfitLossLabel.setText("");
         }
      } catch (GuessMarketHttpException exception) {
         this.statusLabel.setText(exception.getMessage());
      }
   }

   private void clearEventDetails() {
      this.eventIdLabel.setText("");
      this.eventNameLabel.setText("");
      this.eventStatusLabel.setText("");
      this.eventDescriptionLabel.setText("");
      this.eventCommissionLabel.setText("");
      this.eventAccountBalanceLabel.setText("");
      this.eventOptionsLabel.setText("");
      this.eventMethodLabel.setText("");
      this.marketParam1Label.setText("");
      this.marketParam2Label.setText("");
      this.marketParam3Label.setText("");
      this.selectedUserBlockedLabel.setText("");
      this.openEventButton.setDisable(true);
      this.winnerComboBox.getItems().clear();
      this.winnerComboBox.setDisable(true);
      this.closeEventButton.setDisable(true);
      this.orderOptionComboBox.getItems().clear();
      this.orderQuantityField.clear();
      this.orderPriceField.clear();
      this.pendingOrdersListView.getItems().clear();
      this.participantsListView.getItems().clear();
      this.disableOrderControls();
      this.clearOrderBookStatistics();
      this.hideLmsrTradingStatus();
   }

   private void showLmsrTradingStatus(EventSummary event) {
      this.lmsrDetailsBox.setVisible(true);
      this.lmsrDetailsBox.setManaged(true);
      EventDetails details;
      try {
         details = this.fetchEventDetails(event.id());
      } catch (GuessMarketHttpException exception) {
         this.statusLabel.setText(exception.getMessage());
         return;
      }
      if (details.options().size() > 0) {
         OptionState option1 = (OptionState)details.options().get(0);
         Label var10000 = this.lmsrOption1StateLabel;
         String var10001 = option1.name();
         var10000.setText(var10001 + " | current value: " + String.format("%.2f", option1.currentValue()) + " | purchased shares: " + option1.purchasedShares());
      } else {
         this.lmsrOption1StateLabel.setText("");
      }

      if (details.options().size() > 1) {
         OptionState option2 = (OptionState)details.options().get(1);
         Label var13 = this.lmsrOption2StateLabel;
         String var16 = option2.name();
         var13.setText(var16 + " | current value: " + String.format("%.2f", option2.currentValue()) + " | purchased shares: " + option2.purchasedShares());
      } else {
         this.lmsrOption2StateLabel.setText("");
      }

      Label var14 = this.lmsrCollectedCommissionLabel;
      Object[] var10002 = new Object[]{details.collectedCommission()};
      var14.setText("Total commission collected: " + String.format("%.2f", var10002));
      this.lmsrTradeHistoryListView.getItems().clear();
      if (details.tradeHistory().isEmpty()) {
         this.lmsrTradeHistoryListView.getItems().add("No trades have been made.");
      } else {
         for(TradeView trade : details.tradeHistory()) {
            String var15 = trade.optionName();
            String line = "Option: " + var15 + " | Shares: " + trade.quantity() + " | Price paid: " + String.format("%.2f", trade.pricePaid());
            this.lmsrTradeHistoryListView.getItems().add(line);
         }
      }

      this.lmsrPurchaseOptionComboBox.getItems().clear();
      this.lmsrWinnerComboBox.getItems().clear();

      for(String option : event.options()) {
         this.lmsrPurchaseOptionComboBox.getItems().add(option);
         this.lmsrWinnerComboBox.getItems().add(option);
      }

      if (!event.options().isEmpty()) {
         this.lmsrPurchaseOptionComboBox.getSelectionModel().select(0);
         this.lmsrWinnerComboBox.getSelectionModel().select(0);
      }

      if (event.status() == EventStatus.ACTIVE) {
         this.lmsrPurchaseOptionComboBox.setDisable(false);
         this.lmsrPurchaseQuantityField.setDisable(false);
         this.lmsrPurchaseButton.setDisable(false);
         this.lmsrWinnerComboBox.setDisable(false);
         this.lmsrCloseButton.setDisable(false);
      } else {
         this.lmsrPurchaseOptionComboBox.setDisable(true);
         this.lmsrPurchaseQuantityField.setDisable(true);
         this.lmsrPurchaseQuantityField.clear();
         this.lmsrPurchaseButton.setDisable(true);
         this.lmsrWinnerComboBox.setDisable(true);
         this.lmsrCloseButton.setDisable(true);
      }

      if (details.winningOption() == null) {
         this.lmsrClosedSummaryLabel.setText("");
      } else {
         String closedText = "Winning option: " + details.winningOption() + "\nTotal purchased shares for each option:";

         for(OptionState option : details.options()) {
            closedText = closedText + "\n" + option.name() + ": " + option.purchasedShares();
         }

         this.lmsrClosedSummaryLabel.setText(closedText);
      }
   }

   private void hideLmsrTradingStatus() {
      this.lmsrDetailsBox.setVisible(false);
      this.lmsrDetailsBox.setManaged(false);
      this.lmsrOption1StateLabel.setText("");
      this.lmsrOption2StateLabel.setText("");
      this.lmsrCollectedCommissionLabel.setText("");
      this.lmsrTradeHistoryListView.getItems().clear();
      this.lmsrClosedSummaryLabel.setText("");
      this.lmsrPurchaseOptionComboBox.getItems().clear();
      this.lmsrPurchaseQuantityField.clear();
      this.lmsrPurchaseOptionComboBox.setDisable(true);
      this.lmsrPurchaseQuantityField.setDisable(true);
      this.lmsrPurchaseButton.setDisable(true);
      this.lmsrWinnerComboBox.getItems().clear();
      this.lmsrWinnerComboBox.setDisable(true);
      this.lmsrCloseButton.setDisable(true);
   }


   void startPolling() {
      if (this.pollTimeline != null) {
         return;
      }

      this.pollTimeline =
              new Timeline(
                      new KeyFrame(
                              Duration.seconds(POLL_INTERVAL_SECONDS),
                              event -> this.pollIfIdle()
                      )
              );
      this.pollTimeline.setCycleCount(Timeline.INDEFINITE);
      this.pollTimeline.play();
      this.attachPollingWindowLifecycle();
      Platform.runLater(() -> {
         if (this.statusLabel.getScene() != null) {
            this.bindStopPollingToWindow(this.statusLabel.getScene().getWindow());
         }
      });
   }


   private void attachPollingWindowLifecycle() {
      this.statusLabel.sceneProperty().addListener((observable, oldScene, newScene) -> {
         if (newScene != null) {
            this.bindStopPollingToWindow(newScene.getWindow());
            newScene.windowProperty().addListener(
                    (windowObservable, oldWindow, newWindow) -> this.bindStopPollingToWindow(newWindow)
            );
         }
      });
      if (this.statusLabel.getScene() != null) {
         this.bindStopPollingToWindow(this.statusLabel.getScene().getWindow());
      }
   }


   private void bindStopPollingToWindow(Window window) {
      if (window != null) {
         window.setOnHidden(windowEvent -> this.stopPolling());
      }
   }


   void stopPolling() {
      if (this.pollTimeline != null) {
         this.pollTimeline.stop();
         this.pollTimeline = null;
      }
   }


   void pollIfIdle() {
      if (!this.pollInProgress.compareAndSet(false, true)) {
         return;
      }

      Integer selectedEventId = null;
      EventSummary selectedEvent = this.getSelectedDisplayedEvent();
      if (selectedEvent != null) {
         selectedEventId = selectedEvent.id();
      }

      String selectedOrderOption =
              this.orderOptionComboBox.getSelectionModel().getSelectedItem() == null
                      ? null
                      : this.orderOptionComboBox.getSelectionModel().getSelectedItem().toString();
      Integer selectedActiveId = this.selectedEventIdFrom(this.userActiveEvents, this.userActiveEventsListView);
      Integer selectedClosedId = this.selectedEventIdFrom(this.userClosedEvents, this.userClosedEventsListView);
      String selectedOtherUser = this.selectedOtherUserRow();
      String currentUserName = this.privatePollUserName();
      Integer selectedOptionIndex = null;
      if (selectedEvent != null
              && selectedEvent.marketMethodType() == MarketMethodType.ORDER_BOOK
              && selectedOrderOption != null) {
         int optionIndex = this.findOptionIndex(selectedEvent, selectedOrderOption);
         if (optionIndex != -1) {
            selectedOptionIndex = optionIndex;
         }
      }

      Integer finalSelectedEventId = selectedEventId;
      Integer finalSelectedOptionIndex = selectedOptionIndex;
      Integer finalSelectedActiveId = selectedActiveId;
      Integer finalSelectedClosedId = selectedClosedId;
      String finalOrderOption = selectedOrderOption;
      String finalOtherUser = selectedOtherUser;
      String finalUserName = currentUserName;

      Thread worker = new Thread(() -> {
         try {
            PollSnapshot snapshot =
                    this.collectPollSnapshot(
                            finalUserName,
                            finalSelectedEventId,
                            finalSelectedOptionIndex,
                            finalSelectedActiveId,
                            finalSelectedClosedId
                    );
            Platform.runLater(() -> {
               try {
                  this.applyPollSnapshot(
                          snapshot,
                          finalSelectedEventId,
                          finalOrderOption,
                          finalSelectedActiveId,
                          finalSelectedClosedId,
                          finalOtherUser
                  );
               } finally {
                  this.pollInProgress.set(false);
               }
            });
         } catch (GuessMarketHttpException exception) {
            Platform.runLater(() -> {
               this.pollConnectionError = true;
               if (this.statusLabel.getText() == null
                       || !this.statusLabel.getText().equals(exception.getMessage())) {
                  this.statusLabel.setText(exception.getMessage());
               }
               this.pollInProgress.set(false);
            });
         } catch (RuntimeException exception) {
            Platform.runLater(() -> this.pollInProgress.set(false));
         }
      });
      worker.setDaemon(true);
      worker.start();
   }


   private String privatePollUserName() {
      if (this.loggedInUserName != null && !this.loggedInUserName.trim().isEmpty()) {
         return this.loggedInUserName.trim();
      }
      String fromSession = ClientSession.getCurrentUserName();
      if (fromSession == null || fromSession.trim().isEmpty()) {
         return null;
      }
      return fromSession.trim();
   }


   private String selectedOtherUserRow() {
      Object selected = this.usersListView.getSelectionModel().getSelectedItem();
      return selected == null ? null : selected.toString();
   }


   private Integer selectedEventIdFrom(List events, ListView listView) {
      int index = listView.getSelectionModel().getSelectedIndex();
      if (index >= 0 && index < events.size()) {
         return ((EventSummary) events.get(index)).id();
      }
      return null;
   }


   private PollSnapshot collectPollSnapshot(
           String currentUserName,
           Integer selectedEventId,
           Integer selectedOptionIndex,
           Integer selectedActiveId,
           Integer selectedClosedId) {

      PollSnapshot snapshot = new PollSnapshot();
      snapshot.events = this.fetchEvents();
      snapshot.users = this.fetchUsers();
      snapshot.eventAccounts = new HashMap();
      for (EventSummary event : snapshot.events) {
         snapshot.eventAccounts.put(event.id(), this.fetchEventAccount(event.id()));
      }

      if (currentUserName != null && !currentUserName.trim().isEmpty()) {
         String userName = currentUserName.trim();
         snapshot.currentUserName = userName;
         snapshot.accountHistory = this.fetchAccountHistory(userName);
         snapshot.activeEvents = this.fetchUserActiveEvents(userName);
         snapshot.closedEvents = this.fetchUserClosedEvents(userName);
         for (UserSummary user : snapshot.users) {
            if (user.getName().equals(userName)) {
               snapshot.currentUser = user;
               break;
            }
         }
      }

      EventSummary selectedEvent = this.findEventById(snapshot.events, selectedEventId);
      if (selectedEvent != null) {
         snapshot.selectedEvent = selectedEvent;
         snapshot.selectedEventAccount = (Double) snapshot.eventAccounts.get(selectedEvent.id());
         if (snapshot.currentUserName != null) {
            snapshot.selectedShares =
                    this.fetchUserShares(snapshot.currentUserName, selectedEvent.id());
         }
         if (selectedEvent.marketMethodType() == MarketMethodType.LMSR) {
            snapshot.eventDetails = this.fetchEventDetails(selectedEvent.id());
         } else {
            int optionIndex = selectedOptionIndex == null ? 0 : selectedOptionIndex;
            snapshot.pendingOrders = this.fetchPendingOrders(selectedEvent.id(), optionIndex);
            snapshot.orderBookStatistics =
                    this.fetchOrderBookStatistics(selectedEvent.id(), optionIndex);
            snapshot.orderBookParticipants =
                    this.fetchOrderBookParticipants(selectedEvent.id());
         }
      }

      EventSummary participated =
              this.findEventById(snapshot.activeEvents, selectedActiveId);
      if (participated == null) {
         participated = this.findEventById(snapshot.closedEvents, selectedClosedId);
      }
      if (participated != null && snapshot.currentUserName != null) {
         snapshot.participatedEvent = participated;
         snapshot.participatedCommission =
                 this.fetchUserEventCommission(snapshot.currentUserName, participated.id());
         if (participated.marketMethodType() == MarketMethodType.LMSR) {
            snapshot.participatedLmsrTrades =
                    this.fetchUserLmsrTrades(snapshot.currentUserName, participated.id());
            snapshot.participatedShares =
                    this.fetchUserShares(snapshot.currentUserName, participated.id());
            if (participated.status() == EventStatus.CLOSED) {
               snapshot.participatedEventDetails =
                       this.fetchEventDetails(participated.id());
            }
         } else {
            snapshot.participatedObAmountPaid =
                    this.fetchUserObAmountPaid(snapshot.currentUserName, participated.id());
            snapshot.participatedShares =
                    this.fetchUserShares(snapshot.currentUserName, participated.id());
            if (participated.status() == EventStatus.CLOSED) {
               snapshot.participatedObProfitLoss =
                       this.fetchUserObProfitLoss(snapshot.currentUserName, participated.id());
            }
         }
      }

      snapshot.chatCollectedAtNanos = System.nanoTime();
      snapshot.chatMessages = this.fetchChat();
      return snapshot;
   }


   private EventSummary findEventById(List events, Integer eventId) {
      if (events == null || eventId == null) {
         return null;
      }
      for (int i = 0; i < events.size(); i++) {
         EventSummary event = (EventSummary) events.get(i);
         if (event.id() == eventId) {
            return event;
         }
      }
      return null;
   }


   private void applyPollSnapshot(
           PollSnapshot snapshot,
           Integer selectedEventId,
           String selectedOrderOption,
           Integer selectedActiveId,
           Integer selectedClosedId,
           String selectedOtherUser) {

      this.applyingPoll = true;
      try {
         this.applyUsersFromSnapshot(snapshot, selectedOtherUser);
         this.applyChatFromSnapshot(snapshot);
         this.applyEventsFromSnapshot(snapshot, selectedEventId);
         this.applyAccountFromSnapshot(snapshot);
         this.applyParticipatedListsFromSnapshot(snapshot, selectedActiveId, selectedClosedId);

         EventSummary selectedEvent = this.getSelectedDisplayedEvent();
         if (selectedEvent != null) {
            this.applySelectedEventFromSnapshot(snapshot, selectedEvent, selectedOrderOption);
            this.applyHoldingsFromSnapshot(snapshot, selectedEvent);
         }

         if (snapshot.participatedEvent != null) {
            this.applyParticipatedDetailsFromSnapshot(snapshot);
         }

         if (this.pollConnectionError) {
            this.pollConnectionError = false;
            this.statusLabel.setText("Connected to the server.");
         }
      } finally {
         this.applyingPoll = false;
      }
   }


   private void applyUsersFromSnapshot(PollSnapshot snapshot, String selectedOtherUser) {
      this.usersListView.getItems().clear();
      String currentUserName = snapshot.currentUserName;
      int otherIndex = -1;
      if (snapshot.users != null) {
         for (UserSummary user : snapshot.users) {
            if (currentUserName != null && currentUserName.equals(user.getName())) {
               continue;
            }
            String row = this.formatOtherUserRow(user);
            this.usersListView.getItems().add(row);
            if (selectedOtherUser != null && selectedOtherUser.equals(row)) {
               otherIndex = this.usersListView.getItems().size() - 1;
            }
         }
      }
      if (otherIndex >= 0) {
         this.usersListView.getSelectionModel().select(otherIndex);
      }
      if (snapshot.currentUser != null) {
         this.selectedUserNameLabel.setText("Name: " + snapshot.currentUser.getName());
         this.selectedUserCashLabel.setText("Balance: " + snapshot.currentUser.getBalance());
         String blockedText = "Blocked: " + snapshot.currentUser.isBlocked();
         if (snapshot.currentUser.getMarketMakerEventIds() != null
                 && !snapshot.currentUser.getMarketMakerEventIds().isEmpty()) {
            blockedText = blockedText + " | MM events: " + snapshot.currentUser.getMarketMakerEventIds();
         }
         this.selectedUserBlockedLabel.setText(blockedText);
      }
   }


   private void applyEventsFromSnapshot(PollSnapshot snapshot, Integer selectedEventId) {
      this.eventsListView.getItems().clear();
      this.displayedEvents.clear();
      int indexToSelect = -1;
      if (snapshot.events != null) {
         for (EventSummary event : snapshot.events) {
            if (this.eventMatchesCurrentFilters(event)) {
               this.displayedEvents.add(event);
               double account = snapshot.eventAccounts != null
                       && snapshot.eventAccounts.get(event.id()) != null
                       ? (Double) snapshot.eventAccounts.get(event.id())
                       : 0.0;
               this.eventsListView.getItems().add(
                       event.name() + " | " + String.valueOf(event.status()) + " | "
                               + String.valueOf(event.marketMethodType()) + " | "
                               + event.commissionType().xmlValue() + " " + event.commissionPercent()
                               + "% | Account: " + String.format("%.2f", account)
               );
               if (selectedEventId != null && event.id() == selectedEventId) {
                  indexToSelect = this.displayedEvents.size() - 1;
               }
            }
         }
      }
      if (indexToSelect >= 0) {
         this.eventsListView.getSelectionModel().select(indexToSelect);
      }
   }


   private void applyAccountFromSnapshot(PollSnapshot snapshot) {
      if (this.accountHistoryListView == null) {
         return;
      }
      this.accountHistoryListView.getItems().clear();
      if (snapshot.accountHistory != null) {
         for (AccountHistoryRow row : snapshot.accountHistory) {
            this.accountHistoryListView.getItems().add(
                    row.description()
                            + " | "
                            + String.format("%.2f", row.amount())
                            + " | "
                            + String.format("%.2f", row.balanceAfter())
            );
         }
      }
   }


   private void applyParticipatedListsFromSnapshot(
           PollSnapshot snapshot,
           Integer selectedActiveId,
           Integer selectedClosedId) {

      this.userActiveEvents.clear();
      this.userActiveEventsListView.getItems().clear();
      int activeIndex = -1;
      if (snapshot.activeEvents != null) {
         for (EventSummary event : snapshot.activeEvents) {
            this.userActiveEvents.add(event);
            this.userActiveEventsListView.getItems().add(event.id() + " - " + event.name());
            if (selectedActiveId != null && event.id() == selectedActiveId) {
               activeIndex = this.userActiveEvents.size() - 1;
            }
         }
      }

      this.userClosedEvents.clear();
      this.userClosedEventsListView.getItems().clear();
      int closedIndex = -1;
      if (snapshot.closedEvents != null) {
         for (EventSummary event : snapshot.closedEvents) {
            this.userClosedEvents.add(event);
            this.userClosedEventsListView.getItems().add(event.id() + " - " + event.name());
            if (selectedClosedId != null && event.id() == selectedClosedId) {
               closedIndex = this.userClosedEvents.size() - 1;
            }
         }
      }

      if (activeIndex >= 0) {
         this.userActiveEventsListView.getSelectionModel().select(activeIndex);
      }
      if (closedIndex >= 0) {
         this.userClosedEventsListView.getSelectionModel().select(closedIndex);
      }
   }


   private void applySelectedEventFromSnapshot(
           PollSnapshot snapshot,
           EventSummary event,
           String selectedOrderOption) {

      this.eventIdLabel.setText("ID: " + event.id());
      this.eventNameLabel.setText("Name: " + event.name());
      this.eventStatusLabel.setText("Status: " + String.valueOf(event.status()));
      this.openEventButton.setDisable(event.status() != EventStatus.NOT_STARTED);
      this.eventDescriptionLabel.setText("Description: " + event.description());
      this.eventCommissionLabel.setText(
              "Commission: " + event.commissionPercent() + " (" + String.valueOf(event.commissionType()) + ")"
      );
      if (snapshot.selectedEventAccount != null) {
         this.eventAccountBalanceLabel.setText(
                 "Event account balance: " + String.format("%.2f", snapshot.selectedEventAccount)
         );
      }

      if (event.marketMethodType() == MarketMethodType.LMSR) {
         this.eventMethodLabel.setText("Market method: LMSR");
         this.marketParam1Label.setText("b: " + event.liquidityParameter());
         this.applyLmsrDetailsFromSnapshot(snapshot, event);
      } else {
         this.eventMethodLabel.setText("Market method: Order Book");
         this.marketParam1Label.setText("Initial: " + event.orderBookInitial());
         this.marketParam2Label.setText("d: " + event.orderBookD());
         this.marketParam3Label.setText("Allow mint: " + event.allowMint());
         this.fillComboPreservingSelection(this.orderOptionComboBox, event.options(), selectedOrderOption);
         this.fillComboPreservingSelection(this.winnerComboBox, event.options(), selectedOrderOption);
         if (event.status() == EventStatus.ACTIVE) {
            this.enableOrderControls();
            this.winnerComboBox.setDisable(false);
            this.closeEventButton.setDisable(false);
         } else {
            this.disableOrderControls();
            this.winnerComboBox.setDisable(true);
            this.closeEventButton.setDisable(true);
         }
         this.applyOrderBookListsFromSnapshot(snapshot);
      }
   }


   private void applyLmsrDetailsFromSnapshot(PollSnapshot snapshot, EventSummary event) {
      EventDetails details = snapshot.eventDetails;
      if (details == null) {
         return;
      }
      this.lmsrDetailsBox.setVisible(true);
      this.lmsrDetailsBox.setManaged(true);
      if (details.options().size() > 0) {
         OptionState option1 = details.options().get(0);
         this.lmsrOption1StateLabel.setText(
                 option1.name() + " | current value: " + String.format("%.2f", option1.currentValue())
                         + " | purchased shares: " + option1.purchasedShares()
         );
      }
      if (details.options().size() > 1) {
         OptionState option2 = details.options().get(1);
         this.lmsrOption2StateLabel.setText(
                 option2.name() + " | current value: " + String.format("%.2f", option2.currentValue())
                         + " | purchased shares: " + option2.purchasedShares()
         );
      }
      this.lmsrCollectedCommissionLabel.setText(
              "Total commission collected: " + String.format("%.2f", details.collectedCommission())
      );
      this.lmsrTradeHistoryListView.getItems().clear();
      if (details.tradeHistory() == null || details.tradeHistory().isEmpty()) {
         this.lmsrTradeHistoryListView.getItems().add("No trades have been made.");
      } else {
         for (TradeView trade : details.tradeHistory()) {
            this.lmsrTradeHistoryListView.getItems().add(
                    "Option: " + trade.optionName() + " | Shares: " + trade.quantity()
                            + " | Price paid: " + String.format("%.2f", trade.pricePaid())
            );
         }
      }
      String preservedPurchase =
              this.lmsrPurchaseOptionComboBox.getSelectionModel().getSelectedItem() == null
                      ? null
                      : this.lmsrPurchaseOptionComboBox.getSelectionModel().getSelectedItem().toString();
      String preservedWinner =
              this.lmsrWinnerComboBox.getSelectionModel().getSelectedItem() == null
                      ? null
                      : this.lmsrWinnerComboBox.getSelectionModel().getSelectedItem().toString();
      this.fillComboPreservingSelection(this.lmsrPurchaseOptionComboBox, event.options(), preservedPurchase);
      this.fillComboPreservingSelection(this.lmsrWinnerComboBox, event.options(), preservedWinner);
      boolean active = event.status() == EventStatus.ACTIVE;
      this.lmsrPurchaseOptionComboBox.setDisable(!active);
      this.lmsrPurchaseQuantityField.setDisable(!active);
      this.lmsrPurchaseButton.setDisable(!active);
      this.lmsrWinnerComboBox.setDisable(!active);
      this.lmsrCloseButton.setDisable(!active);
      if (details.winningOption() == null) {
         this.lmsrClosedSummaryLabel.setText("");
      } else {
         String closedText = "Winning option: " + details.winningOption()
                 + "\nTotal purchased shares for each option:";
         for (OptionState option : details.options()) {
            closedText = closedText + "\n" + option.name() + ": " + option.purchasedShares();
         }
         this.lmsrClosedSummaryLabel.setText(closedText);
      }
   }


   private void applyOrderBookListsFromSnapshot(PollSnapshot snapshot) {
      this.pendingOrdersListView.getItems().clear();
      if (snapshot.pendingOrders == null || snapshot.pendingOrders.isEmpty()) {
         this.pendingOrdersListView.getItems().add("No pending orders");
      } else {
         for (PendingOrderInfo order : snapshot.pendingOrders) {
            this.pendingOrdersListView.getItems().add(
                    order.getUserName() + " | " + String.valueOf(order.getType())
                            + " | " + order.getQuantity() + " shares | " + order.getPrice()
            );
         }
      }

      this.participantsListView.getItems().clear();
      EventSummary event = snapshot.selectedEvent;
      if (snapshot.orderBookParticipants == null || snapshot.orderBookParticipants.isEmpty()) {
         this.participantsListView.getItems().add("No participants");
      } else if (event != null) {
         for (OrderBookParticipantInfo participant : snapshot.orderBookParticipants) {
            String text = participant.getUserName();
            for (int optionIndex = 0; optionIndex < event.options().size(); ++optionIndex) {
               int shares = (Integer) participant.getSharesPerOption().get(optionIndex);
               Double value = (Double) participant.getValuePerOption().get(optionIndex);
               text = text + " | " + event.options().get(optionIndex) + ": " + shares + " shares";
               if (value == null) {
                  text = text + " | value: N/A";
               } else {
                  text = text + " | value: " + String.format("%.2f", value);
               }
            }
            this.participantsListView.getItems().add(text);
         }
      }

      if (snapshot.orderBookStatistics != null) {
         this.lastPriceLabel.setText(this.formatStatistic(snapshot.orderBookStatistics.getLast()));
         this.bidPriceLabel.setText(this.formatStatistic(snapshot.orderBookStatistics.getBid()));
         this.askPriceLabel.setText(this.formatStatistic(snapshot.orderBookStatistics.getAsk()));
         this.midPriceLabel.setText(this.formatStatistic(snapshot.orderBookStatistics.getMid()));
         this.spreadLabel.setText(this.formatStatistic(snapshot.orderBookStatistics.getSpread()));
      }
   }


   private void applyHoldingsFromSnapshot(PollSnapshot snapshot, EventSummary event) {
      if (snapshot.selectedShares == null) {
         return;
      }
      String holdingsText = "Holdings in " + event.name() + ":\n";
      List<String> optionNames = snapshot.selectedShares.getOptionNames();
      List<Integer> shares = snapshot.selectedShares.getShares();
      if (optionNames == null) {
         optionNames = event.options();
      }
      for (int optionIndex = 0; optionIndex < optionNames.size(); ++optionIndex) {
         int shareCount = 0;
         if (shares != null && optionIndex < shares.size() && shares.get(optionIndex) != null) {
            shareCount = shares.get(optionIndex);
         }
         holdingsText = holdingsText + optionNames.get(optionIndex) + ": " + shareCount + " shares";
         if (optionIndex < optionNames.size() - 1) {
            holdingsText = holdingsText + "\n";
         }
      }
      this.selectedUserHoldingsLabel.setText(holdingsText);
   }


   private void applyParticipatedDetailsFromSnapshot(PollSnapshot snapshot) {
      EventSummary event = snapshot.participatedEvent;
      this.userEventDetailsTitleLabel.setText(event.name());
      if (snapshot.participatedCommission != null) {
         this.userEventCommissionLabel.setText(
                 "Total commission paid for this event: "
                         + String.format("%.2f", snapshot.participatedCommission.getCommission())
         );
      }
      if (event.marketMethodType() == MarketMethodType.LMSR) {
         this.userObDetailsBox.setVisible(false);
         this.userObDetailsBox.setManaged(false);
         this.userLmsrDetailsBox.setVisible(true);
         this.userLmsrDetailsBox.setManaged(true);
         this.userLmsrTradesListView.getItems().clear();
         if (snapshot.participatedLmsrTrades != null) {
            for (UserLmsrTrade trade : snapshot.participatedLmsrTrades) {
               this.userLmsrTradesListView.getItems().add(
                       "Option: " + trade.getOptionName()
                               + " | Shares: " + trade.getQuantity()
                               + " | Payment: " + String.format("%.2f", trade.getSharesPrice())
                               + " | Commission: " + String.format("%.2f", trade.getCommission())
               );
            }
         }
         if (event.status() == EventStatus.CLOSED && snapshot.participatedEventDetails != null) {
            String closedText = "Winning option: " + snapshot.participatedEventDetails.winningOption();
            if (snapshot.participatedShares != null && snapshot.participatedShares.getOptionNames() != null) {
               for (int i = 0; i < snapshot.participatedShares.getOptionNames().size(); i++) {
                  int shareCount = 0;
                  if (snapshot.participatedShares.getShares() != null
                          && i < snapshot.participatedShares.getShares().size()
                          && snapshot.participatedShares.getShares().get(i) != null) {
                     shareCount = snapshot.participatedShares.getShares().get(i);
                  }
                  closedText = closedText + "\n" + snapshot.participatedShares.getOptionNames().get(i)
                          + ": " + shareCount + " shares";
               }
            }
            this.userLmsrClosedInfoLabel.setText(closedText);
         }
      } else {
         this.userLmsrDetailsBox.setVisible(false);
         this.userLmsrDetailsBox.setManaged(false);
         this.userObDetailsBox.setVisible(true);
         this.userObDetailsBox.setManaged(true);
         this.userObHoldingsListView.getItems().clear();
         List<String> options = event.options();
         List<Double> amountPaid = snapshot.participatedObAmountPaid == null
                 ? null
                 : snapshot.participatedObAmountPaid.getAmountPaid();
         List<Integer> shares = snapshot.participatedShares == null
                 ? null
                 : snapshot.participatedShares.getShares();
         for (int i = 0; i < options.size(); i++) {
            int shareCount = 0;
            if (shares != null && i < shares.size() && shares.get(i) != null) {
               shareCount = shares.get(i);
            }
            double paid = 0.0;
            if (amountPaid != null && i < amountPaid.size() && amountPaid.get(i) != null) {
               paid = amountPaid.get(i);
            }
            this.userObHoldingsListView.getItems().add(
                    options.get(i) + " | Shares: " + shareCount + " | Amount paid: " + String.format("%.2f", paid)
            );
         }
         if (event.status() == EventStatus.CLOSED && snapshot.participatedObProfitLoss != null) {
            this.userObProfitLossLabel.setText(
                    "Profit/Loss: " + String.format("%.2f", snapshot.participatedObProfitLoss.getProfitLoss())
            );
         } else if (event.status() != EventStatus.CLOSED) {
            this.userObProfitLossLabel.setText("");
         }
      }
   }


   private void fillComboPreservingSelection(
           ComboBox combo,
           List<String> options,
           String preserved) {

      Object current = preserved != null ? preserved : combo.getSelectionModel().getSelectedItem();
      combo.getItems().setAll(options);
      if (current != null && combo.getItems().contains(current)) {
         combo.getSelectionModel().select(current);
      } else if (!combo.getItems().isEmpty() && combo.getSelectionModel().getSelectedIndex() < 0) {
         combo.getSelectionModel().select(0);
      }
   }


   private static class PollSnapshot {
      List<EventSummary> events;
      List<UserSummary> users;
      List<ChatMessageResponse> chatMessages;
      long chatCollectedAtNanos;
      Map eventAccounts;
      String currentUserName;
      UserSummary currentUser;
      List<AccountHistoryRow> accountHistory;
      List<EventSummary> activeEvents;
      List<EventSummary> closedEvents;
      EventSummary selectedEvent;
      Double selectedEventAccount;
      UserSharesResponse selectedShares;
      EventDetails eventDetails;
      List<PendingOrderInfo> pendingOrders;
      OrderBookStatistics orderBookStatistics;
      List<OrderBookParticipantInfo> orderBookParticipants;
      EventSummary participatedEvent;
      UserEventCommissionResponse participatedCommission;
      List<UserLmsrTrade> participatedLmsrTrades;
      UserSharesResponse participatedShares;
      EventDetails participatedEventDetails;
      UserObAmountPaidResponse participatedObAmountPaid;
      UserObProfitLossResponse participatedObProfitLoss;
   }


   private List<UserSummary> fetchUsers() {
      List<UserSummary> users =
              this.httpClient.getJson(
                      "/users",
                      USER_LIST_TYPE
              );
      if (users == null) {
         return new ArrayList();
      }
      return users;
   }


   private List<ChatMessageResponse> fetchChat() {
      List<ChatMessageResponse> messages =
              this.httpClient.getJson(
                      "/chat",
                      CHAT_LIST_TYPE
              );
      if (messages == null) {
         return new ArrayList();
      }
      return messages;
   }


   private UserSummary findCurrentUser(String userName) {
      List<UserSummary> users = this.fetchUsers();
      for (UserSummary user : users) {
         if (user.getName().equals(userName)) {
            return user;
         }
      }
      return null;
   }


   private List<AccountHistoryRow> fetchAccountHistory(String userName) {
      List<AccountHistoryRow> rows =
              this.httpClient.getJson(
                      this.userQuery("/account-history", userName),
                      ACCOUNT_HISTORY_TYPE
              );
      if (rows == null) {
         return new ArrayList();
      }
      return rows;
   }


   private List<EventSummary> fetchUserActiveEvents(String userName) {
      List<EventSummary> events =
              this.httpClient.getJson(
                      this.userQuery("/user-active-events", userName),
                      EVENT_LIST_TYPE
              );
      if (events == null) {
         return new ArrayList();
      }
      return events;
   }


   private List<EventSummary> fetchUserClosedEvents(String userName) {
      List<EventSummary> events =
              this.httpClient.getJson(
                      this.userQuery("/user-closed-events", userName),
                      EVENT_LIST_TYPE
              );
      if (events == null) {
         return new ArrayList();
      }
      return events;
   }


   private List<UserLmsrTrade> fetchUserLmsrTrades(
           String userName,
           int eventId) {

      List<UserLmsrTrade> trades =
              this.httpClient.getJson(
                      this.userEventQuery("/user-lmsr-trades", userName, eventId),
                      USER_LMSR_TRADES_TYPE
              );
      if (trades == null) {
         return new ArrayList();
      }
      return trades;
   }


   private UserEventCommissionResponse fetchUserEventCommission(
           String userName,
           int eventId) {

      UserEventCommissionResponse response =
              this.httpClient.getJson(
                      this.userEventQuery("/user-event-commission", userName, eventId),
                      UserEventCommissionResponse.class
              );
      if (response == null) {
         throw new GuessMarketHttpException("Commission data was missing.");
      }
      return response;
   }


   private UserObAmountPaidResponse fetchUserObAmountPaid(
           String userName,
           int eventId) {

      UserObAmountPaidResponse response =
              this.httpClient.getJson(
                      this.userEventQuery("/user-ob-amount-paid", userName, eventId),
                      UserObAmountPaidResponse.class
              );
      if (response == null) {
         throw new GuessMarketHttpException("Amount-paid data was missing.");
      }
      return response;
   }


   private UserObProfitLossResponse fetchUserObProfitLoss(
           String userName,
           int eventId) {

      UserObProfitLossResponse response =
              this.httpClient.getJson(
                      this.userEventQuery("/user-ob-profit-loss", userName, eventId),
                      UserObProfitLossResponse.class
              );
      if (response == null) {
         throw new GuessMarketHttpException("Profit/loss data was missing.");
      }
      return response;
   }


   private String userQuery(String path, String userName) {
      return path + "?userName=" + URLEncoder.encode(userName, StandardCharsets.UTF_8);
   }


   private String userEventQuery(
           String path,
           String userName,
           int eventId) {

      return this.userQuery(path, userName) + "&eventId=" + eventId;
   }


   private List<EventSummary> fetchEvents() {
      List<EventSummary> events =
              this.httpClient.getJson(
                      "/events",
                      EVENT_LIST_TYPE
              );

      if (events == null) {
         return new ArrayList();
      }

      return events;
   }


   private double fetchEventAccount(int eventId) {
      EventAccountResponse response =
              this.httpClient.getJson(
                      "/event-account?eventId=" + eventId,
                      EventAccountResponse.class
              );

      if (response == null) {
         throw new GuessMarketHttpException(
                 "Event account data was missing."
         );
      }

      return response.getBalance();
   }


   private EventDetails fetchEventDetails(int eventId) {
      return this.httpClient.getJson(
              "/event-details?eventId=" + eventId,
              EventDetails.class
      );
   }


   private List<PendingOrderInfo> fetchPendingOrders(
           int eventId,
           int optionIndex) {

      List<PendingOrderInfo> orders =
              this.httpClient.getJson(
                      "/pending-orders?eventId="
                              + eventId
                              + "&optionIndex="
                              + optionIndex,
                      PENDING_ORDERS_TYPE
              );

      if (orders == null) {
         return new ArrayList();
      }

      return orders;
   }


   private OrderBookStatistics fetchOrderBookStatistics(
           int eventId,
           int optionIndex) {

      OrderBookStatistics statistics =
              this.httpClient.getJson(
                      "/order-book-statistics?eventId="
                              + eventId
                              + "&optionIndex="
                              + optionIndex,
                      OrderBookStatistics.class
              );

      if (statistics == null) {
         return new OrderBookStatistics(
                 null,
                 null,
                 null,
                 null,
                 null
         );
      }

      return statistics;
   }


   private List<OrderBookParticipantInfo> fetchOrderBookParticipants(
           int eventId) {

      List<OrderBookParticipantInfo> participants =
              this.httpClient.getJson(
                      "/order-book-participants?eventId=" + eventId,
                      PARTICIPANTS_TYPE
              );

      if (participants == null) {
         return new ArrayList();
      }

      return participants;
   }


   private String requireLoggedInUserName() {
      String userName = ClientSession.getCurrentUserName();
      if (userName == null || userName.trim().isEmpty()) {
         this.statusLabel.setText("Please log in first.");
         return null;
      }

      return userName.trim();
   }


   private void refreshSelectedEventFromServer() {
      this.loadEventsToList();
      EventSummary updatedEvent = this.getSelectedDisplayedEvent();
      if (updatedEvent != null) {
         this.showEventDetails(updatedEvent);
         this.showSelectedUserHoldings();
      }
      this.refreshLoggedInUserAccount();
   }


   void refreshLoggedInUserAccount() {
      this.loadUsersToList();
      this.showLoggedInUserDetails();
      this.loadAccountHistory();
      this.loadUserActiveEvents();
   }


   void loadAccountHistory() {
      if (this.accountHistoryListView == null) {
         return;
      }

      this.accountHistoryListView.getItems().clear();
      String userName = ClientSession.getCurrentUserName();
      if (userName == null || userName.trim().isEmpty()) {
         return;
      }

      try {
         List<AccountHistoryRow> rows = this.fetchAccountHistory(userName.trim());
         for (AccountHistoryRow row : rows) {
            this.accountHistoryListView.getItems().add(
                    row.description()
                            + " | "
                            + String.format("%.2f", row.amount())
                            + " | "
                            + String.format("%.2f", row.balanceAfter())
            );
         }
      } catch (GuessMarketHttpException exception) {
         this.statusLabel.setText(exception.getMessage());
      }
   }


   private void closeEventOnServer(
           int eventId,
           int winningOptionIndex) {

      String userName = this.requireLoggedInUserName();
      if (userName == null) {
         return;
      }

      try {
         Map<String, String> fields = new HashMap();
         fields.put("userName", userName);
         fields.put("eventId", String.valueOf(eventId));
         fields.put("winningOptionIndex", String.valueOf(winningOptionIndex));
         CloseEventResponse response =
                 this.httpClient.postFormJson(
                         "/close-event",
                         fields,
                         CloseEventResponse.class
                 );
         this.refreshSelectedEventFromServer();
         if (response != null && response.getWinningOption() != null) {
            this.statusLabel.setText(
                    "Event closed successfully. Winner: " + response.getWinningOption()
            );
         } else if (response != null && response.getMessage() != null && !response.getMessage().isBlank()) {
            this.statusLabel.setText(response.getMessage());
         } else {
            this.statusLabel.setText("Event closed successfully.");
         }
      } catch (GuessMarketHttpException exception) {
         this.statusLabel.setText(exception.getMessage());
      }
   }


   private UserSharesResponse fetchUserShares(
           String userName,
           int eventId) {

      String encodedName =
              URLEncoder.encode(
                      userName,
                      StandardCharsets.UTF_8
              );

      UserSharesResponse response =
              this.httpClient.getJson(
                      "/user-shares?userName="
                              + encodedName
                              + "&eventId="
                              + eventId,
                      UserSharesResponse.class
              );

      if (response == null) {
         throw new GuessMarketHttpException(
                 "User shares data was missing."
         );
      }

      return response;
   }
}
