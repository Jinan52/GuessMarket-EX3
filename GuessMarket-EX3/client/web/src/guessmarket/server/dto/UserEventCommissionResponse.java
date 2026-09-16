package guessmarket.server.dto;

public class UserEventCommissionResponse {

    private final String userName;
    private final int eventId;
    private final double commission;


    public UserEventCommissionResponse(
            String userName,
            int eventId,
            double commission) {

        this.userName = userName;
        this.eventId = eventId;
        this.commission = commission;
    }


    public String getUserName() {
        return userName;
    }


    public int getEventId() {
        return eventId;
    }


    public double getCommission() {
        return commission;
    }
}
