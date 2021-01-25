package hellojetty.servlet;

import hellojetty.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class AsyncHelloServlet extends HttpServlet {
    private static Logger LOG = LogManager.getLogger(AsyncHelloServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final String content = "Async GET successful";
        AsyncContext ctx = req.startAsync();
        ctx.start(new Runnable() {
            @Override
            public void run() {
                LOG.info("Start serving async GET request...");
                try {
                    resp.setStatus(SC_OK);
                    resp.setContentType("text/plain");
                    resp.setContentLength(content.length());
                    resp.setCharacterEncoding("utf-8");
                    resp.getWriter().println(content);
                } catch (IOException e) {
                    try {
                        resp.sendError(SC_INTERNAL_SERVER_ERROR);
                    } catch (IOException ioException) {

                    }
                } finally {
                    ctx.complete();
                    LOG.info("Finished serving async GET request");
                }
            }
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final String content = "Async POST successful";
        AsyncContext ctx = req.startAsync();
        ctx.setTimeout(100);
        ctx.start(new Runnable() {
            @Override
            public void run() {
                LOG.info("Start serving async POST request...");
                try {
                    LOG.info("POST body: {}", Util.getBody(req));
                    resp.setStatus(SC_OK);
                    resp.setContentType("text/plain");
                    resp.setContentLength(content.length());
                    resp.setCharacterEncoding("utf-8");
                    resp.getWriter().println(content);
                } catch (IOException e) {
                    try {
                        resp.sendError(SC_INTERNAL_SERVER_ERROR);
                    } catch (IOException ioException) {

                    }
                } finally {
                    ctx.complete();
                    LOG.info("Finished serving async POST request");
                }
            }
        });
    }
}
