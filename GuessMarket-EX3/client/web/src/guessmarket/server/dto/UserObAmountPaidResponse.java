package guessmarket.server.dto;

import java.util.List;

public class UserObAmountPaidResponse {

    private final String userName;
    private final int eventId;
    private final List<Double> amountPaid;


    public UserObAmountPaidResponse(
            String userName,
            int eventId,
            List<Double> amountPaid) {

        this.userName = userName;
        this.eventId = eventId;
        this.amountPaid = amountPaid;
    }


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
