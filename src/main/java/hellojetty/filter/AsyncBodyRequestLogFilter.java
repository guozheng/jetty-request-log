package hellojetty.filter;

import hellojetty.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AsyncBodyRequestLogFilter implements Filter {
    private static Logger LOG = LogManager.getLogger(AsyncBodyRequestLogFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("Filter init: {}", this.getClass());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOG.info("Start applying filter {}...", this.getClass());

        HttpServletResponse resp = (HttpServletResponse) response;
        // replace request with wrapper request that caches the body
        HttpServletRequest req = new CachedBodyRequestWrapper((HttpServletRequest) request);

        // process request and generate response by servlet
        chain.doFilter(req, resp);

        // read body again for request log
        AsyncContext ctx = req.getAsyncContext();
        ctx.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                LOG.info(logRequestWithBody(req, resp));
                LOG.info("ctx completed, applying filter {}", this.getClass());
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                LOG.info(logRequestWithBody(req, resp));
                LOG.info("ctx timed out, applying filter {}", this.getClass());
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                LOG.info(logRequestWithBody(req, resp));
                LOG.info("ctx had error, applying filter {}", this.getClass());
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {

            }
        });
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
