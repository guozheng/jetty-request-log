package hellojetty.filter;

import hellojetty.StringCachedRequestWrapper;
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

public class SampledRequestLogFilter implements Filter {
  private static Logger LOG = LogManager.getLogger(SampledRequestLogFilter.class);

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    LOG.info("Filter init");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    LOG.info("Start applying filter...");

    // insert a response header
    if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
      HttpServletResponse resp = (HttpServletResponse)response;
      resp.setHeader("X-Test", "true");
    }

    // replace request with wrapper request that caches the body
    request = new StringCachedRequestWrapper((HttpServletRequest) request);

    // process request and generate response by servlet
    chain.doFilter(request, response);

    // read body again for request log
    StringCachedRequestWrapper wrapper = (StringCachedRequestWrapper)request;
    LOG.info("BODY from filter: {}", wrapper.getBody());

    LOG.info("Finished applying filter");
  }

  @Override
  public void destroy() {
    LOG.info("Filter destroyed");
  }
}
