package hellojetty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Util {
  private static final Logger LOG = LogManager.getLogger(Util.class);

  public static String getBodyAsOneLine(HttpServletRequest request) {
    try {
      return request.getReader()
              .lines()
              .map(s -> s.trim())
              .collect(Collectors.joining());
    } catch (IOException e) {
      return "";
    }
  }

  public static String getBody(HttpServletRequest request) {
    try {
      return request.getReader()
              .lines()
              .collect(Collectors.joining());
    } catch (IOException e) {
      return "";
    }
  }

  /**
   * Log request based on Apache log format or NCSA format, last field is request body.
   * For details see: https://www.loganalyzer.net/log-analyzer/apache-combined-log.html
   *
   * @param request         {@link HttpServletRequest} request
   * @param response        {@link HttpServletResponse} response
   * @return
   */
  public static StringBuilder buildRequestLogLine(HttpServletRequest request,
                                              HttpServletResponse response) {
    StringBuilder buf = new StringBuilder();

    // server name
    buf.append(request.getServerName()).append(' ');

    // remote address, prefer XFF request header value if available
    String addr = request.getHeader(HttpHeader.X_FORWARDED_FOR.toString());
    if (addr == null) {
      addr = request.getRemoteAddr();
    }
    buf.append(addr);

    // RFC931: id for the client making HTTP request, use - here
    buf.append(" -");

    // username, use - here
    buf.append(" -");

    // timestamp, e.g. [14/Jan/2021:07:29:20 +0000]
    buf.append(" [")
            .append(DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.now()))
            .append("] ");

    // method and URI
    buf.append("\"")
            .append(request.getMethod())
            .append(' ')
            .append(request.getRequestURI())
            .append(' ')
            .append(request.getProtocol())
            .append("\" ");

    // status code
    int status = response.getStatus();
    if (status >= 0) {
      buf.append((char)(48 + status / 100 % 10));
      buf.append((char)(48 + status / 10 % 10));
      buf.append((char)(48 + status % 10));
    } else {
      buf.append(status);
    }

    // response content length not available
    buf.append(" - ");

    // REFERER request header
    String referer = request.getHeader(HttpHeader.REFERER.toString());
    if (referer == null) {
      buf.append("\"-\" ");
    } else {
      buf.append('"');
      buf.append(referer);
      buf.append("\" ");
    }

    // User-Agent request header
    String agent = request.getHeader(HttpHeader.USER_AGENT.toString());
    if (agent == null) {
      buf.append("\"-\"");
    } else {
      buf.append('"');
      buf.append(agent);
      buf.append('"');
    }

    // latency not available, set to 0
    buf.append(" ").append(0).append(" ");

    // finally log the body
    // body contains line breaks, we combine the lines into one line
    String body = getBodyAsOneLine(request);
    buf.append(body);
    return buf;
  }
}
