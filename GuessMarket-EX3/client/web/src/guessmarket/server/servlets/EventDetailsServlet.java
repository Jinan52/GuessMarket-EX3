package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.server.dto.JsonResponse;
import guessmarket.server.utils.ServletUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class EventDetailsServlet extends HttpServlet {

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

        String eventIdText =
                request.getParameter("eventId");

        if (eventIdText == null
                ||
                eventIdText.trim().isEmpty()) {

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    false,
                                    "Event id is required."
                            )
                    )
            );

            return;
        }

        int eventId;

        try {

            eventId =
                    Integer.parseInt(
                            eventIdText.trim()
                    );

        } catch (NumberFormatException exception) {

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    false,
                                    "Event id must be a number."
                            )
                    )
            );

            return;
        }

        try {

            out.print(
                    gson.toJson(
                            ServletUtils.getEngine(
                                    getServletContext()
                            ).getEventDetails(
                                    eventId
                            )
                    )
            );

        } catch (IllegalArgumentException |
                 IllegalStateException exception) {

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    false,
                                    exception.getMessage()
                            )
                    )
            );
        }
    }
}
