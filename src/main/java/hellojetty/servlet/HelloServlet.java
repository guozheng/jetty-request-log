package hellojetty.servlet;

import hellojetty.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HelloServlet extends HttpServlet {
  private static Logger LOG = LogManager.getLogger(HelloServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    LOG.info("Start serving GET request...");

    final String content = "GET Success";
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("text/plain");
    resp.setContentLength(content.length());
    resp.setCharacterEncoding("utf-8");
    resp.getWriter().println(content);

    LOG.info("Finished serving GET request");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    LOG.info("Start serving POST request");

    final String content = "POST Success";
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("text/plain");
    resp.setContentLength(content.length());
    resp.setCharacterEncoding("utf-8");
    resp.getWriter().println(content);

    // processing post body
    LOG.info("POST body: {}", Util.getPostBody(req));

    LOG.info("Finished serving POST request");
  }
}
