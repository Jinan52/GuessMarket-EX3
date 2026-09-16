package guessmarket.engine.dto;

import guessmarket.engine.model.CommissionType;
import guessmarket.engine.model.EventStatus;
import guessmarket.engine.model.MarketMethodType;

import java.util.List;

public record EventSummary(
        int id,
        String name,
        String description,
        int commissionPercent,
        CommissionType commissionType,
        List<String> options,
        EventStatus status,
        MarketMethodType marketMethodType,
        double liquidityParameter,
        int orderBookInitial,
        int orderBookD,
        boolean allowMint) {
}