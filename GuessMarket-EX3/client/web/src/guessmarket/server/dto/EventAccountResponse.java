package guessmarket.server.dto;

public class EventAccountResponse {

    private final int eventId;
    private final double balance;


    public EventAccountResponse(
            int eventId,
            double balance) {

        this.eventId = eventId;
        this.balance = balance;
    }


    public int getEventId() {
        return eventId;
    }


    public double getBalance() {
        return balance;
    }
}
