package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.server.dto.JsonResponse;
import guessmarket.server.utils.ServletUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class FundsServlet extends HttpServlet {

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

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    false,
                                    "User name cannot be empty."
                            )
                    )
            );

            return;
        }

        String amountText =
                request.getParameter("amount");

        if (amountText == null
                ||
                amountText.trim().isEmpty()) {

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    false,
                                    "Amount is required."
                            )
                    )
            );

            return;
        }

        double amount;

        try {

            amount =
                    Double.parseDouble(
                            amountText.trim()
                    );

        } catch (NumberFormatException exception) {

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    false,
                                    "Amount must be a number."
                            )
                    )
            );

            return;
        }

        try {

            ServletUtils.getEngine(
                    getServletContext()
            ).addFunds(
                    userName.trim(),
                    amount
            );

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    true,
                                    "Funds added successfully."
                            )
                    )
            );

        } catch (IllegalArgumentException exception) {

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
