package guessmarket.server.dto;

public class CloseEventResponse {

    private final boolean success;
    private final String message;
    private final String winningOption;


    public CloseEventResponse(
            boolean success,
            String message,
            String winningOption) {

        this.success = success;
        this.message = message;
        this.winningOption = winningOption;
    }


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
