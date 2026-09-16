package guessmarket.engine.dto;

import guessmarket.engine.model.CommissionType;
import guessmarket.engine.model.EventStatus;

import java.util.List;

public record EventDetails(
        int id,
        String name,
        String description,
        int commissionPercent,
        CommissionType commissionType,
        EventStatus status,
        List<OptionState> options,
        double accountBalance,
        double collectedCommission,
        List<TradeView> tradeHistory,
        String winningOption) {
}
