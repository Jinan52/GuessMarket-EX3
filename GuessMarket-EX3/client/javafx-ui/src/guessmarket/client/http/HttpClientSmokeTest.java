package guessmarket.client.http;

import com.google.gson.reflect.TypeToken;
import guessmarket.engine.dto.EventSummary;
import guessmarket.engine.dto.UserSummary;

import java.util.List;
import java.util.Map;

public final class HttpClientSmokeTest {

    public static void main(String[] args) {

        GuessMarketHttpClient client =
                new GuessMarketHttpClient();

        String ping =
                client.getRaw("/ping");

        check(
                "OK".equals(ping),
                "GET /ping must return OK, got: " + ping
        );

        System.out.println("A ping=" + ping);

        String userName =
                "Step5A_"
                        + System.currentTimeMillis();

        ClientJsonResponse login =
                client.postFormJson(
                        "/login",
                        Map.of("userName", userName),
                        ClientJsonResponse.class
                );

        check(
                login.isSuccess(),
                "POST /login must succeed for a new name."
        );

        System.out.println(
                "B login success="
                        + login.isSuccess()
                        + " message="
                        + login.getMessage()
        );

        List<UserSummary> users =
                client.getJson(
                        "/users",
                        new TypeToken<List<UserSummary>>() {
                        }.getType()
                );

        check(
                users != null && !users.isEmpty(),
                "GET /users must return a JSON list with the new user."
        );

        System.out.println("C usersCount=" + users.size());

        List<EventSummary> events =
                client.getJson(
                        "/events",
                        new TypeToken<List<EventSummary>>() {
                        }.getType()
                );

        check(
                events != null,
                "GET /events must return a JSON list."
        );

        System.out.println("D eventsCount=" + events.size());

        boolean connectionErrorReported =
                false;

        try {

            new GuessMarketHttpClient(
                    "http://127.0.0.1:1/guess-market"
            ).getRaw("/ping");

        } catch (GuessMarketHttpException exception) {

            connectionErrorReported = true;
            System.out.println(
                    "E connectionError="
                            + exception.getMessage()
            );
        }

        check(
                connectionErrorReported,
                "A failed connection must throw GuessMarketHttpException."
        );

        System.out.println(
                "All STEP 5A HTTP helper checks passed."
        );
    }


    private static void check(
            boolean condition,
            String message) {

        if (!condition) {

            throw new AssertionError(message);
        }
    }
}
