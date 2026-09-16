package guessmarket.client.http;

import java.util.List;

public class UserSharesResponse {

    private String userName;
    private int eventId;
    private List<String> optionNames;
    private List<Integer> shares;


    public String getUserName() {

        return userName;
    }


    public int getEventId() {

        return eventId;
    }


    public List<String> getOptionNames() {

        return optionNames;
    }


    public List<Integer> getShares() {

        return shares;
    }
}
