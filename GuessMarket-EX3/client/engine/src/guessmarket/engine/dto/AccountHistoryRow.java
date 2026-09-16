package guessmarket.engine.dto;


public record AccountHistoryRow(
        String description,
        double amount,
        double balanceAfter) {
}
