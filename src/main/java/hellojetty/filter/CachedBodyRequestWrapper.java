package hellojetty.filter;

import hellojetty.Util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
  private final String body;

  /**
   * Constructs a request object wrapping the given request.
   *
   * @throws IllegalArgumentException if the request is null
   */
  public CachedBodyRequestWrapper(HttpServletRequest request) {
    super(request);
    this.body = Util.getBody(request);
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
    return new CachedBodyServletInputStream(byteArrayInputStream);
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(this.getInputStream()));
  }

  /**
   * Use this method to read body as many times as you need.
   * @return
   */
  public String getBody() {
    return this.body;
  }
}
