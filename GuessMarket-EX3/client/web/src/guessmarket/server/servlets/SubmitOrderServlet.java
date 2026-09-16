package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.engine.model.OrderType;
import guessmarket.server.dto.JsonResponse;
import guessmarket.server.utils.ServletUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class SubmitOrderServlet extends HttpServlet {

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

        String typeText =
                request.getParameter("type");

        if (typeText == null
                ||
                typeText.trim().isEmpty()) {

            writeError(
                    out,
                    "Order type is required."
            );

            return;
        }

        String trimmedType =
                typeText.trim();

        if (!trimmedType.equals("BUY")
                &&
                !trimmedType.equals("SELL")) {

            writeError(
                    out,
                    "Order type must be BUY or SELL."
            );

            return;
        }

        OrderType type =
                OrderType.valueOf(
                        trimmedType
                );

        Integer quantity =
                parseIntParameter(
                        out,
                        request.getParameter("quantity"),
                        "Quantity"
                );

        if (quantity == null) {

            return;
        }

        Double price =
                parseDoubleParameter(
                        out,
                        request.getParameter("price"),
                        "Price"
                );

        if (price == null) {

            return;
        }

        try {

            ServletUtils.getEngine(
                    getServletContext()
            ).submitOrder(
                    userName.trim(),
                    eventId,
                    optionIndex,
                    type,
                    quantity,
                    price
            );

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    true,
                                    "Order submitted successfully."
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
            String fieldName) {

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


    private Double parseDoubleParameter(
            PrintWriter out,
            String text,
            String fieldName) {

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

            double value =
                    Double.parseDouble(
                            text.trim()
                    );

            if (Double.isNaN(value)
                    ||
                    Double.isInfinite(value)) {

                writeError(
                        out,
                        fieldName + " must be a finite number."
                );

                return null;
            }

            return value;

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
