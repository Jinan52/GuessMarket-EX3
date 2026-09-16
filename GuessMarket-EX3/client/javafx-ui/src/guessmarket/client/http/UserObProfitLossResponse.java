package guessmarket.client.http;

public class UserObProfitLossResponse {

    private String userName;
    private int eventId;
    private double profitLoss;


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
