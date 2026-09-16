package guessmarket.server.dto;

import guessmarket.engine.model.PurchaseResult;

public class LmsrPurchaseResponse {

    private final boolean success;
    private final String message;
    private final PurchaseResult purchase;


    public LmsrPurchaseResponse(
            boolean success,
            String message,
            PurchaseResult purchase) {

        this.success = success;
        this.message = message;
        this.purchase = purchase;
    }


    public boolean isSuccess() {
        return success;
    }


    public String getMessage() {
        return message;
    }


    public PurchaseResult getPurchase() {
        return purchase;
    }
}
