package guessmarket.engine.dto;

import guessmarket.engine.model.OrderType;

public class PendingOrderInfo {

    private final String userName;
    private final OrderType type;
    private final int quantity;
    private final double price;


    public PendingOrderInfo(
            String userName,
            OrderType type,
            int quantity,
            double price) {

        this.userName = userName;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
    }


    public String getUserName() {
        return userName;
    }


    public OrderType getType() {
        return type;
    }


    public int getQuantity() {
        return quantity;
    }


    public double getPrice() {
        return price;
    }
}