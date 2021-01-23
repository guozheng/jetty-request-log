package hellojetty;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.ServletInputStream;

public class SampledRequestLog extends AbstractNCSARequestLog {
  private static final Logger LOG = LogManager.getLogger(SampledRequestLog.class);

  @Override
  protected boolean isEnabled() {
    return true;
  }

  @Override
  public void write(String requestEntry) throws IOException {
    LOG.info(requestEntry, new Object[0]);
  }

  @Override
  public void log(Request request, Response response) {
    if (Util.shouldSample(request)) {
//      logWithByteArrayStreamCachedRequestWrapper(request);
//      logWithStringCachedRequestWrapper(request);
//      logWithInputStreamReset(request);
//      logWithReaderReset(request);

      // NONE of the above works ;-(

      LOG.info("BODY from sampled request log: {}", Util.getBody(request));
    }
  }

  /**
   * Log with a request wrapper that caches the request body.
   * But this does not seem to work since the servlet already consumed request body input stream.
   * @param request
   */
  private void logWithByteArrayStreamCachedRequestWrapper(Request request) {
    final ByteArrayStreamCachedRequestWrapper cachedRequestWrapper =
        new ByteArrayStreamCachedRequestWrapper(request);
    try {
      LOG.info("BODY: {}", IOUtils.toString(cachedRequestWrapper.getInputStream()));
    } catch (IOException e) {
      LOG.error("Error logging BODY", e);
    }
  }

  /**
   * A variant of cached request wrapper, the body is stored as a string.
   * It has the same problem that request input stream has been consumed already.
   * @param request
   */
  private void logWithStringCachedRequestWrapper(Request request) {
    final StringCachedRequestWrapper wrapper = new StringCachedRequestWrapper(request);
    LOG.info("BODY: {}", wrapper.getBody());
  }

  /**
   * Log with request reader reset.
   * The reader needs to be marked when we process request body for the first time.
   * @param request
   */
  private void logWithReaderReset(Request request) {
    try {
      if (!request.getReader().ready() && request.getReader().markSupported()) {
        request.getReader().reset(); // reset to the most recent mark
        LOG.info("Request reader reset");
      }
      String body = Optional.ofNullable(request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()))).orElse("");
      LOG.info("BODY: '{}'", body);
    } catch (IOException e) {
      LOG.error("Error logging BODY", e);
    }
  }

  /**
   * Log with input stream reset.
   * The input stream needs to be marked when we process the request body for the first time.
   * @param request
   */
  private void logWithInputStreamReset(Request request) {
    try {
      ServletInputStream sis = request.getInputStream();
      if (!sis.isReady() && sis.markSupported()) {
        sis.reset();
        LOG.info("BODY: {}", IOUtils.toString(request.getInputStream()));
      } else {
        LOG.info("Request inputstream does not support mark");
      }
    } catch (IOException e) {
      LOG.error("Error logging BODY", e);
    }
  }

}
