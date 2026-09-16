package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.engine.api.GuessMarketEngine;
import guessmarket.engine.dto.UserSummary;
import guessmarket.server.dto.ChatMessage;
import guessmarket.server.dto.JsonResponse;
import guessmarket.server.utils.ServletUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;

public class ChatSendServlet extends HttpServlet {

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

        String message =
                request.getParameter("message");

        if (message == null
                ||
                message.trim().isEmpty()) {

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    false,
                                    "Message cannot be empty."
                            )
                    )
            );

            return;
        }

        String trimmedName =
                userName.trim();
        String trimmedMessage =
                message.trim();

        GuessMarketEngine engine =
                ServletUtils.getEngine(
                        getServletContext()
                );

        if (!userExists(engine, trimmedName)) {

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    false,
                                    "A user with name "
                                            + trimmedName
                                            + " was not found."
                            )
                    )
            );

            return;
        }

        ServletUtils.getChatLog(
                getServletContext()
        ).add(
                new ChatMessage(
                        trimmedName,
                        trimmedMessage,
                        Instant.now().toString()
                )
        );

        out.print(
                gson.toJson(
                        new JsonResponse(
                                true,
                                "Message sent successfully."
                        )
                )
        );
    }


    private boolean userExists(
            GuessMarketEngine engine,
            String userName) {

        List<UserSummary> users =
                engine.getAllUsers();

        for (UserSummary user : users) {

            if (user.getName().equals(userName)) {

                return true;
            }
        }

        return false;
    }
}
