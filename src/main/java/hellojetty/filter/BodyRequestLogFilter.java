package hellojetty.filter;

import hellojetty.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BodyRequestLogFilter implements Filter {
  private static Logger LOG = LogManager.getLogger(BodyRequestLogFilter.class);

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    LOG.info("Filter init: {}", this.getClass());
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    LOG.info("Start applying filter {}...", this.getClass());

    HttpServletResponse resp = (HttpServletResponse) response;
    // replace request with wrapper request that caches the body
    HttpServletRequest req = new CachedBodyRequestWrapper((HttpServletRequest) request);

    // process request and generate response by servlet
    chain.doFilter(req, resp);

    // read body again for request log
    LOG.info(logRequestWithBody(req, resp));

    LOG.info("Finished applying filter {}", this.getClass());
  }

  @Override
  public void destroy() {
    LOG.info("Filter destroyed: {}", this.getClass());
  }

  protected String logRequestWithBody(HttpServletRequest request,
                                          HttpServletResponse response) {
    return Util.buildRequestLogLine(request, response).toString();
  }

}
