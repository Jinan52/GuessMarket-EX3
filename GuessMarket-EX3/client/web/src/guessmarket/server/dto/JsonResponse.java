package guessmarket.server.dto;

public class JsonResponse {

    private final boolean success;
    private final String message;


    public JsonResponse(
            boolean success,
            String message) {

        this.success = success;
        this.message = message;
    }


    public boolean isSuccess() {
        return success;
    }


    public String getMessage() {
        return message;
    }
}
