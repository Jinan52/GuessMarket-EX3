package guessmarket.engine.model;


public class UserLmsrTrade {

    private final int eventId;

    private final String optionName;

    private final int quantity;

    private final double sharesPrice;

    private final double commission;


    public UserLmsrTrade(
            int eventId,
            String optionName,
            int quantity,
            double sharesPrice,
            double commission) {

        this.eventId = eventId;
        this.optionName = optionName;
        this.quantity = quantity;
        this.sharesPrice = sharesPrice;
        this.commission = commission;
    }


    public int getEventId() {

        return eventId;
    }


    public String getOptionName() {

        return optionName;
    }


    public int getQuantity() {

        return quantity;
    }


    public double getSharesPrice() {

        return sharesPrice;
    }


    public double getCommission() {

        return commission;
    }
}
