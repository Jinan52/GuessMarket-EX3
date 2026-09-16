package guessmarket.engine.model;

import guessmarket.engine.dto.AccountHistoryRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MarketUser {

    private final String name;

    private double balance;

    private boolean blocked;

    private final List<Integer> marketMakerEventIds;

    private final Map<Integer, List<Integer>> holdings;

    private final List<Integer> participatedEventIds;

    private final List<UserLmsrTrade> lmsrTrades;

    private double totalCommissionPaid;

    private final Map<Integer, List<Double>> obAmountPaidPerOption;

    private final Map<Integer, Double> obSellProceeds;

    private final Map<Integer, Double> commissionPaidPerEvent;

    private final Map<Integer, Double> closeGrossPayoutPerEvent;

    private final List<AccountHistoryRow> accountHistory;


    public MarketUser(
            String name,
            double initialCash) {

        if (name == null ||
                name.isEmpty()) {

            throw new IllegalArgumentException(
                    "User name cannot be empty."
            );
        }


        if (initialCash < 0) {

            throw new IllegalArgumentException(
                    "Initial cash cannot be negative."
            );
        }


        this.name = name;
        this.balance = initialCash;

        this.blocked = false;

        this.marketMakerEventIds =
                new ArrayList<>();

        this.holdings =
                new LinkedHashMap<>();

        this.participatedEventIds =
                new ArrayList<>();

        this.lmsrTrades =
                new ArrayList<>();

        this.totalCommissionPaid =
                0;

        this.obAmountPaidPerOption =
                new LinkedHashMap<>();

        this.obSellProceeds =
                new LinkedHashMap<>();

        this.commissionPaidPerEvent =
                new LinkedHashMap<>();

        this.closeGrossPayoutPerEvent =
                new LinkedHashMap<>();

        this.accountHistory =
                new ArrayList<>();
    }


    public String getName() {

        return name;
    }


    public double getBalance() {

        return balance;
    }


    public boolean isBlocked() {

        return blocked;
    }


    public List<Integer> getMarketMakerEventIds() {

        return Collections.unmodifiableList(
                marketMakerEventIds
        );
    }


    public void addMarketMakerEvent(
            int eventId) {

        marketMakerEventIds.add(
                eventId
        );
    }


    public boolean isMarketMakerOf(
            int eventId) {

        return marketMakerEventIds.contains(
                eventId
        );
    }


    /*
     * Remembers that this user took part
     * in an event. The same event id is
     * stored only once.
     */
    public void addParticipatedEvent(
            int eventId) {

        if (participatedEventIds.contains(
                eventId
        )) {

            return;
        }


        participatedEventIds.add(
                eventId
        );
    }


    public List<Integer> getParticipatedEventIds() {

        return Collections.unmodifiableList(
                participatedEventIds
        );
    }


    public void addLmsrTrade(
            UserLmsrTrade trade) {

        lmsrTrades.add(
                trade
        );
    }


    public List<UserLmsrTrade> getLmsrTrades() {

        return Collections.unmodifiableList(
                lmsrTrades
        );
    }


    public void addCommissionPaid(
            double amount) {

        if (amount < 0) {

            throw new IllegalArgumentException(
                    "Amount cannot be negative."
            );
        }


        totalCommissionPaid +=
                amount;
    }


    public double getTotalCommissionPaid() {

        return totalCommissionPaid;
    }


    public void addCommissionPaid(
            double amount,
            int eventId) {

        addCommissionPaid(
                amount
        );


        Double current =
                commissionPaidPerEvent.get(
                        eventId
                );


        if (current == null) {

            current =
                    0.0;
        }


        commissionPaidPerEvent.put(
                eventId,
                current + amount
        );
    }


    public double getCommissionPaidForEvent(
            int eventId) {

        Double current =
                commissionPaidPerEvent.get(
                        eventId
                );


        if (current == null) {

            return 0;
        }


        return current;
    }


    private void ensureObAmountPaid(
            int eventId,
            int numberOfOptions) {

        if (!obAmountPaidPerOption.containsKey(
                eventId
        )) {

            List<Double> amounts =
                    new ArrayList<>();


            for (int i = 0;
                 i < numberOfOptions;
                 i++) {

                amounts.add(
                        0.0
                );
            }


            obAmountPaidPerOption.put(
                    eventId,
                    amounts
            );
        }
    }


    public void addObAmountPaid(
            int eventId,
            int optionIndex,
            double amount,
            int numberOfOptions) {

        if (amount < 0) {

            throw new IllegalArgumentException(
                    "Amount cannot be negative."
            );
        }


        ensureObAmountPaid(
                eventId,
                numberOfOptions
        );


        List<Double> amounts =
                obAmountPaidPerOption.get(
                        eventId
                );


        if (optionIndex < 0 ||
                optionIndex >=
                        amounts.size()) {

            throw new IllegalArgumentException(
                    "Invalid option selection."
            );
        }


        double current =
                amounts.get(
                        optionIndex
                );


        amounts.set(
                optionIndex,
                current + amount
        );
    }


    public List<Double> getObAmountPaidPerOption(
            int eventId,
            int numberOfOptions) {

        List<Double> stored =
                obAmountPaidPerOption.get(
                        eventId
                );


        List<Double> result =
                new ArrayList<>();


        for (int i = 0;
             i < numberOfOptions;
             i++) {

            if (stored == null) {

                result.add(
                        0.0
                );

            } else {

                result.add(
                        stored.get(
                                i
                        )
                );
            }
        }


        return result;
    }


    public void addObSellProceeds(
            int eventId,
            double amount) {

        if (amount < 0) {

            throw new IllegalArgumentException(
                    "Amount cannot be negative."
            );
        }


        Double current =
                obSellProceeds.get(
                        eventId
                );


        if (current == null) {

            current =
                    0.0;
        }


        obSellProceeds.put(
                eventId,
                current + amount
        );
    }


    public double getObSellProceeds(
            int eventId) {

        Double current =
                obSellProceeds.get(
                        eventId
                );


        if (current == null) {

            return 0;
        }


        return current;
    }


    public void addCloseGrossPayout(
            int eventId,
            double amount) {

        if (amount < 0) {

            throw new IllegalArgumentException(
                    "Amount cannot be negative."
            );
        }


        Double current =
                closeGrossPayoutPerEvent.get(
                        eventId
                );


        if (current == null) {

            current =
                    0.0;
        }


        closeGrossPayoutPerEvent.put(
                eventId,
                current + amount
        );
    }


    public double getCloseGrossPayout(
            int eventId) {

        Double current =
                closeGrossPayoutPerEvent.get(
                        eventId
                );


        if (current == null) {

            return 0;
        }


        return current;
    }


    public double getOrderBookProfitLoss(
            int eventId) {

        double amountPaidSum =
                0;


        List<Double> amounts =
                obAmountPaidPerOption.get(
                        eventId
                );


        if (amounts != null) {

            for (int i = 0;
                 i < amounts.size();
                 i++) {

                amountPaidSum +=
                        amounts.get(
                                i
                        );
            }
        }


        return getObSellProceeds(
                eventId
        )
                + getCloseGrossPayout(
                eventId
        )
                - amountPaidSum
                - getCommissionPaidForEvent(
                eventId
        );
    }


    /*
     * Used when opening an event.
     *
     * The Market Maker must already have
     * enough money to open the event.
     */
    public void withdraw(
            double amount) {

        if (amount < 0) {

            throw new IllegalArgumentException(
                    "Amount cannot be negative."
            );
        }


        if (balance < amount) {

            throw new IllegalStateException(
                    "The user does not have enough money."
            );
        }


        balance -= amount;


        addHistoryRow(
                "Account withdrawal",
                -amount
        );
    }


    /*
     * Used for trading.
     *
     * According to the assignment, a trade
     * that causes a negative balance is
     * completed, and afterwards the user
     * becomes blocked.
     */
    public void payForTrade(
            double amount) {

        if (amount < 0) {

            throw new IllegalArgumentException(
                    "Amount cannot be negative."
            );
        }


        if (blocked) {

            throw new IllegalStateException(
                    "This user is blocked."
            );
        }


        balance -= amount;


        if (balance < 0) {

            blocked = true;
        }


        addHistoryRow(
                "Trade payment",
                -amount
        );
    }


    public void deposit(
            double amount) {

        deposit(
                amount,
                "Account deposit"
        );
    }


    public void deposit(
            double amount,
            String description) {

        if (amount < 0) {

            throw new IllegalArgumentException(
                    "Amount cannot be negative."
            );
        }


        balance += amount;


        addHistoryRow(
                description,
                amount
        );
    }


    public List<AccountHistoryRow> getAccountHistory() {

        return List.copyOf(
                accountHistory
        );
    }


    private void addHistoryRow(
            String description,
            double signedAmount) {

        if (signedAmount == 0) {

            return;
        }


        accountHistory.add(
                new AccountHistoryRow(
                        description,
                        signedAmount,
                        balance
                )
        );
    }


    private void ensureEventHoldings(
            int eventId,
            int numberOfOptions) {

        if (!holdings.containsKey(
                eventId
        )) {

            List<Integer> eventHoldings =
                    new ArrayList<>();


            for (int i = 0;
                 i < numberOfOptions;
                 i++) {

                eventHoldings.add(
                        0
                );
            }


            holdings.put(
                    eventId,
                    eventHoldings
            );
        }
    }


    public void addShares(
            int eventId,
            int optionIndex,
            int quantity,
            int numberOfOptions) {

        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Share quantity must be positive."
            );
        }


        ensureEventHoldings(
                eventId,
                numberOfOptions
        );


        List<Integer> eventHoldings =
                holdings.get(
                        eventId
                );


        if (optionIndex < 0 ||
                optionIndex >=
                        eventHoldings.size()) {

            throw new IllegalArgumentException(
                    "Invalid option selection."
            );
        }


        int currentShares =
                eventHoldings.get(
                        optionIndex
                );


        eventHoldings.set(
                optionIndex,
                Math.addExact(
                        currentShares,
                        quantity
                )
        );
    }


    /*
     * Needed when a SELL order
     * is actually executed.
     */
    public void removeShares(
            int eventId,
            int optionIndex,
            int quantity) {

        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Share quantity must be positive."
            );
        }


        List<Integer> eventHoldings =
                holdings.get(
                        eventId
                );


        if (eventHoldings == null) {

            throw new IllegalStateException(
                    "The user does not own shares in this event."
            );
        }


        if (optionIndex < 0 ||
                optionIndex >=
                        eventHoldings.size()) {

            throw new IllegalArgumentException(
                    "Invalid option selection."
            );
        }


        int currentShares =
                eventHoldings.get(
                        optionIndex
                );


        if (currentShares < quantity) {

            throw new IllegalStateException(
                    "The user does not have enough shares."
            );
        }


        eventHoldings.set(
                optionIndex,
                currentShares - quantity
        );
    }


    public int getShares(
            int eventId,
            int optionIndex) {

        List<Integer> eventHoldings =
                holdings.get(
                        eventId
                );


        if (eventHoldings == null) {

            return 0;
        }


        if (optionIndex < 0 ||
                optionIndex >=
                        eventHoldings.size()) {

            throw new IllegalArgumentException(
                    "Invalid option selection."
            );
        }


        return eventHoldings.get(
                optionIndex
        );
    }
}