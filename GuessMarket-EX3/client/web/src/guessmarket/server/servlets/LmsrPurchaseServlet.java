package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.engine.model.PurchaseResult;
import guessmarket.server.dto.JsonResponse;
import guessmarket.server.dto.LmsrPurchaseResponse;
import guessmarket.server.utils.ServletUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class LmsrPurchaseServlet extends HttpServlet {

    private final Gson gson =
            new Gson();


    @Override
    public void doPost(
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

        Integer quantity =
                parseIntParameter(
                        out,
                        request.getParameter("quantity"),
                        "Quantity"
                );

        if (quantity == null) {

            return;
        }

        try {

            PurchaseResult purchase =
                    ServletUtils.getEngine(
                            getServletContext()
                    ).purchaseShares(
                            userName.trim(),
                            eventId,
                            optionIndex,
                            quantity
                    );

            out.print(
                    gson.toJson(
                            new LmsrPurchaseResponse(
                                    true,
                                    "Purchase completed.",
                                    purchase
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


    private Integer parseIntParameter(
            PrintWriter out,
            String text,
            String fieldName)
            throws IOException {

        if (text == null
                ||
                text.trim().isEmpty()) {

            writeError(
                    out,
                    fieldName + " is required."
            );

            return null;
        }

        try {

            return Integer.parseInt(
                    text.trim()
            );

        } catch (NumberFormatException exception) {

            writeError(
                    out,
                    fieldName + " must be a number."
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
