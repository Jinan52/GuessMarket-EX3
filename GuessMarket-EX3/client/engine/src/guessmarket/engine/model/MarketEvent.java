package guessmarket.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MarketEvent {

    private static final int NUMBER_OF_OPTIONS = 2;

    private final int id;
    private final String name;
    private final String description;
    private final int commissionPercent;
    private final CommissionType commissionType;

    private final List<MarketOption> options;
    private final List<Trade> tradeHistory;

    /*
     * For Order Book events, every option has
     * its own OrderBook.
     *
     * LMSR events keep this list empty.
     */
    private final List<OrderBook> orderBooks;

    private final MarketMethodType marketMethodType;

    /*
     * LMSR parameter.
     */
    private double liquidityParameter;

    /*
     * Order Book parameters.
     */
    private int orderBookInitial;
    private int orderBookD;
    private boolean allowMint;

    private EventStatus status;
    private double accountBalance;
    private double collectedCommission;
    private Integer winningOptionIndex;


    /*
     * Constructor for LMSR.
     */
    public MarketEvent(
            int id,
            String name,
            String description,
            int commissionPercent,
            CommissionType commissionType,
            List<String> optionNames,
            double liquidityParameter) {

        if (optionNames.size() != NUMBER_OF_OPTIONS) {

            throw new IllegalArgumentException(
                    "An event must have exactly two options."
            );
        }


        if (liquidityParameter <= 0) {

            throw new IllegalArgumentException(
                    "The LMSR b value must be greater than zero."
            );
        }


        this.id = id;
        this.name = name;
        this.description = description;
        this.commissionPercent = commissionPercent;
        this.commissionType = commissionType;


        this.options =
                new ArrayList<>();


        for (String optionName :
                optionNames) {

            this.options.add(
                    new MarketOption(
                            optionName
                    )
            );
        }


        this.tradeHistory =
                new ArrayList<>();


        this.orderBooks =
                new ArrayList<>();


        this.marketMethodType =
                MarketMethodType.LMSR;


        this.liquidityParameter =
                liquidityParameter;


        this.orderBookInitial = 0;
        this.orderBookD = 0;
        this.allowMint = false;


        this.status =
                EventStatus.NOT_STARTED;


        this.accountBalance = 0;

        this.collectedCommission = 0;

        this.winningOptionIndex = null;
    }


    /*
     * Constructor for Order Book.
     */
    public MarketEvent(
            int id,
            String name,
            String description,
            int commissionPercent,
            CommissionType commissionType,
            List<String> optionNames,
            int initial,
            int d,
            boolean allowMint) {

        if (optionNames.size() != NUMBER_OF_OPTIONS) {

            throw new IllegalArgumentException(
                    "An event must have exactly two options."
            );
        }


        if (initial < 0) {

            throw new IllegalArgumentException(
                    "Order Book initial value cannot be negative."
            );
        }


        if (d <= 0) {

            throw new IllegalArgumentException(
                    "Order Book d value must be greater than zero."
            );
        }


        this.id = id;
        this.name = name;
        this.description = description;
        this.commissionPercent = commissionPercent;
        this.commissionType = commissionType;


        this.options =
                new ArrayList<>();


        for (String optionName :
                optionNames) {

            this.options.add(
                    new MarketOption(
                            optionName
                    )
            );
        }


        this.tradeHistory =
                new ArrayList<>();


        /*
         * One OrderBook for every option.
         */
        this.orderBooks =
                new ArrayList<>();


        for (int i = 0;
             i < optionNames.size();
             i++) {

            this.orderBooks.add(
                    new OrderBook()
            );
        }


        this.marketMethodType =
                MarketMethodType.ORDER_BOOK;


        this.liquidityParameter = 0;


        this.orderBookInitial =
                initial;

        this.orderBookD =
                d;

        this.allowMint =
                allowMint;


        this.status =
                EventStatus.NOT_STARTED;


        this.accountBalance = 0;

        this.collectedCommission = 0;

        this.winningOptionIndex = null;
    }


    public int getId() {

        return id;
    }


    public String getName() {

        return name;
    }


    public String getDescription() {

        return description;
    }


    public int getCommissionPercent() {

        return commissionPercent;
    }


    public CommissionType getCommissionType() {

        return commissionType;
    }


    public List<MarketOption> getOptions() {

        return Collections.unmodifiableList(
                options
        );
    }


    public MarketMethodType getMarketMethodType() {

        return marketMethodType;
    }


    public double getLiquidityParameter() {

        return liquidityParameter;
    }


    public int getOrderBookInitial() {

        return orderBookInitial;
    }


    public int getOrderBookD() {

        return orderBookD;
    }


    public boolean isAllowMint() {

        return allowMint;
    }


    public EventStatus getStatus() {

        return status;
    }


    public double getAccountBalance() {

        return accountBalance;
    }


    /*
     * Used when MINT money enters
     * the event account.
     */
    public void addToAccountBalance(
            double amount) {

        if (amount < 0) {

            throw new IllegalArgumentException(
                    "Amount cannot be negative."
            );
        }


        accountBalance +=
                amount;
    }

    public void removeFromAccountBalance(
            double amount) {

        if (amount < 0) {

            throw new IllegalArgumentException(
                    "Amount cannot be negative."
            );
        }


        if (amount > accountBalance) {

            throw new IllegalStateException(
                    "The event account does not contain enough money."
            );
        }


        accountBalance -= amount;
    }


    public void markAsClosed(
            int winningOptionIndex) {

        if (status != EventStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Only an active event can be closed."
            );
        }


        validateOptionIndex(
                winningOptionIndex
        );


        this.winningOptionIndex =
                winningOptionIndex;


        this.status =
                EventStatus.CLOSED;
    }

    public double getCollectedCommission() {

        return collectedCommission;
    }


    /*
     * Used when commission is collected.
     */
    public void addCollectedCommission(
            double amount) {

        if (amount < 0) {

            throw new IllegalArgumentException(
                    "Commission amount cannot be negative."
            );
        }


        collectedCommission +=
                amount;
    }


    public Integer getWinningOptionIndex() {

        return winningOptionIndex;
    }


    /*
     * Returns the Order Book
     * of one option.
     */
    public OrderBook getOrderBook(
            int optionIndex) {

        if (marketMethodType
                != MarketMethodType.ORDER_BOOK) {

            throw new IllegalStateException(
                    "This event does not use Order Book."
            );
        }


        validateOptionIndex(
                optionIndex
        );


        return orderBooks.get(
                optionIndex
        );
    }


    public List<OrderBook> getOrderBooks() {

        if (marketMethodType
                != MarketMethodType.ORDER_BOOK) {

            throw new IllegalStateException(
                    "This event does not use Order Book."
            );
        }


        return Collections.unmodifiableList(
                orderBooks
        );
    }


    public List<Trade> getTradeHistoryNewestFirst() {

        List<Trade> newestFirst =
                new ArrayList<>(
                        tradeHistory
                );


        Collections.reverse(
                newestFirst
        );


        return Collections.unmodifiableList(
                newestFirst
        );
    }


    /*
     * LMSR price.
     */
    public double getOptionPrice(
            int optionIndex) {

        if (marketMethodType
                != MarketMethodType.LMSR) {

            throw new IllegalStateException(
                    "Option price using LMSR is not available for an Order Book event."
            );
        }


        validateOptionIndex(
                optionIndex
        );


        double firstExponent =
                options.get(0)
                        .getPurchasedShares()
                        / liquidityParameter;


        double secondExponent =
                options.get(1)
                        .getPurchasedShares()
                        / liquidityParameter;


        double maximumExponent =
                Math.max(
                        firstExponent,
                        secondExponent
                );


        double firstWeight =
                Math.exp(
                        firstExponent
                                - maximumExponent
                );


        double secondWeight =
                Math.exp(
                        secondExponent
                                - maximumExponent
                );


        double weightsSum =
                firstWeight
                        + secondWeight;


        if (optionIndex == 0) {

            return firstWeight
                    / weightsSum;
        }


        return secondWeight
                / weightsSum;
    }


    /*
     * LMSR purchase.
     */
    public PurchaseResult purchaseShares(
            int optionIndex,
            int quantity) {

        if (marketMethodType
                != MarketMethodType.LMSR) {

            throw new IllegalStateException(
                    "This operation is available only for LMSR events."
            );
        }


        if (status
                != EventStatus.ACTIVE) {

            throw new IllegalStateException(
                    "The selected event is not active."
            );
        }


        validateOptionIndex(
                optionIndex
        );


        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "The share quantity must be a positive whole number."
            );
        }


        int firstSharesBefore =
                options.get(0)
                        .getPurchasedShares();


        int secondSharesBefore =
                options.get(1)
                        .getPurchasedShares();


        int firstSharesAfter =
                firstSharesBefore;


        int secondSharesAfter =
                secondSharesBefore;


        if (optionIndex == 0) {

            firstSharesAfter =
                    Math.addExact(
                            firstSharesBefore,
                            quantity
                    );

        } else {

            secondSharesAfter =
                    Math.addExact(
                            secondSharesBefore,
                            quantity
                    );
        }


        double sharesPrice =
                calculateCost(
                        firstSharesAfter,
                        secondSharesAfter
                )
                        -
                        calculateCost(
                                firstSharesBefore,
                                secondSharesBefore
                        );


        double commission =
                0;


        if (commissionType
                == CommissionType.ON_PURCHASE) {

            commission =
                    sharesPrice
                            * commissionPercent
                            / 100.0;
        }


        double totalPaid =
                sharesPrice
                        + commission;


        options.get(
                optionIndex
        ).addShares(
                quantity
        );


        accountBalance +=
                totalPaid;


        collectedCommission +=
                commission;


        tradeHistory.add(
                new Trade(
                        options.get(
                                optionIndex
                        ).getName(),
                        quantity,
                        sharesPrice
                )
        );


        return new PurchaseResult(
                sharesPrice,
                commission,
                totalPaid
        );
    }


    public CloseResult close(
            int winningOptionIndex) {

        if (status
                != EventStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Only an active event can be closed."
            );
        }


        validateOptionIndex(
                winningOptionIndex
        );


        int winningShares =
                options.get(
                        winningOptionIndex
                ).getPurchasedShares();


        double grossWinnersPayment =
                winningShares;


        double closeCommission =
                0;


        if (commissionType
                == CommissionType.ON_CLOSE) {

            closeCommission =
                    grossWinnersPayment
                            * commissionPercent
                            / 100.0;
        }


        double netWinnersPayment =
                grossWinnersPayment
                        - closeCommission;


        collectedCommission +=
                closeCommission;


        accountBalance -=
                netWinnersPayment;


        status =
                EventStatus.CLOSED;


        this.winningOptionIndex =
                winningOptionIndex;


        return new CloseResult(
                options.get(
                        winningOptionIndex
                ).getName()
        );
    }


    public double getRequiredOpeningAmount() {

        if (marketMethodType
                == MarketMethodType.LMSR) {

            return calculateInitialSubsidy();
        }


        return orderBookInitial;
    }


    public void open() {

        if (status
                != EventStatus.NOT_STARTED) {

            throw new IllegalStateException(
                    "The event cannot be opened."
            );
        }


        accountBalance =
                getRequiredOpeningAmount();


        status =
                EventStatus.ACTIVE;
    }


    private double calculateInitialSubsidy() {

        return liquidityParameter
                * Math.log(
                NUMBER_OF_OPTIONS
        );
    }


    private double calculateCost(
            int firstShares,
            int secondShares) {

        double firstExponent =
                firstShares
                        / liquidityParameter;


        double secondExponent =
                secondShares
                        / liquidityParameter;


        double maximumExponent =
                Math.max(
                        firstExponent,
                        secondExponent
                );


        return liquidityParameter
                * (
                maximumExponent
                        +
                        Math.log(
                                Math.exp(
                                        firstExponent
                                                - maximumExponent
                                )
                                        +
                                        Math.exp(
                                                secondExponent
                                                        - maximumExponent
                                        )
                        )
        );
    }


    private void validateOptionIndex(
            int optionIndex) {

        if (optionIndex < 0
                ||
                optionIndex >=
                        options.size()) {

            throw new IllegalArgumentException(
                    "Invalid option selection."
            );
        }
    }
}