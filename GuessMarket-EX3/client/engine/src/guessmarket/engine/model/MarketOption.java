package guessmarket.engine.model;

public final class MarketOption {
    private final String name;
    private int purchasedShares;

    public MarketOption(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getPurchasedShares() {
        return purchasedShares;
    }

    public void addShares(int quantity) {
        purchasedShares = Math.addExact(purchasedShares, quantity);
    }
}
