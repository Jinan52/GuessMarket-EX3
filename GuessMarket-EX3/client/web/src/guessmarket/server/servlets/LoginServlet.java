package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.engine.api.GuessMarketEngine;
import guessmarket.server.dto.JsonResponse;
import guessmarket.server.utils.ServletUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class LoginServlet extends HttpServlet {

    private final Gson gson =
            new Gson();


    @Override
    public void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        String userName =
                request.getParameter("userName");

        GuessMarketEngine engine =
                ServletUtils.getEngine(
                        getServletContext()
                );

        PrintWriter out =
                response.getWriter();

        try {

            engine.registerUser(
                    userName
            );

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    true,
                                    "User registered successfully."
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
