package guessmarket.server.dto;

public class ChatMessage {

    private final String userName;
    private final String message;
    private final String timestamp;


    public ChatMessage(
            String userName,
            String message,
            String timestamp) {

        this.userName = userName;
        this.message = message;
        this.timestamp = timestamp;
    }


    public String getUserName() {
        return userName;
    }


    public String getMessage() {
        return message;
    }


    public String getTimestamp() {
        return timestamp;
    }
}
