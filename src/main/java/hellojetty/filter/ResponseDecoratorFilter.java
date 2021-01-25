package hellojetty.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseDecoratorFilter implements Filter {
    private static Logger LOG = LogManager.getLogger(ResponseDecoratorFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("Filter init: {}", this.getClass());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOG.info("Start applying filter {}...", this.getClass());

        if (response instanceof HttpServletResponse) {
            HttpServletResponse resp = (HttpServletResponse) response;
            // insert a response header
            resp.setHeader("X-Hello-Jetty", "hello");
            chain.doFilter(request, resp);
        } else {
            chain.doFilter(request, response);
        }

        LOG.info("Finished applying filter {}", this.getClass());
    }

    @Override
    public void destroy() {
        LOG.info("Filter destroyed: {}", this.getClass());
    }
}
