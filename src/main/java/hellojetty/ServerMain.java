package hellojetty;

import hellojetty.filter.SampledRequestLogFilter;
import hellojetty.servlet.HelloServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Slf4jRequestLog;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.EnumSet;
import java.util.Locale;
import javax.servlet.DispatcherType;

/**
 * Demo code from Jetty documentation.
 * See: https://www.eclipse.org/jetty/documentation/jetty-9/index.html#embedding-jetty
 */
public class ServerMain {
  private static Logger LOG = LogManager.getLogger(ServerMain.class);

  private static Server createServer() {
    Server server = new Server(8080);

    ServletContextHandler context = new ServletContextHandler(
        server, "/", true, false);

    // add a sync handler
    context.addServlet(HelloServlet.class, "/hello/*");

    // add a test filter
    context.addFilter(SampledRequestLogFilter.class, "/hello/*", EnumSet.of(DispatcherType.REQUEST));

    HandlerCollection handlers = new HandlerCollection();

    // add servlet handlers
    handlers.addHandler(context);

    // add req log
    AbstractNCSARequestLog requestLog = new Slf4jRequestLog();
    requestLog.setLogDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS X");
    requestLog.setLogLocale(Locale.ENGLISH);
    requestLog.setExtended(true);
    requestLog.setLogCookies(false);
    requestLog.setLogTimeZone("GMT");
    requestLog.setLogLatency(true);
    requestLog.setLogServer(true);
    requestLog.setPreferProxiedForAddress(true);
    RequestLogHandler requestLogHandler = new RequestLogHandler();
    requestLogHandler.setRequestLog(requestLog);
    handlers.addHandler(requestLogHandler);

    // add sampled req log for POST
    AbstractNCSARequestLog sampledRequestLog = new SampledRequestLog();
    sampledRequestLog.setLogDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS X");
    sampledRequestLog.setLogLocale(Locale.ENGLISH);
    sampledRequestLog.setExtended(true);
    sampledRequestLog.setLogCookies(false);
    sampledRequestLog.setLogTimeZone("GMT");
    sampledRequestLog.setLogLatency(true);
    sampledRequestLog.setLogServer(true);
    sampledRequestLog.setPreferProxiedForAddress(true);
    RequestLogHandler sampledRequestLogHandler = new RequestLogHandler();
    sampledRequestLogHandler.setRequestLog(sampledRequestLog);
    handlers.addHandler(sampledRequestLogHandler);

    server.setHandler(handlers);

    return server;
  }

  public static void main(String[] args) throws Exception {
    LOG.info("server starting...");
    Server server = createServer();

    server.start();
    LOG.info("server started...");
//    server.dumpStdErr();
    server.join();
  }
}