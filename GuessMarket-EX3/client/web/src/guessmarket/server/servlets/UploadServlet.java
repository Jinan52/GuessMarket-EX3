package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.engine.api.GuessMarketEngine;
import guessmarket.engine.dto.LoadResult;
import guessmarket.server.dto.JsonResponse;
import guessmarket.server.utils.ServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

@MultipartConfig(
        fileSizeThreshold = 10 * 1024 * 1024,
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 12 * 1024 * 1024
)
public class UploadServlet extends HttpServlet {

    private final Gson gson =
            new Gson();


    @Override
    public void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out =
                response.getWriter();

        String userName =
                request.getParameter("userName");

        Part filePart =
                request.getPart("file");

        if (filePart == null
                ||
                filePart.getSize() == 0) {

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    false,
                                    "XML file is required."
                            )
                    )
            );

            return;
        }

        GuessMarketEngine engine =
                ServletUtils.getEngine(
                        getServletContext()
                );

        try (InputStream xmlSource =
                     filePart.getInputStream()) {

            LoadResult loadResult =
                    engine.loadXml(
                            xmlSource,
                            userName
                    );

            out.print(
                    gson.toJson(
                            new JsonResponse(
                                    loadResult.success(),
                                    loadResult.message()
                            )
                    )
            );
        }
    }
}
