package guessmarket.engine.api;

import guessmarket.engine.dto.AccountHistoryRow;
import guessmarket.engine.dto.EventDetails;
import guessmarket.engine.dto.EventSummary;
import guessmarket.engine.dto.LoadResult;
import guessmarket.engine.dto.OrderBookParticipantInfo;
import guessmarket.engine.dto.OrderBookStatistics;
import guessmarket.engine.dto.PendingOrderInfo;
import guessmarket.engine.dto.UserSummary;

import guessmarket.engine.model.CloseResult;
import guessmarket.engine.model.OrderType;
import guessmarket.engine.model.PurchaseResult;
import guessmarket.engine.model.UserLmsrTrade;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;


public interface GuessMarketEngine {


    LoadResult loadXml(
            Path xmlPath
    );


    LoadResult loadXml(
            Path xmlPath,
            String uploaderName
    );


    LoadResult loadXml(
            InputStream xmlSource,
            String uploaderName
    );


    boolean hasLoadedSystem();


    List<EventSummary> getAllEvents();


    List<EventSummary> getActiveEvents();


    List<UserSummary> getAllUsers();


    void registerUser(
            String userName
    );


    void addFunds(
            String userName,
            double amount
    );


    List<AccountHistoryRow> getAccountHistory(
            String userName
    );


    List<EventSummary> getUserActiveEvents(
            String userName
    );


    List<EventSummary> getUserClosedEvents(
            String userName
    );


    List<UserLmsrTrade> getUserLmsrTrades(
            String userName,
            int eventId
    );


    double getUserTotalCommissionPaid(
            String userName
    );


    List<Double> getUserObAmountPaid(
            String userName,
            int eventId
    );


    double getUserCommissionPaidForEvent(
            String userName,
            int eventId
    );


    double getUserOrderBookProfitLoss(
            String userName,
            int eventId
    );


    int getUserShares(
            String userName,
            int eventId,
            int optionIndex
    );


    EventDetails getEventDetails(
            int eventId
    );

    double getEventAccountBalance(
            int eventId
    );

    void openEvent(
            String userName,
            int eventId
    );


    void submitOrder(
            String userName,
            int eventId,
            int optionIndex,
            OrderType type,
            int quantity,
            double price
    );


    List<PendingOrderInfo> getPendingOrders(
            int eventId,
            int optionIndex
    );


    OrderBookStatistics getOrderBookStatistics(
            int eventId,
            int optionIndex
    );


    List<OrderBookParticipantInfo> getOrderBookParticipants(
            int eventId
    );


    PurchaseResult purchaseShares(
            int eventId,
            int optionIndex,
            int quantity
    );


    PurchaseResult purchaseShares(
            String userName,
            int eventId,
            int optionIndex,
            int quantity
    );


    CloseResult closeEvent(
            int eventId,
            int winningOptionIndex
    );


    CloseResult closeOrderBookEvent(
            String userName,
            int eventId,
            int winningOptionIndex
    );


    CloseResult closeLmsrEvent(
            String marketMakerName,
            int eventId,
            int winningOptionIndex
    );
}