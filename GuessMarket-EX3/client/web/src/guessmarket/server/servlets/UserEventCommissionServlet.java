package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.server.dto.JsonResponse;
import guessmarket.server.dto.UserEventCommissionResponse;
import guessmarket.server.utils.ServletUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class UserEventCommissionServlet extends HttpServlet {

    private final Gson gson =
            new Gson();


    @Override
    public void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out =
                response.getWriter();

        String userName =
                request.getParameter("userName");

        if (userName == null
                ||
                userName.trim().isEmpty()) {

            writeError(
                    out,
                    "User name cannot be empty."
            );

            return;
        }

        Integer eventId =
                parseEventId(
                        out,
                        request.getParameter("eventId")
                );

        if (eventId == null) {

            return;
        }

        String trimmedUserName =
                userName.trim();

        try {

            double commission =
                    ServletUtils.getEngine(
                            getServletContext()
                    ).getUserCommissionPaidForEvent(
                            trimmedUserName,
                            eventId
                    );

            out.print(
                    gson.toJson(
                            new UserEventCommissionResponse(
                                    trimmedUserName,
                                    eventId,
                                    commission
                            )
                    )
            );

        } catch (IllegalArgumentException |
                 IllegalStateException exception) {

            writeError(
                    out,
                    exception.getMessage()
            );
        }
    }


    private Integer parseEventId(
            PrintWriter out,
            String eventIdText) {

        if (eventIdText == null
                ||
                eventIdText.trim().isEmpty()) {

            writeError(
                    out,
                    "Event id is required."
            );

            return null;
        }

        try {

            return Integer.parseInt(
                    eventIdText.trim()
            );

        } catch (NumberFormatException exception) {

            writeError(
                    out,
                    "Event id must be a number."
            );

            return null;
        }
    }


    private void writeError(
            PrintWriter out,
            String message) {

        out.print(
                gson.toJson(
                        new JsonResponse(
                                false,
                                message
                        )
                )
        );
    }
}
