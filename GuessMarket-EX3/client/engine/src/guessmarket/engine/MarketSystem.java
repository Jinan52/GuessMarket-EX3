package guessmarket.engine;

import guessmarket.engine.api.GuessMarketEngine;

import guessmarket.engine.dto.AccountHistoryRow;
import guessmarket.engine.dto.EventDetails;
import guessmarket.engine.dto.EventSummary;
import guessmarket.engine.dto.LoadResult;
import guessmarket.engine.dto.OptionState;
import guessmarket.engine.dto.OrderBookStatistics;
import guessmarket.engine.dto.PendingOrderInfo;
import guessmarket.engine.dto.TradeView;
import guessmarket.engine.dto.UserSummary;
import guessmarket.engine.dto.OrderBookParticipantInfo;

import guessmarket.engine.model.CloseResult;
import guessmarket.engine.model.CommissionType;
import guessmarket.engine.model.EventStatus;
import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.MarketMethodType;
import guessmarket.engine.model.MarketOption;
import guessmarket.engine.model.MarketUser;
import guessmarket.engine.model.Order;
import guessmarket.engine.model.OrderBook;
import guessmarket.engine.model.OrderType;
import guessmarket.engine.model.PurchaseResult;
import guessmarket.engine.model.UserLmsrTrade;

import guessmarket.generated.GMEvent;
import guessmarket.generated.GuessMarket;

import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MarketSystem
        implements GuessMarketEngine {

    private Map<Integer, MarketEvent> events =
            new LinkedHashMap<>();


    private Map<String, MarketUser> users =
            new LinkedHashMap<>();


    private int nextEventId =
            1;


    @Override
    public LoadResult loadXml(
            Path xmlPath) {

        return LoadResult.failure(
                "XML must be loaded by a registered user."
        );
    }


    @Override
    public LoadResult loadXml(
            Path xmlPath,
            String uploaderName) {

        try (InputStream xmlSource =
                     Files.newInputStream(
                             xmlPath
                     )) {

            return loadXml(
                    xmlSource,
                    uploaderName
            );

        } catch (IOException exception) {

            return LoadResult.failure(
                    "The XML file was not loaded. "
                            + readableMessage(
                            exception
                    )
            );
        }
    }


    @Override
    public LoadResult loadXml(
            InputStream xmlSource,
            String uploaderName) {

        try {

            if (uploaderName == null
                    ||
                    uploaderName.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "User name cannot be empty."
                );
            }


            String trimmedUploaderName =
                    uploaderName.trim();


            MarketUser uploader =
                    users.get(
                            trimmedUploaderName
                    );


            if (uploader == null) {

                throw new IllegalArgumentException(
                        "No user with name "
                                + trimmedUploaderName
                                + " exists."
                );
            }


            XmlMarketLoaderEx2 loader =
                    new XmlMarketLoaderEx2();


            GuessMarket loadedMarket =
                    loader.load(
                            xmlSource
                    );


            List<GMEvent> xmlEvents =
                    loadedMarket
                            .getGMEvents()
                            .getGMEvent();


            for (GMEvent xmlEvent : xmlEvents) {

                if (eventNameExists(
                        xmlEvent.getName()
                )) {

                    throw new IllegalArgumentException(
                            "Duplicate event name: "
                                    + xmlEvent.getName()
                    );
                }
            }


            List<MarketEvent> eventsToAdd =
                    new ArrayList<>();


            int candidateEventId =
                    nextEventId;


            for (GMEvent xmlEvent : xmlEvents) {

                eventsToAdd.add(
                        createMarketEvent(
                                xmlEvent,
                                candidateEventId
                        )
                );


                candidateEventId++;
            }


            for (MarketEvent marketEvent : eventsToAdd) {

                events.put(
                        marketEvent.getId(),
                        marketEvent
                );


                uploader.addMarketMakerEvent(
                        marketEvent.getId()
                );
            }


            int addedCount =
                    eventsToAdd.size();


            nextEventId =
                    candidateEventId;


            return LoadResult.success(
                    addedCount
            );


        } catch (JAXBException |
                 RuntimeException exception) {

            return LoadResult.failure(
                    "The XML file was not loaded. "
                            + readableMessage(
                            exception
                    )
            );
        }
    }


    private boolean eventNameExists(
            String eventName) {

        for (MarketEvent event :
                events.values()) {

            if (event.getName().equals(
                    eventName
            )) {

                return true;
            }
        }


        return false;
    }


    private MarketEvent createMarketEvent(
            GMEvent xmlEvent,
            int eventId) {

        List<String> optionNames =
                new ArrayList<>();


        for (String option :
                xmlEvent
                        .getGMOptions()
                        .getGMOption()) {

            optionNames.add(
                    option
            );
        }


        CommissionType commissionType =
                CommissionType.fromXml(
                        xmlEvent
                                .getCommission()
                                .getType()
                );


        /*
         * LMSR.
         */
        if (xmlEvent
                .getGMMethod()
                .getGMLMSR() != null) {

            return new MarketEvent(
                    eventId,
                    xmlEvent.getName(),
                    xmlEvent.getDescription(),

                    xmlEvent
                            .getCommission()
                            .getValue(),

                    commissionType,

                    optionNames,

                    xmlEvent
                            .getGMMethod()
                            .getGMLMSR()
                            .getB()
            );
        }


        /*
         * Order Book.
         */
        if (xmlEvent
                .getGMMethod()
                .getGMOrderBook() != null) {

            int initial =
                    xmlEvent
                            .getGMMethod()
                            .getGMOrderBook()
                            .getInitial();


            int d =
                    xmlEvent
                            .getGMMethod()
                            .getGMOrderBook()
                            .getD();


            boolean allowMint =
                    Boolean.parseBoolean(
                            xmlEvent
                                    .getGMMethod()
                                    .getGMOrderBook()
                                    .getAllowMint()
                    );


            return new MarketEvent(
                    eventId,
                    xmlEvent.getName(),
                    xmlEvent.getDescription(),

                    xmlEvent
                            .getCommission()
                            .getValue(),

                    commissionType,

                    optionNames,

                    initial,
                    d,
                    allowMint
            );
        }


        throw new IllegalArgumentException(
                "Event "
                        + xmlEvent.getName()
                        + " does not have a market method."
        );
    }


    @Override
    public boolean hasLoadedSystem() {

        return !events.isEmpty();
    }


    @Override
    public List<EventSummary> getAllEvents() {

        List<EventSummary> result =
                new ArrayList<>();


        for (MarketEvent event :
                events.values()) {

            result.add(
                    toSummary(
                            event
                    )
            );
        }


        return result;
    }


    @Override
    public List<EventSummary> getActiveEvents() {

        List<EventSummary> result =
                new ArrayList<>();


        for (MarketEvent event :
                events.values()) {

            if (event.getStatus()
                    == EventStatus.ACTIVE) {

                result.add(
                        toSummary(
                                event
                        )
                );
            }
        }


        return result;
    }


    @Override
    public List<UserSummary> getAllUsers() {

        List<UserSummary> result =
                new ArrayList<>();


        for (MarketUser user :
                users.values()) {

            result.add(
                    new UserSummary(
                            user.getName(),
                            user.getBalance(),
                            user.isBlocked(),
                            user.getMarketMakerEventIds()
                    )
            );
        }


        return result;
    }


    @Override
    public void registerUser(
            String userName) {

        if (userName == null) {

            throw new IllegalArgumentException(
                    "User name cannot be empty."
            );
        }


        String trimmedName =
                userName.trim();


        if (trimmedName.isEmpty()) {

            throw new IllegalArgumentException(
                    "User name cannot be empty."
            );
        }


        if (users.containsKey(
                trimmedName
        )) {

            throw new IllegalArgumentException(
                    "A user with name "
                            + trimmedName
                            + " already exists."
            );
        }


        MarketUser newUser =
                new MarketUser(
                        trimmedName,
                        0.0
                );


        users.put(
                trimmedName,
                newUser
        );
    }


    @Override
    public void addFunds(
            String userName,
            double amount) {

        MarketUser user =
                users.get(
                        userName
                );


        if (user == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + userName
                            + " exists."
            );
        }


        if (Double.isNaN(amount)
                ||
                Double.isInfinite(amount)) {

            throw new IllegalArgumentException(
                    "Amount must be a finite number."
            );
        }


        if (amount <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than 0."
            );
        }


        user.deposit(
                amount,
                "Funds added"
        );
    }


    @Override
    public List<AccountHistoryRow> getAccountHistory(
            String userName) {

        MarketUser user =
                users.get(
                        userName
                );


        if (user == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + userName
                            + " exists."
            );
        }


        return user.getAccountHistory();
    }


    @Override
    public List<EventSummary> getUserActiveEvents(
            String userName) {

        MarketUser user =
                users.get(
                        userName
                );


        if (user == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + userName
                            + " exists."
            );
        }


        List<EventSummary> result =
                new ArrayList<>();


        List<Integer> participatedIds =
                user.getParticipatedEventIds();


        for (int i = 0;
             i < participatedIds.size();
             i++) {

            int eventId =
                    participatedIds.get(
                            i
                    );


            MarketEvent event =
                    events.get(
                            eventId
                    );


            if (event == null) {

                continue;
            }


            if (event.getStatus()
                    == EventStatus.ACTIVE) {

                result.add(
                        toSummary(
                                event
                        )
                );
            }
        }


        return result;
    }


    @Override
    public List<EventSummary> getUserClosedEvents(
            String userName) {

        MarketUser user =
                users.get(
                        userName
                );


        if (user == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + userName
                            + " exists."
            );
        }


        List<EventSummary> result =
                new ArrayList<>();


        List<Integer> participatedIds =
                user.getParticipatedEventIds();


        for (int i = 0;
             i < participatedIds.size();
             i++) {

            int eventId =
                    participatedIds.get(
                            i
                    );


            MarketEvent event =
                    events.get(
                            eventId
                    );


            if (event == null) {

                continue;
            }


            if (event.getStatus()
                    == EventStatus.CLOSED) {

                result.add(
                        toSummary(
                                event
                        )
                );
            }
        }


        return result;
    }


    @Override
    public List<UserLmsrTrade> getUserLmsrTrades(
            String userName,
            int eventId) {

        MarketUser user =
                users.get(
                        userName
                );


        if (user == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + userName
                            + " exists."
            );
        }


        List<UserLmsrTrade> allTrades =
                user.getLmsrTrades();


        List<UserLmsrTrade> result =
                new ArrayList<>();


        for (int i = allTrades.size() - 1;
             i >= 0;
             i--) {

            UserLmsrTrade trade =
                    allTrades.get(
                            i
                    );


            if (trade.getEventId()
                    == eventId) {

                result.add(
                        trade
                );
            }
        }


        return result;
    }


    @Override
    public double getUserTotalCommissionPaid(
            String userName) {

        MarketUser user =
                users.get(
                        userName
                );


        if (user == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + userName
                            + " exists."
            );
        }


        return user.getTotalCommissionPaid();
    }


    private MarketUser requireUser(
            String userName) {

        MarketUser user =
                users.get(
                        userName
                );


        if (user == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + userName
                            + " exists."
            );
        }


        return user;
    }


    @Override
    public List<Double> getUserObAmountPaid(
            String userName,
            int eventId) {

        MarketUser user =
                requireUser(
                        userName
                );


        MarketEvent event =
                requireEvent(
                        eventId
                );


        return user.getObAmountPaidPerOption(
                eventId,
                event.getOptions().size()
        );
    }


    @Override
    public double getUserCommissionPaidForEvent(
            String userName,
            int eventId) {

        MarketUser user =
                requireUser(
                        userName
                );


        requireEvent(
                eventId
        );


        return user.getCommissionPaidForEvent(
                eventId
        );
    }


    @Override
    public double getUserOrderBookProfitLoss(
            String userName,
            int eventId) {

        MarketUser user =
                requireUser(
                        userName
                );


        MarketEvent event =
                requireEvent(
                        eventId
                );


        if (event.getMarketMethodType()
                != MarketMethodType.ORDER_BOOK) {

            throw new IllegalStateException(
                    "The selected event is not an Order Book event."
            );
        }


        if (event.getStatus()
                != EventStatus.CLOSED) {

            throw new IllegalStateException(
                    "Profit/loss is available only after the event is closed."
            );
        }


        return user.getOrderBookProfitLoss(
                eventId
        );
    }


    @Override
    public void openEvent(
            String userName,
            int eventId) {

        MarketUser user =
                users.get(
                        userName
                );


        if (user == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + userName
                            + " exists."
            );
        }


        if (user.isBlocked()) {

            throw new IllegalStateException(
                    "This user is blocked."
            );
        }


        MarketEvent event =
                requireEvent(
                        eventId
                );


        if (!user.isMarketMakerOf(
                eventId
        )) {

            throw new IllegalStateException(
                    "The selected user is not the Market Maker of this event."
            );
        }


        if (event.getStatus()
                != EventStatus.NOT_STARTED) {

            throw new IllegalStateException(
                    "The event is not in NOT_STARTED status."
            );
        }


        double requiredAmount =
                event.getRequiredOpeningAmount();


        /*
         * Opening an event requires
         * sufficient money.
         */
        user.withdraw(
                requiredAmount
        );


        /*
         * Initial shares for Order Book.
         */
        if (event.getMarketMethodType()
                == MarketMethodType.ORDER_BOOK) {

            int d =
                    event.getOrderBookD();


            int initial =
                    event.getOrderBookInitial();


            if (d <= 0) {

                throw new IllegalStateException(
                        "Order Book base value d must be positive."
                );
            }


            int initialShares =
                    initial / d;


            for (int optionIndex = 0;
                 optionIndex
                         < event
                         .getOptions()
                         .size();
                 optionIndex++) {

                user.addShares(
                        eventId,
                        optionIndex,
                        initialShares,
                        event.getOptions().size()
                );
            }
        }


        event.open();
    }


    @Override
    public int getUserShares(
            String userName,
            int eventId,
            int optionIndex) {

        MarketUser user =
                users.get(
                        userName
                );


        if (user == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + userName
                            + " exists."
            );
        }


        requireEvent(
                eventId
        );


        return user.getShares(
                eventId,
                optionIndex
        );
    }


    @Override
    public void submitOrder(
            String userName,
            int eventId,
            int optionIndex,
            OrderType type,
            int quantity,
            double price) {

        MarketUser user =
                users.get(
                        userName
                );


        if (user == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + userName
                            + " exists."
            );
        }


        if (user.isBlocked()) {

            throw new IllegalStateException(
                    "This user is blocked."
            );
        }


        MarketEvent event =
                requireEvent(
                        eventId
                );


        if (event.getMarketMethodType()
                != MarketMethodType.ORDER_BOOK) {

            throw new IllegalStateException(
                    "The selected event is not an Order Book event."
            );
        }


        if (event.getStatus()
                != EventStatus.ACTIVE) {

            throw new IllegalStateException(
                    "The selected event is not active."
            );
        }


        if (optionIndex < 0
                ||
                optionIndex >=
                        event
                                .getOptions()
                                .size()) {

            throw new IllegalArgumentException(
                    "Invalid option selection."
            );
        }


        if (type == null) {

            throw new IllegalArgumentException(
                    "Order type cannot be empty."
            );
        }


        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Order quantity must be positive."
            );
        }


        if (price < 0) {

            throw new IllegalArgumentException(
                    "Order price cannot be negative."
            );
        }


        double maximumPrice =
                event.getOrderBookD()
                        - 0.01;


        if (price > maximumPrice) {

            throw new IllegalArgumentException(
                    "Order price cannot be greater than "
                            + maximumPrice
                            + "."
            );
        }


        /*
         * A seller cannot submit more shares
         * than they own, including shares
         * already offered in pending orders.
         */
        if (type
                == OrderType.SELL) {

            int ownedShares =
                    user.getShares(
                            eventId,
                            optionIndex
                    );


            int alreadyOfferedShares =
                    getPendingSellQuantity(
                            event,
                            optionIndex,
                            userName
                    );


            if (ownedShares
                    < quantity
                    + alreadyOfferedShares) {

                throw new IllegalStateException(
                        "The user does not have enough shares to sell."
                );
            }
        }


        Order newOrder =
                new Order(
                        userName,
                        type,
                        quantity,
                        price
                );


        OrderBook orderBook =
                event.getOrderBook(
                        optionIndex
                );


        orderBook.addOrder(
                newOrder
        );


        /*
         * First try normal BUY/SELL matching.
         */
        processMatches(
                event,
                orderBook,
                optionIndex,
                newOrder
        );


        /*
         * If a BUY order still remains,
         * try MINT.
         */
        if (type
                == OrderType.BUY
                &&
                !newOrder.isCompleted()) {

            processMint(
                    event,
                    optionIndex,
                    newOrder
            );
        }


        user.addParticipatedEvent(
                eventId
        );
    }

    @Override
    public List<OrderBookParticipantInfo> getOrderBookParticipants(
            int eventId) {

        MarketEvent event =
                requireEvent(
                        eventId
                );


        if (event.getMarketMethodType()
                != MarketMethodType.ORDER_BOOK) {

            throw new IllegalStateException(
                    "The selected event is not an Order Book event."
            );
        }


        List<OrderBookParticipantInfo> result =
                new ArrayList<>();


        for (MarketUser user :
                users.values()) {

            List<Integer> sharesPerOption =
                    new ArrayList<>();


            List<Double> valuePerOption =
                    new ArrayList<>();


            /*
             * Collect holdings and their value.
             * Membership is not based on these
             * current amounts.
             */
            for (int optionIndex = 0;
                 optionIndex < event.getOptions().size();
                 optionIndex++) {

                int shares =
                        user.getShares(
                                eventId,
                                optionIndex
                        );


                sharesPerOption.add(
                        shares
                );


                OrderBook orderBook =
                        event.getOrderBook(
                                optionIndex
                        );


                /*
                 * The appendix says that the current
                 * share value is usually represented
                 * by the price of the last transaction.
                 */
                Double lastPrice =
                        orderBook.getLastPrice();


                Double holdingValue =
                        null;


                if (lastPrice != null) {

                    holdingValue =
                            shares
                                    * lastPrice;
                }


                valuePerOption.add(
                        holdingValue
                );
            }


            if (user.getParticipatedEventIds()
                    .contains(
                            eventId
                    )) {

                result.add(
                        new OrderBookParticipantInfo(
                                user.getName(),
                                sharesPerOption,
                                valuePerOption
                        )
                );
            }
        }


        return result;
    }
    @Override
    public double getEventAccountBalance(
            int eventId) {

        MarketEvent event =
                requireEvent(
                        eventId
                );

        return event.getAccountBalance();
    }

    private int getPendingSellQuantity(
            MarketEvent event,
            int optionIndex,
            String userName) {

        int total =
                0;


        OrderBook orderBook =
                event.getOrderBook(
                        optionIndex
                );


        for (Order order :
                orderBook.getSellOrders()) {

            if (order.getUserName()
                    .equals(
                            userName
                    )) {

                total +=
                        order.getRemainingQuantity();
            }
        }


        return total;
    }


    /*
     * Normal BUY / SELL matching.
     */
    private void processMatches(
            MarketEvent event,
            OrderBook orderBook,
            int optionIndex,
            Order newOrder) {

        while (orderBook.hasBuyOrders()
                &&
                orderBook.hasSellOrders()) {


            Order buyOrder =
                    orderBook
                            .getHighestBuyOrder();


            Order sellOrder =
                    orderBook
                            .getLowestSellOrder();


            /*
             * No meeting between BID and ASK.
             */
            if (buyOrder.getPrice()
                    < sellOrder.getPrice()) {

                break;
            }


            int tradedQuantity =
                    Math.min(
                            buyOrder
                                    .getRemainingQuantity(),

                            sellOrder
                                    .getRemainingQuantity()
                    );


            /*
             * Waiting order determines
             * transaction price.
             */
            double tradePrice;


            if (newOrder.getType()
                    == OrderType.BUY) {

                tradePrice =
                        sellOrder.getPrice();

            } else {

                tradePrice =
                        buyOrder.getPrice();
            }


            double tradeAmount =
                    tradedQuantity
                            * tradePrice;


            /*
             * Save LAST.
             */
            orderBook.setLastPrice(
                    tradePrice
            );


            /*
             * ON_PURCHASE commission.
             */
            double commission =
                    0;


            if (event.getCommissionType()
                    == CommissionType.ON_PURCHASE) {

                commission =
                        tradeAmount
                                * event
                                .getCommissionPercent()
                                / 100.0;
            }


            MarketUser buyer =
                    users.get(
                            buyOrder
                                    .getUserName()
                    );


            MarketUser seller =
                    users.get(
                            sellOrder
                                    .getUserName()
                    );


            if (buyer == null
                    ||
                    seller == null) {

                throw new IllegalStateException(
                        "A user in the Order Book does not exist."
                );
            }


            MarketUser marketMaker =
                    findMarketMaker(
                            event.getId()
                    );


            /*
             * Buyer pays:
             * transaction + commission.
             */
            buyer.payForTrade(
                    tradeAmount
                            + commission
            );


            if (commission > 0) {

                buyer.addCommissionPaid(
                        commission,
                        event.getId()
                );
            }


            /*
             * Seller receives transaction money.
             */
            seller.deposit(
                    tradeAmount
            );


            buyer.addObAmountPaid(
                    event.getId(),
                    optionIndex,
                    tradeAmount,
                    event.getOptions().size()
            );


            seller.addObSellProceeds(
                    event.getId(),
                    tradeAmount
            );


            /*
             * Commission goes to MM.
             */
            if (commission > 0) {

                marketMaker.deposit(
                        commission
                );


                event.addCollectedCommission(
                        commission
                );
            }


            /*
             * Shares move from seller
             * to buyer.
             */
            seller.removeShares(
                    event.getId(),
                    optionIndex,
                    tradedQuantity
            );


            buyer.addShares(
                    event.getId(),
                    optionIndex,
                    tradedQuantity,
                    event.getOptions().size()
            );


            /*
             * Reduce both orders.
             */
            buyOrder.reduceQuantity(
                    tradedQuantity
            );


            sellOrder.reduceQuantity(
                    tradedQuantity
            );


            /*
             * Remove completed orders.
             */
            if (buyOrder.isCompleted()) {

                orderBook.removeBuyOrder(
                        buyOrder
                );
            }


            if (sellOrder.isCompleted()) {

                orderBook.removeSellOrder(
                        sellOrder
                );
            }


            /*
             * The new order is finished.
             */
            if (newOrder.isCompleted()) {

                break;
            }
        }
    }


    /*
     * MINT processing.
     */
    private void processMint(
            MarketEvent event,
            int newOptionIndex,
            Order newOrder) {

        if (!event.isAllowMint()) {
            return;
        }


        int oppositeOptionIndex;


        if (newOptionIndex == 0) {

            oppositeOptionIndex = 1;

        } else {

            oppositeOptionIndex = 0;
        }


        OrderBook newOrderBook =
                event.getOrderBook(
                        newOptionIndex
                );


        OrderBook oppositeOrderBook =
                event.getOrderBook(
                        oppositeOptionIndex
                );


        while (!newOrder.isCompleted()
                &&
                oppositeOrderBook
                        .hasBuyOrders()) {


            Order waitingOrder =
                    oppositeOrderBook
                            .getHighestBuyOrder();


            if (waitingOrder == null) {
                break;
            }


            double pricesSum =
                    newOrder.getPrice()
                            + waitingOrder.getPrice();


            /*
             * MINT requires the prices
             * to reach at least d.
             */
            if (pricesSum
                    < event.getOrderBookD()) {

                break;
            }


            /*
             * If quantities are different,
             * use the smaller quantity.
             */
            int mintQuantity =
                    Math.min(
                            newOrder
                                    .getRemainingQuantity(),

                            waitingOrder
                                    .getRemainingQuantity()
                    );


            /*
             * Waiting order keeps
             * its original price.
             */
            double waitingPrice =
                    waitingOrder.getPrice();


            /*
             * New order pays the
             * complementary price to d.
             */
            double newPrice =
                    event.getOrderBookD()
                            - waitingPrice;


            double waitingPayment =
                    mintQuantity
                            * waitingPrice;


            double newPayment =
                    mintQuantity
                            * newPrice;


            MarketUser waitingUser =
                    users.get(
                            waitingOrder
                                    .getUserName()
                    );


            MarketUser newUser =
                    users.get(
                            newOrder
                                    .getUserName()
                    );


            if (waitingUser == null
                    ||
                    newUser == null) {

                throw new IllegalStateException(
                        "A user in the Order Book does not exist."
                );
            }


            /*
             * Calculate ON_PURCHASE commission
             * separately for each buyer.
             */
            double waitingCommission =
                    0;


            double newCommission =
                    0;


            if (event.getCommissionType()
                    == CommissionType.ON_PURCHASE) {

                waitingCommission =
                        waitingPayment
                                * event
                                .getCommissionPercent()
                                / 100.0;


                newCommission =
                        newPayment
                                * event
                                .getCommissionPercent()
                                / 100.0;
            }


            MarketUser marketMaker =
                    findMarketMaker(
                            event.getId()
                    );


            /*
             * Each buyer pays:
             * purchase amount + commission.
             */
            waitingUser.payForTrade(
                    waitingPayment
                            + waitingCommission
            );


            if (waitingCommission > 0) {

                waitingUser.addCommissionPaid(
                        waitingCommission,
                        event.getId()
                );
            }


            newUser.payForTrade(
                    newPayment
                            + newCommission
            );


            if (newCommission > 0) {

                newUser.addCommissionPaid(
                        newCommission,
                        event.getId()
                );
            }


            waitingUser.addObAmountPaid(
                    event.getId(),
                    oppositeOptionIndex,
                    waitingPayment,
                    event.getOptions().size()
            );


            newUser.addObAmountPaid(
                    event.getId(),
                    newOptionIndex,
                    newPayment,
                    event.getOptions().size()
            );


            /*
             * MINT creates new shares.
             */
            waitingUser.addShares(
                    event.getId(),
                    oppositeOptionIndex,
                    mintQuantity,
                    event.getOptions().size()
            );


            newUser.addShares(
                    event.getId(),
                    newOptionIndex,
                    mintQuantity,
                    event.getOptions().size()
            );


            /*
             * Only the actual MINT purchase money
             * enters the event account.
             *
             * Commission does NOT enter
             * the event account.
             */
            event.addToAccountBalance(
                    waitingPayment
                            + newPayment
            );


            /*
             * Commission goes to the MM.
             */
            double totalCommission =
                    waitingCommission
                            + newCommission;


            if (totalCommission > 0) {

                marketMaker.deposit(
                        totalCommission
                );


                event.addCollectedCommission(
                        totalCommission
                );
            }


            /*
             * Reduce both BUY orders.
             */
            waitingOrder.reduceQuantity(
                    mintQuantity
            );


            newOrder.reduceQuantity(
                    mintQuantity
            );


            /*
             * Remove completed waiting order.
             */
            if (waitingOrder.isCompleted()) {

                oppositeOrderBook
                        .removeBuyOrder(
                                waitingOrder
                        );
            }


            /*
             * Remove completed new order.
             */
            if (newOrder.isCompleted()) {

                newOrderBook
                        .removeBuyOrder(
                                newOrder
                        );
            }
        }
    }


    /*
     * Find MM of one event.
     */
    private MarketUser findMarketMaker(
            int eventId) {

        for (MarketUser user :
                users.values()) {

            if (user.isMarketMakerOf(
                    eventId
            )) {

                return user;
            }
        }


        throw new IllegalStateException(
                "No Market Maker was found for event "
                        + eventId
                        + "."
        );
    }


    @Override
    public List<PendingOrderInfo> getPendingOrders(
            int eventId,
            int optionIndex) {

        MarketEvent event =
                requireEvent(
                        eventId
                );


        if (event.getMarketMethodType()
                != MarketMethodType.ORDER_BOOK) {

            throw new IllegalStateException(
                    "The selected event is not an Order Book event."
            );
        }


        if (optionIndex < 0
                ||
                optionIndex >=
                        event
                                .getOptions()
                                .size()) {

            throw new IllegalArgumentException(
                    "Invalid option selection."
            );
        }


        List<PendingOrderInfo> result =
                new ArrayList<>();


        OrderBook orderBook =
                event.getOrderBook(
                        optionIndex
                );


        for (Order order :
                orderBook.getBuyOrders()) {

            result.add(
                    new PendingOrderInfo(
                            order.getUserName(),
                            order.getType(),
                            order.getRemainingQuantity(),
                            order.getPrice()
                    )
            );
        }


        for (Order order :
                orderBook.getSellOrders()) {

            result.add(
                    new PendingOrderInfo(
                            order.getUserName(),
                            order.getType(),
                            order.getRemainingQuantity(),
                            order.getPrice()
                    )
            );
        }


        return result;
    }


    @Override
    public OrderBookStatistics getOrderBookStatistics(
            int eventId,
            int optionIndex) {

        MarketEvent event =
                requireEvent(
                        eventId
                );


        if (event.getMarketMethodType()
                != MarketMethodType.ORDER_BOOK) {

            throw new IllegalStateException(
                    "The selected event is not an Order Book event."
            );
        }


        if (optionIndex < 0
                ||
                optionIndex >=
                        event
                                .getOptions()
                                .size()) {

            throw new IllegalArgumentException(
                    "Invalid option selection."
            );
        }


        OrderBook orderBook =
                event.getOrderBook(
                        optionIndex
                );


        return new OrderBookStatistics(
                orderBook.getLastPrice(),
                orderBook.getHighestBid(),
                orderBook.getLowestAsk(),
                orderBook.getMidPrice(),
                orderBook.getSpread()
        );
    }


    @Override
    public EventDetails getEventDetails(
            int eventId) {

        return toDetails(
                requireEvent(
                        eventId
                )
        );
    }


    @Override
    public PurchaseResult purchaseShares(
            int eventId,
            int optionIndex,
            int quantity) {

        return requireEvent(
                eventId
        ).purchaseShares(
                optionIndex,
                quantity
        );
    }


    /*
     * LMSR purchase by a named user.
     *
     * The buyer pays from their own
     * balance. The share price goes to
     * the event account. ON_PURCHASE
     * commission goes to the Market Maker.
     */
    @Override
    public PurchaseResult purchaseShares(
            String userName,
            int eventId,
            int optionIndex,
            int quantity) {

        MarketUser user =
                users.get(
                        userName
                );


        if (user == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + userName
                            + " exists."
            );
        }


        if (user.isBlocked()) {

            throw new IllegalStateException(
                    "This user is blocked."
            );
        }


        MarketEvent event =
                requireEvent(
                        eventId
                );


        if (event.getMarketMethodType()
                != MarketMethodType.LMSR) {

            throw new IllegalStateException(
                    "This operation is available only for LMSR events."
            );
        }


        PurchaseResult result =
                event.purchaseShares(
                        optionIndex,
                        quantity
                );


        user.payForTrade(
                result.totalPaid()
        );


        if (result.commission()
                > 0) {

            user.addCommissionPaid(
                    result.commission(),
                    event.getId()
            );
        }


        user.addShares(
                event.getId(),
                optionIndex,
                quantity,
                event.getOptions().size()
        );


        if (result.commission()
                > 0) {

            MarketUser marketMaker =
                    findMarketMaker(
                            event.getId()
                    );


            event.removeFromAccountBalance(
                    result.commission()
            );


            marketMaker.deposit(
                    result.commission()
            );
        }


        user.addParticipatedEvent(
                event.getId()
        );


        String optionName =
                event.getOptions()
                        .get(
                                optionIndex
                        )
                        .getName();


        user.addLmsrTrade(
                new UserLmsrTrade(
                        event.getId(),
                        optionName,
                        quantity,
                        result.sharesPrice(),
                        result.commission()
                )
        );


        return result;
    }


    @Override
    public CloseResult closeEvent(
            int eventId,
            int winningOptionIndex) {

        return requireEvent(
                eventId
        ).close(
                winningOptionIndex
        );
    }


    private MarketEvent requireEvent(
            int eventId) {

        MarketEvent event =
                events.get(
                        eventId
                );


        if (event == null) {

            throw new IllegalArgumentException(
                    "No event with id "
                            + eventId
                            + " exists."
            );
        }


        return event;
    }

    @Override
    public CloseResult closeOrderBookEvent(
            String userName,
            int eventId,
            int winningOptionIndex) {

        MarketUser closingUser =
                users.get(
                        userName
                );


        if (closingUser == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + userName
                            + " exists."
            );
        }


        /*
         * A blocked user cannot perform actions.
         */
        if (closingUser.isBlocked()) {

            throw new IllegalStateException(
                    "This user is blocked."
            );
        }


        MarketEvent event =
                requireEvent(
                        eventId
                );


        /*
         * This method is only for
         * Order Book events.
         */
        if (event.getMarketMethodType()
                != MarketMethodType.ORDER_BOOK) {

            throw new IllegalStateException(
                    "The selected event is not an Order Book event."
            );
        }


        /*
         * Only an ACTIVE event can close.
         */
        if (event.getStatus()
                != EventStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Only an active event can be closed."
            );
        }


        /*
         * Only the Market Maker
         * may close the event.
         */
        if (!closingUser.isMarketMakerOf(
                eventId
        )) {

            throw new IllegalStateException(
                    "Only the Market Maker can close this event."
            );
        }


        /*
         * Validate winning option.
         */
        if (winningOptionIndex < 0
                ||
                winningOptionIndex
                        >= event
                        .getOptions()
                        .size()) {

            throw new IllegalArgumentException(
                    "Invalid winning option."
            );
        }


        MarketUser marketMaker =
                findMarketMaker(
                        eventId
                );


        /*
         * Go through every user.
         */
        for (MarketUser user :
                users.values()) {


            int winningShares =
                    user.getShares(
                            eventId,
                            winningOptionIndex
                    );


            /*
             * User did not hold
             * winning shares.
             */
            if (winningShares == 0) {

                continue;
            }


            /*
             * In Order Book:
             *
             * one winning share pays d.
             */
            double grossPayment =
                    winningShares
                            * event.getOrderBookD();


            double commission =
                    0;


            /*
             * ON_CLOSE commission is taken
             * from the winning payout.
             */
            if (event.getCommissionType()
                    == CommissionType.ON_CLOSE) {

                commission =
                        grossPayment
                                * event
                                .getCommissionPercent()
                                / 100.0;
            }


            double netPayment =
                    grossPayment
                            - commission;


            /*
             * Winner receives the amount
             * after commission.
             */
            user.deposit(
                    netPayment
            );


            user.addCloseGrossPayout(
                    eventId,
                    grossPayment
            );


            /*
             * Commission goes to MM.
             */
            if (commission > 0) {

                user.addCommissionPaid(
                        commission,
                        eventId
                );


                marketMaker.deposit(
                        commission
                );


                event.addCollectedCommission(
                        commission
                );
            }


            /*
             * The event account loses the
             * whole gross payout:
             *
             * net payment + commission.
             */
            event.removeFromAccountBalance(
                    grossPayment
            );
        }


        /*
         * All winning holders were paid.
         * Now close the event.
         */
        event.markAsClosed(
                winningOptionIndex
        );


        return new CloseResult(
                event
                        .getOptions()
                        .get(
                                winningOptionIndex
                        )
                        .getName()
        );
    }


    /*
     * LMSR close by the Market Maker.
     *
     * Each user is paid $1 per winning
     * share. ON_CLOSE commission is taken
     * from that payout and given to the
     * Market Maker. The remaining event
     * account is returned to the Market Maker.
     */
    @Override
    public CloseResult closeLmsrEvent(
            String marketMakerName,
            int eventId,
            int winningOptionIndex) {

        MarketUser closingUser =
                users.get(
                        marketMakerName
                );


        if (closingUser == null) {

            throw new IllegalArgumentException(
                    "No user with name "
                            + marketMakerName
                            + " exists."
            );
        }


        if (closingUser.isBlocked()) {

            throw new IllegalStateException(
                    "This user is blocked."
            );
        }


        MarketEvent event =
                requireEvent(
                        eventId
                );


        if (event.getMarketMethodType()
                != MarketMethodType.LMSR) {

            throw new IllegalStateException(
                    "The selected event is not an LMSR event."
            );
        }


        if (event.getStatus()
                != EventStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Only an active event can be closed."
            );
        }


        if (!closingUser.isMarketMakerOf(
                eventId
        )) {

            throw new IllegalStateException(
                    "Only the Market Maker can close this event."
            );
        }


        if (winningOptionIndex < 0
                ||
                winningOptionIndex
                        >= event
                        .getOptions()
                        .size()) {

            throw new IllegalArgumentException(
                    "Invalid winning option."
            );
        }


        MarketUser marketMaker =
                findMarketMaker(
                        eventId
                );


        double totalGrossPayout =
                0;


        for (MarketUser user :
                users.values()) {

            int winningShares =
                    user.getShares(
                            eventId,
                            winningOptionIndex
                    );


            if (winningShares == 0) {

                continue;
            }


            double grossPayment =
                    winningShares;


            double commission =
                    0;


            if (event.getCommissionType()
                    == CommissionType.ON_CLOSE) {

                commission =
                        grossPayment
                                * event
                                .getCommissionPercent()
                                / 100.0;
            }


            double netPayment =
                    grossPayment
                            - commission;


            user.deposit(
                    netPayment
            );


            if (commission > 0) {

                user.addCommissionPaid(
                        commission,
                        eventId
                );


                marketMaker.deposit(
                        commission
                );


                event.addCollectedCommission(
                        commission
                );
            }


            totalGrossPayout +=
                    grossPayment;
        }


        double accountBefore =
                event.getAccountBalance();


        if (accountBefore > 0) {

            event.removeFromAccountBalance(
                    accountBefore
            );
        }


        double leftover =
                accountBefore
                        - totalGrossPayout;


        if (leftover > 0) {

            marketMaker.deposit(
                    leftover
            );

        } else if (leftover < 0) {

            marketMaker.payForTrade(
                    -leftover
            );
        }


        event.markAsClosed(
                winningOptionIndex
        );


        return new CloseResult(
                event
                        .getOptions()
                        .get(
                                winningOptionIndex
                        )
                        .getName()
        );
    }

    private EventSummary toSummary(
            MarketEvent event) {

        List<String> optionNames =
                new ArrayList<>();


        for (MarketOption option :
                event.getOptions()) {

            optionNames.add(
                    option.getName()
            );
        }


        return new EventSummary(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getCommissionPercent(),
                event.getCommissionType(),
                optionNames,
                event.getStatus(),
                event.getMarketMethodType(),
                event.getLiquidityParameter(),
                event.getOrderBookInitial(),
                event.getOrderBookD(),
                event.isAllowMint()
        );
    }


    private EventDetails toDetails(
            MarketEvent event) {

        if (event.getMarketMethodType()
                == MarketMethodType.ORDER_BOOK) {

            throw new IllegalStateException(
                    "Order Book event details are not implemented yet."
            );
        }


        List<OptionState> optionStates =
                new ArrayList<>();


        for (int optionIndex = 0;
             optionIndex
                     < event
                     .getOptions()
                     .size();
             optionIndex++) {

            MarketOption option =
                    event
                            .getOptions()
                            .get(
                                    optionIndex
                            );


            optionStates.add(
                    new OptionState(
                            option.getName(),
                            event.getOptionPrice(
                                    optionIndex
                            ),
                            option.getPurchasedShares()
                    )
            );
        }


        List<TradeView> tradeViews =
                new ArrayList<>();


        for (guessmarket.engine.model.Trade trade :
                event
                        .getTradeHistoryNewestFirst()) {

            tradeViews.add(
                    new TradeView(
                            trade.optionName(),
                            trade.quantity(),
                            trade.pricePaid()
                    )
            );
        }


        String winningOption =
                null;


        if (event.getWinningOptionIndex()
                != null) {

            winningOption =
                    event
                            .getOptions()
                            .get(
                                    event
                                            .getWinningOptionIndex()
                            )
                            .getName();
        }


        return new EventDetails(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getCommissionPercent(),
                event.getCommissionType(),
                event.getStatus(),
                optionStates,
                event.getAccountBalance(),
                event.getCollectedCommission(),
                tradeViews,
                winningOption
        );
    }


    private String readableMessage(
            Throwable throwable) {

        String message =
                throwable.getMessage();


        if (message == null
                ||
                message.isBlank()) {

            return throwable
                    .getClass()
                    .getSimpleName();
        }


        return message;
    }
}