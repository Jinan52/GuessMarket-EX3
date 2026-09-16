package guessmarket.client.http;

import guessmarket.engine.model.PurchaseResult;

public class LmsrPurchaseResponse {

    private boolean success;
    private String message;
    private PurchaseResult purchase;


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
