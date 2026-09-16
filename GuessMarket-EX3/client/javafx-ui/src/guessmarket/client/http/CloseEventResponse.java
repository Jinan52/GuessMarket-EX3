package guessmarket.client.http;

public class CloseEventResponse {

    private boolean success;
    private String message;
    private String winningOption;


    public boolean isSuccess() {

        return success;
    }


    public String getMessage() {

        return message;
    }


    public String getWinningOption() {

        return winningOption;
    }
}
