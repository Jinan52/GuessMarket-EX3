package guessmarket.server.dto;

import java.util.List;

public class UserSharesResponse {

    private final String userName;
    private final int eventId;
    private final List<String> optionNames;
    private final List<Integer> shares;


    public UserSharesResponse(
            String userName,
            int eventId,
            List<String> optionNames,
            List<Integer> shares) {

        this.userName = userName;
        this.eventId = eventId;
        this.optionNames = optionNames;
        this.shares = shares;
    }


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
