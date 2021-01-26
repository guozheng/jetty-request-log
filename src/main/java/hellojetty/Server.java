package hellojetty;

import hellojetty.filter.AsyncBodyRequestLogFilter;
import hellojetty.filter.BodyRequestLogFilter;
import hellojetty.filter.ResponseDecoratorFilter;
import hellojetty.servlet.AsyncHelloServlet;
import hellojetty.servlet.HelloServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
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
public class Server {
  private static Logger LOG = LogManager.getLogger(Server.class);

  private static org.eclipse.jetty.server.Server createServer() {
    org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(8080);

    ServletContextHandler context = new ServletContextHandler(
        server, "/", true, false);

    // add a sync handler
    context.addServlet(HelloServlet.class, "/hello/*");
    // add an async handler
    context.addServlet(AsyncHelloServlet.class, "/async-hello/*");

    // add a sync request logging filter
    context.addFilter(BodyRequestLogFilter.class, "/hello/*",
            EnumSet.of(DispatcherType.REQUEST));
    // add an async request logging filter
    context.addFilter(AsyncBodyRequestLogFilter.class, "/async-hello/*",
            EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC));

    // add a sync response decoration filter
    context.addFilter(ResponseDecoratorFilter.class, "/*",
            EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC));

    HandlerCollection handlers = new HandlerCollection();

    // add servlet handlers
    handlers.addHandler(context);

    // add req log using Jetty's Slf4jRequestLog, it does not log request body
    Slf4jRequestLog requestLog = new Slf4jRequestLog();
    requestLog.setLoggerName("Slf4jRequestLog"); // see logger config in log4j2.xml
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

    server.setHandler(handlers);

    return server;
  }

  public static void main(String[] args) throws Exception {
    LOG.info("server starting...");
    org.eclipse.jetty.server.Server server = createServer();

    server.start();
    LOG.info("server started...");
//    server.dumpStdErr();
    server.join();
  }
}