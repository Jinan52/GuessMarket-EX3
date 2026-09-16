package guessmarket.server.dto;

public class UserObProfitLossResponse {

    private final String userName;
    private final int eventId;
    private final double profitLoss;


    public UserObProfitLossResponse(
            String userName,
            int eventId,
            double profitLoss) {

        this.userName = userName;
        this.eventId = eventId;
        this.profitLoss = profitLoss;
    }


    public String getUserName() {
        return userName;
    }


    public int getEventId() {
        return eventId;
    }


    public double getProfitLoss() {
        return profitLoss;
    }
}
