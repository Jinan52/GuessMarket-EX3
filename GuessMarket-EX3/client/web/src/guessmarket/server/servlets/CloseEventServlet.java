package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.engine.api.GuessMarketEngine;
import guessmarket.engine.dto.EventSummary;
import guessmarket.engine.model.CloseResult;
import guessmarket.engine.model.MarketMethodType;
import guessmarket.server.dto.CloseEventResponse;
import guessmarket.server.dto.JsonResponse;
import guessmarket.server.utils.ServletUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CloseEventServlet extends HttpServlet {

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

        Integer winningOptionIndex =
                parseIntParameter(
                        out,
                        request.getParameter("winningOptionIndex"),
                        "Winning option index"
                );

        if (winningOptionIndex == null) {

            return;
        }

        GuessMarketEngine engine =
                ServletUtils.getEngine(
                        getServletContext()
                );

        try {

            MarketMethodType marketMethod =
                    findMarketMethod(
                            engine,
                            eventId
                    );

            CloseResult closeResult;

            if (marketMethod
                    == MarketMethodType.ORDER_BOOK) {

                closeResult =
                        engine.closeOrderBookEvent(
                                userName.trim(),
                                eventId,
                                winningOptionIndex
                        );

            } else {

                closeResult =
                        engine.closeLmsrEvent(
                                userName.trim(),
                                eventId,
                                winningOptionIndex
                        );
            }

            out.print(
                    gson.toJson(
                            new CloseEventResponse(
                                    true,
                                    "Event closed successfully.",
                                    closeResult.winningOption()
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


    private MarketMethodType findMarketMethod(
            GuessMarketEngine engine,
            int eventId) {

        List<EventSummary> events =
                engine.getAllEvents();

        for (EventSummary event : events) {

            if (event.id() == eventId) {

                return event.marketMethodType();
            }
        }

        return MarketMethodType.LMSR;
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
