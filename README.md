# Log Jetty Request Body in Both Sync and Async Request Processing

## Overview
Jetty provides classes such as `NCSARequestLog`, `Slf4jRequestLog` (older versions) and `CustomRequestLog` (newer versions) to log requests in the [https://www.loganalyzer.net/log-analyzer/apache-combined-log.html](NCSA Format), also known as Common Log Format or Apache Log Format. However, these request log implementation does not include request body. In many use cases such as debugging, replaying the requests for performance testing, etc. request body in the request log will be super useful.

However, it is a bit tricky to log request body since the content for a request object is a stream and it can be consumed only once, when the body is needed to implement the application logic. It is of course possible to log the request body right after consuming the stream, but it is not a clean and reusable way, also for async servlet, this way of logging the request body gets in the way of sending response.

`HttpServletRequestWrapper`, as part of the Servlet standard, provides a way to extend `HttpServletRequest` for custom needs. We extend `HttpServletRequestWrapper` to cache the request body, then override `getInputStream` and `getReader` methods to read from the cache body. In this way, the request body can be read multiple times. See `CachedBodyRequestWrapper` for details.

`Filter` is a reusable and pluggable mechanism to pre or post process request and responses. For example, we can use filters to pre-process requests, e.g. validate and filter out invalid requests, check authentication, rate limit requests, etc. We can also use filters to post-process responses after servlet processor logic, e.g. add extra HTTP headers, enhance response body, log the request, etc. In this example, we use the filter to log request with body.

## Log Output
In this simple demo, we use log4j2 to implement logging. The configuration is in `log4j2.xml`.

   * *server log*: server log is configured to output at console
   * *default request Jetty log*: as a comparison, we use `Slf4jRequestLog` to output request log without body in the file `/tmp/slf4j_request.log`
   * *request log with body*: our request log implementation with body as the last field on a request log line, output to the file `/tmp/request.log`

## How to Run
```commandline
$./gradlew build run

[INFO ] 2021-01-24 23:44:57.647 [main] Server - server starting...
[INFO ] 2021-01-24 23:44:57.674 [main] log - Logging initialized @683ms to org.eclipse.jetty.util.log.Slf4jLog
[INFO ] 2021-01-24 23:44:57.734 [main] Server - jetty-9.4.6.v20170531
[INFO ] 2021-01-24 23:44:57.764 [main] session - DefaultSessionIdManager workerName=node0
[INFO ] 2021-01-24 23:44:57.764 [main] session - No SessionScavenger set, using defaults
[INFO ] 2021-01-24 23:44:57.766 [main] session - Scavenging every 600000ms
[INFO ] 2021-01-24 23:44:57.772 [main] BodyRequestLogFilter - Filter init: class hellojetty.filter.BodyRequestLogFilter
[INFO ] 2021-01-24 23:44:57.772 [main] AsyncBodyRequestLogFilter - Filter init: class hellojetty.filter.AsyncBodyRequestLogFilter
[INFO ] 2021-01-24 23:44:57.773 [main] ResponseDecoratorFilter - Filter init: class hellojetty.filter.ResponseDecoratorFilter
[INFO ] 2021-01-24 23:44:57.774 [main] ContextHandler - Started o.e.j.s.ServletContextHandler@4bff7da0{/,null,AVAILABLE}
[INFO ] 2021-01-24 23:44:57.811 [main] AbstractConnector - Started ServerConnector@1df8b5b8{HTTP/1.1,[http/1.1]}{0.0.0.0:8080}
[INFO ] 2021-01-24 23:44:57.811 [main] Server - Started @822ms
[INFO ] 2021-01-24 23:44:57.812 [main] Server - server started...
```

## Sample Requests
### GET
```commandline
$curl --location --request GET 'localhost:8080/hello/'

GET Success

// console output
[INFO ] 2021-01-25 21:43:01.353 [qtp1560940633-22] BodyRequestLogFilter - Start applying filter class hellojetty.filter.BodyRequestLogFilter...
[INFO ] 2021-01-25 21:43:01.354 [qtp1560940633-22] ResponseDecoratorFilter - Start applying filter class hellojetty.filter.ResponseDecoratorFilter...
[INFO ] 2021-01-25 21:43:01.354 [qtp1560940633-22] HelloServlet - Start serving GET request...
[INFO ] 2021-01-25 21:43:01.355 [qtp1560940633-22] HelloServlet - Finished serving GET request
[INFO ] 2021-01-25 21:43:01.355 [qtp1560940633-22] ResponseDecoratorFilter - Finished applying filter class hellojetty.filter.ResponseDecoratorFilter
[INFO ] 2021-01-25 21:43:01.356 [qtp1560940633-22] BodyRequestLogFilter - Finished applying filter class hellojetty.filter.BodyRequestLogFilter

// /tmp/slf4j_request.log (jetty's request log)
localhost 0:0:0:0:0:0:0:1 - - [2021-01-26T05:42:49.580 Z] "GET /hello HTTP/1.1" 200 14 "-" "PostmanRuntime/7.26.10" 77

// /tmp/request.log (our request log)
localhost 0:0:0:0:0:0:0:1 - - [25/Jan/2021:21:42:49 -0800] "GET /hello HTTP/1.1" 200 - "-" "PostmanRuntime/7.26.10" 0
```
### POST
```commandline
$curl --location --request POST 'localhost:8080/hello/' \
 --header 'Content-Type: text/plain' \
 --data-raw '{"name": "jetty"}'

POST Success

// console output
[INFO ] 2021-01-25 21:45:17.168 [qtp1560940633-18] BodyRequestLogFilter - Start applying filter class hellojetty.filter.BodyRequestLogFilter...
[INFO ] 2021-01-25 21:45:17.169 [qtp1560940633-18] ResponseDecoratorFilter - Start applying filter class hellojetty.filter.ResponseDecoratorFilter...
[INFO ] 2021-01-25 21:45:17.169 [qtp1560940633-18] HelloServlet - Start serving POST request
[INFO ] 2021-01-25 21:45:17.169 [qtp1560940633-18] HelloServlet - POST body: {"name": "jetty"}
[INFO ] 2021-01-25 21:45:17.170 [qtp1560940633-18] HelloServlet - Finished serving POST request
[INFO ] 2021-01-25 21:45:17.170 [qtp1560940633-18] ResponseDecoratorFilter - Finished applying filter class hellojetty.filter.ResponseDecoratorFilter
[INFO ] 2021-01-25 21:45:17.170 [qtp1560940633-18] BodyRequestLogFilter - Finished applying filter class hellojetty.filter.BodyRequestLogFilter

// /tmp/slf4j_request.log (jetty's request log)
localhost 0:0:0:0:0:0:0:1 - - [2021-01-26T05:45:17.168 Z] "POST /hello HTTP/1.1" 200 15 "-" "PostmanRuntime/7.26.10" 2

// /tmp/request.log (our request log)
localhost 0:0:0:0:0:0:0:1 - - [25/Jan/2021:21:45:17 -0800] "POST /hello HTTP/1.1" 200 - "-" "PostmanRuntime/7.26.10" 0 {"name": "jetty"}
```

### Async GET
```commandline
curl --location --request GET 'localhost:8080/async-hello'

Async GET successful

// console output
[INFO ] 2021-01-25 21:47:20.659 [qtp1560940633-20] AsyncBodyRequestLogFilter - Start applying filter class hellojetty.filter.AsyncBodyRequestLogFilter...
[INFO ] 2021-01-25 21:47:20.659 [qtp1560940633-20] ResponseDecoratorFilter - Start applying filter class hellojetty.filter.ResponseDecoratorFilter...
[INFO ] 2021-01-25 21:47:20.663 [qtp1560940633-20] ResponseDecoratorFilter - Finished applying filter class hellojetty.filter.ResponseDecoratorFilter
[INFO ] 2021-01-25 21:47:20.663 [qtp1560940633-32] AsyncHelloServlet - Start serving async GET request...
[INFO ] 2021-01-25 21:47:20.664 [qtp1560940633-32] AsyncHelloServlet - Finished serving async GET request
[INFO ] 2021-01-25 21:47:20.665 [qtp1560940633-20] AsyncBodyRequestLogFilter - ctx completed, applying filter class hellojetty.filter.AsyncBodyRequestLogFilter$1

// /tmp/slf4j_request.log (jetty's request log)
localhost 0:0:0:0:0:0:0:1 - - [2021-01-26T05:47:20.658 Z] "GET /async-hello HTTP/1.1" 200 20 "-" "PostmanRuntime/7.26.10" 7

// /tmp/request.log (our request log)
localhost 0:0:0:0:0:0:0:1 - - [25/Jan/2021:21:47:20 -0800] "GET /async-hello HTTP/1.1" 200 - "-" "PostmanRuntime/7.26.10" 0
```

### Async POST
```commandline
curl --location --request POST 'localhost:8080/async-hello' \
--header 'Content-Type: application/json' \
--data-raw '{"name": "jetty"}'

Async POST successful

// console output
[INFO ] 2021-01-25 21:50:09.868 [qtp1560940633-16] AsyncBodyRequestLogFilter - Start applying filter class hellojetty.filter.AsyncBodyRequestLogFilter...
[INFO ] 2021-01-25 21:50:09.869 [qtp1560940633-16] ResponseDecoratorFilter - Start applying filter class hellojetty.filter.ResponseDecoratorFilter...
[INFO ] 2021-01-25 21:50:09.869 [qtp1560940633-16] ResponseDecoratorFilter - Finished applying filter class hellojetty.filter.ResponseDecoratorFilter
[INFO ] 2021-01-25 21:50:09.869 [qtp1560940633-26] AsyncHelloServlet - Start serving async POST request...
[INFO ] 2021-01-25 21:50:09.869 [qtp1560940633-26] AsyncHelloServlet - POST body: {"name": "jetty"}
[INFO ] 2021-01-25 21:50:09.871 [qtp1560940633-26] AsyncBodyRequestLogFilter - ctx completed, applying filter class hellojetty.filter.AsyncBodyRequestLogFilter$1
[INFO ] 2021-01-25 21:50:09.872 [qtp1560940633-26] AsyncHelloServlet - Finished serving async POST request

// /tmp/slf4j_request.log (jetty's request log)
localhost 0:0:0:0:0:0:0:1 - - [2021-01-26T05:50:09.868 Z] "POST /async-hello HTTP/1.1" 200 21 "-" "PostmanRuntime/7.26.10" 3

// /tmp/request.log (our request log)
localhost 0:0:0:0:0:0:0:1 - - [25/Jan/2021:21:50:09 -0800] "POST /async-hello HTTP/1.1" 200 - "-" "PostmanRuntime/7.26.10" 0 {"name": "jetty"}
```

## Notes and Discussion

There are several ways to add request logging to Jetty such as `NCSARequestLog`, `Slf4jRequestLog` (older versions) and `CustomRequestLog` (newer versions), or by extending abstract class `AbstractNCSARequestLog` (for example `SampledRequestLog` in this demo project). 

However, there is no good way to log request body for POST, PUT requests, etc.

The root problem is that request input stream can only be consumed once by design. So, if the servlet has read/consumed the request body during request processing, there is no good way to get request body again at a later stage for log processing.
We've tried these things and NONE of them worked for us:

   * *use input stream or reader mark and reset:* mark the input stream or reader the first time the request body is read in servlet handler, at later time reset the stream to read it again in request logger
   * *use a wrapper request to cache request body and use it in another handler:* create a request wrapper to cache request body, then use it in another handler, this does not work either with the same invalid state error
   * *directly log the body when the request input stream is consumed for the first time*: this certainly works, but the implementation is not so elegant like the filter.
   
In the end, we finally got it working using a filter:
   1. add a filter that replaces the request with request wrapper, where request body is cached
   1. pass the wrapped request to the servlet handler, which processes as usual
   1. apply the second part of the filter to read cached request body and log it
   
Note the last step is different for sync and async servlet processing is slightly different (see `BodyRequestLogFilter` and `AsyncBodyRequestLogFilter` in the demo project):

For sync processing, each filter logic can be broken into three parts: pre filter1, pre filter2, ... servlet processing and sending response, post filter1, post filter2...

For async processing, post filter logic needs to be registered with event listeners on `AsyncContext`, otherwise, the execution order will be pre filter1, pre filter2, ... post filter1, post fileter2, ... servlet processing and sending response. In this case, the post filter actions might not be getting the final response it needs to process.
   
## References

   * [embedded jetty](https://www.eclipse.org/jetty/documentation/jetty-9/index.html#embedding-jetty)
   * [servlet filter example](https://www.journaldev.com/1933/java-servlet-filter-example-tutorial)
   * [use HttpServletRequestWrapper to read body twice](https://howtodoinjava.com/java/servlets/httpservletrequestwrapper-example-read-request-body/)
   * [mark and reset input stream to log body not working](https://www.kopis.de/blog/2017/11/02/enable-request-logging-in-jetty/)
   