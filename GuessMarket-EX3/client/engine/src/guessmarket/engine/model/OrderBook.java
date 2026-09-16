package guessmarket.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderBook {

    private final List<Order> buyOrders;
    private final List<Order> sellOrders;

    /*
     * Price of the last completed trade.
     * null means that no trade happened yet.
     */
    private Double lastPrice;


    public OrderBook() {

        this.buyOrders =
                new ArrayList<>();

        this.sellOrders =
                new ArrayList<>();

        this.lastPrice = null;
    }


    public void addOrder(
            Order order) {

        if (order == null) {

            throw new IllegalArgumentException(
                    "Order cannot be null."
            );
        }


        if (order.getType()
                == OrderType.BUY) {

            buyOrders.add(
                    order
            );

        } else {

            sellOrders.add(
                    order
            );
        }
    }


    public List<Order> getBuyOrders() {

        return Collections.unmodifiableList(
                buyOrders
        );
    }


    public List<Order> getSellOrders() {

        return Collections.unmodifiableList(
                sellOrders
        );
    }


    public void removeBuyOrder(
            Order order) {

        buyOrders.remove(
                order
        );
    }


    public void removeSellOrder(
            Order order) {

        sellOrders.remove(
                order
        );
    }


    public boolean hasBuyOrders() {

        return !buyOrders.isEmpty();
    }


    public boolean hasSellOrders() {

        return !sellOrders.isEmpty();
    }


    public Order getHighestBuyOrder() {

        if (buyOrders.isEmpty()) {
            return null;
        }


        Order bestOrder =
                buyOrders.get(0);


        for (int i = 1;
             i < buyOrders.size();
             i++) {

            Order currentOrder =
                    buyOrders.get(i);


            if (currentOrder.getPrice()
                    > bestOrder.getPrice()) {

                bestOrder =
                        currentOrder;
            }
        }


        return bestOrder;
    }


    public Order getLowestSellOrder() {

        if (sellOrders.isEmpty()) {
            return null;
        }


        Order bestOrder =
                sellOrders.get(0);


        for (int i = 1;
             i < sellOrders.size();
             i++) {

            Order currentOrder =
                    sellOrders.get(i);


            if (currentOrder.getPrice()
                    < bestOrder.getPrice()) {

                bestOrder =
                        currentOrder;
            }
        }


        return bestOrder;
    }


    /*
     * LAST
     */
    public Double getLastPrice() {

        return lastPrice;
    }


    public void setLastPrice(
            double lastPrice) {

        this.lastPrice =
                lastPrice;
    }


    /*
     * BID
     */
    public Double getHighestBid() {

        Order order =
                getHighestBuyOrder();


        if (order == null) {
            return null;
        }


        return order.getPrice();
    }


    /*
     * ASK
     */
    public Double getLowestAsk() {

        Order order =
                getLowestSellOrder();


        if (order == null) {
            return null;
        }


        return order.getPrice();
    }


    /*
     * MID
     */
    public Double getMidPrice() {

        Double bid =
                getHighestBid();

        Double ask =
                getLowestAsk();


        if (bid == null ||
                ask == null) {

            return null;
        }


        return (bid + ask) / 2.0;
    }


    /*
     * SPREAD
     */
    public Double getSpread() {

        Double bid =
                getHighestBid();

        Double ask =
                getLowestAsk();


        if (bid == null ||
                ask == null) {

            return null;
        }


        return ask - bid;
    }
}