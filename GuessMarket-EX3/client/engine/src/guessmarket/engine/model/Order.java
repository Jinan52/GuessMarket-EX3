package guessmarket.engine.model;

public class Order {

    private final String userName;
    private final OrderType type;
    private final double price;

    private int remainingQuantity;


    public Order(
            String userName,
            OrderType type,
            int quantity,
            double price) {

        if (userName == null ||
                userName.isEmpty()) {

            throw new IllegalArgumentException(
                    "User name cannot be empty."
            );
        }

        if (type == null) {

            throw new IllegalArgumentException(
                    "Order type cannot be empty."
            );
        }

        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Order quantity must be positive."
            );
        }

        if (price < 0) {

            throw new IllegalArgumentException(
                    "Order price cannot be negative."
            );
        }

        this.userName = userName;
        this.type = type;
        this.remainingQuantity = quantity;
        this.price = price;
    }


    public String getUserName() {
        return userName;
    }


    public OrderType getType() {
        return type;
    }


    public int getRemainingQuantity() {
        return remainingQuantity;
    }


    public double getPrice() {
        return price;
    }


    public void reduceQuantity(
            int quantity) {

        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be positive."
            );
        }

        if (quantity >
                remainingQuantity) {

            throw new IllegalArgumentException(
                    "Cannot reduce more shares than the order contains."
            );
        }

        remainingQuantity -= quantity;
    }


    public boolean isCompleted() {

        return remainingQuantity == 0;
    }
}