package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.server.utils.ServletUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ChatServlet extends HttpServlet {

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

        out.print(
                gson.toJson(
                        ServletUtils.getChatLog(
                                getServletContext()
                        ).snapshot()
                )
        );
    }
}
