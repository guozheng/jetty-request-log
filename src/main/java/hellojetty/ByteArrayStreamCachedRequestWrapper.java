package hellojetty;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Input from a request is a stream, once consumed, we cannot read from the stream anymore.
 * So, we need to extend request wrapper and cache the request body for reading again.
 * See: https://stackoverflow.com/questions/4449096/how-to-read-request-getinputstream-multiple-times
 */
public class ByteArrayStreamCachedRequestWrapper extends HttpServletRequestWrapper {
  private ByteArrayOutputStream cachedBytes;

  /**
   * Constructs a request object wrapping the given request.
   *
   * @throws IllegalArgumentException if the request is null
   */
  public ByteArrayStreamCachedRequestWrapper(HttpServletRequest request) {
    super(request);
  }

  private void cacheInputStream() throws IOException {
    cachedBytes = new ByteArrayOutputStream();
    IOUtils.copy(super.getInputStream(), cachedBytes); // getting inputstream gives error
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (cachedBytes == null) {
      cacheInputStream();
    }
    return new ByteArrayStreamCachedServletInputStream(cachedBytes);
  }
}
