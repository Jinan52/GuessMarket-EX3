package guessmarket.client.http;

import java.util.List;

public class UserObAmountPaidResponse {

    private String userName;
    private int eventId;
    private List<Double> amountPaid;


    public String getUserName() {

        return userName;
    }


    public int getEventId() {

        return eventId;
    }


    public List<Double> getAmountPaid() {

        return amountPaid;
    }
}
