package guessmarket.engine.dto;

import java.util.List;

public class UserSummary {

    private final String name;
    private final double balance;
    private final boolean blocked;
    private final List<Integer> marketMakerEventIds;


    public UserSummary(
            String name,
            double balance,
            boolean blocked,
            List<Integer> marketMakerEventIds) {

        this.name = name;
        this.balance = balance;
        this.blocked = blocked;
        this.marketMakerEventIds = marketMakerEventIds;
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
        return marketMakerEventIds;
    }
}