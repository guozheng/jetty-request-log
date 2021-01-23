package hellojetty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

public class Util {
  private static final Logger LOG = LogManager.getLogger(Util.class);

  public static String getPostBody(HttpServletRequest request) throws IOException {
    return getBody(request);
  }

  /**
   * Read request body with inputstream.
   * @param request
   * @return
   */
  public static String getBody(HttpServletRequest request) {
    StringBuilder stringBuilder = new StringBuilder();
    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
      char[] charBuffer = new char[128];
      int count = -1;
      while ((count = bufferedReader.read(charBuffer)) > 0) {
        stringBuilder.append(charBuffer, 0, count);
      }
    } catch (IOException e) {
      LOG.error("Error reading body from request inputstream", e);
    }

    return stringBuilder.toString();
  }

  /**
   * Read request body with reader.
   * @param request
   * @return
   * @throws IOException
   */
  public static String getBodyWithReader(HttpServletRequest request) throws IOException {
    String body = Optional.ofNullable(request.getReader().lines()
        .collect(Collectors.joining(System.lineSeparator())))
        .orElse("");

//    // experiment with mark, then reset in another place to read again
//    if (request.getReader().markSupported()) {
//      final int mark = 1024;
//      request.getReader().mark(mark);
//      LOG.info("marked request reader at {}", mark);
//    } else {
//      LOG.info("mark not supported on request reader");
//    }

    return body;
  }

  public static boolean shouldSample(HttpServletRequest request) {
    return request.getMethod().equalsIgnoreCase("POST");
  }
}
