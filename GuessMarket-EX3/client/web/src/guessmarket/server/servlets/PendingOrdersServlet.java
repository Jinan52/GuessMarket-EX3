package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.server.dto.JsonResponse;
import guessmarket.server.utils.ServletUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class PendingOrdersServlet extends HttpServlet {

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

        Integer eventId =
                parseIntParameter(
                        out,
                        request.getParameter("eventId"),
                        "Event id"
                );

        if (eventId == null) {

            return;
        }

        Integer optionIndex =
                parseIntParameter(
                        out,
                        request.getParameter("optionIndex"),
                        "Option index"
                );

        if (optionIndex == null) {

            return;
        }

        try {

            out.print(
                    gson.toJson(
                            ServletUtils.getEngine(
                                    getServletContext()
                            ).getPendingOrders(
                                    eventId,
                                    optionIndex
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


    private Integer parseIntParameter(
            PrintWriter out,
            String text,
            String fieldName) {

        if (text == null
                ||
                text.trim().isEmpty()) {

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    false,
                                    fieldName + " is required."
                            )
                    )
            );

            return null;
        }

        try {

            return Integer.parseInt(
                    text.trim()
            );

        } catch (NumberFormatException exception) {

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    false,
                                    fieldName + " must be a number."
                            )
                    )
            );

            return null;
        }
    }
}
