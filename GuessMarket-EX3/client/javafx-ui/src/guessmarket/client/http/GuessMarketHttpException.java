package guessmarket.client.http;

public class GuessMarketHttpException extends RuntimeException {

    public GuessMarketHttpException(
            String message) {

        super(message);
    }


    public GuessMarketHttpException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}
