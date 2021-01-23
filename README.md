# A Demo Project to Explore Embedded Jetty

## How to Run
```commandline
$./gradlew build run

[INFO ] 2021-01-12 14:16:30.809 [main] ServerMain - server starting...
[INFO ] 2021-01-12 14:16:30.836 [main] log - Logging initialized @534ms to org.eclipse.jetty.util.log.Slf4jLog
[INFO ] 2021-01-12 14:16:30.909 [main] Server - jetty-9.4.6.v20170531
[INFO ] 2021-01-12 14:16:30.946 [main] session - DefaultSessionIdManager workerName=node0
[INFO ] 2021-01-12 14:16:30.946 [main] session - No SessionScavenger set, using defaults
[INFO ] 2021-01-12 14:16:30.948 [main] session - Scavenging every 660000ms
[INFO ] 2021-01-12 14:16:30.953 [main] SampledRequestLogFilter - Filter init
[INFO ] 2021-01-12 14:16:30.954 [main] ContextHandler - Started o.e.j.s.ServletContextHandler@15eb5ee5{/,null,AVAILABLE}
[INFO ] 2021-01-12 14:16:30.986 [main] AbstractConnector - Started ServerConnector@55b7a4e0{HTTP/1.1,[http/1.1]}{0.0.0.0:8080}
[INFO ] 2021-01-12 14:16:30.987 [main] Server - Started @685ms
[INFO ] 2021-01-12 14:16:30.987 [main] ServerMain - server started...
```

## Sample Requests
### GET
```commandline
$curl --location --request GET 'localhost:8080/hello/'

GET Success

[INFO ] 2021-01-12 14:18:58.794 [qtp393040818-19] SampledRequestLogFilter - Start applying filter...
[INFO ] 2021-01-12 14:18:58.794 [qtp393040818-19] SampledRequestLogFilter - BODY from filter: 
[INFO ] 2021-01-12 14:18:58.794 [qtp393040818-19] HelloServlet - Start serving GET request...
[INFO ] 2021-01-12 14:18:58.795 [qtp393040818-19] HelloServlet - Finished serving GET request
[INFO ] 2021-01-12 14:18:58.795 [qtp393040818-19] SampledRequestLogFilter - Finished applying filter
[INFO ] 2021-01-12 14:18:58.795 [qtp393040818-19] RequestLog - localhost 0:0:0:0:0:0:0:1 - - [2021-01-12T22:18:58.794 Z] "GET /hello/ HTTP/1.1" 200 11 "-" "PostmanRuntime/7.26.8" 1
```
### POST
```commandline
$curl --location --request POST 'localhost:8080/hello/' \
 --header 'Content-Type: text/plain' \
 --data-raw 'Hello'

POST Success

[INFO ] 2021-01-12 14:45:16.742 [qtp393040818-18] SampledRequestLogFilter - Start applying filter...
[INFO ] 2021-01-12 14:45:16.742 [qtp393040818-18] HelloServlet - Start serving POST request
[INFO ] 2021-01-12 14:45:16.743 [qtp393040818-18] HelloServlet - POST body: Hello
[INFO ] 2021-01-12 14:45:16.743 [qtp393040818-18] HelloServlet - Finished serving POST request
[INFO ] 2021-01-12 14:45:16.743 [qtp393040818-18] SampledRequestLogFilter - BODY from filter: Hello
[INFO ] 2021-01-12 14:45:16.743 [qtp393040818-18] SampledRequestLogFilter - Finished applying filter
[INFO ] 2021-01-12 14:45:16.744 [qtp393040818-18] RequestLog - localhost 0:0:0:0:0:0:0:1 - - [2021-01-12T22:45:16.742 Z] "POST /hello/ HTTP/1.1" 200 12 "-" "PostmanRuntime/7.26.8" 2
[INFO ] 2021-01-12 14:45:16.744 [qtp393040818-18] SampledRequestLog - Sampling request, doing nothing
```

## Problems We Explored

### Log Request Body
There are several ways to add request logging to Jetty such as `Slf4jRequestLog`, `NCSARequestLog`, or by extending abstract class `AbstractNCSARequestLog` (for example `SampledRequestLog` in this demo project).
However, there is no good way to add request log support for requests with body such as POST, PUT, etc.

One problem is that request input stream can only be consumed once by design. So, if the servlet has got the request body, there is no good way to get request body again at a later stage of the request processing.
We've tried these things and NONE of them worked for us:

   * *use input stream or reader mark and reset:* mark the input stream or reader the first time the request body is read in servlet handler, at later time reset the stream to read it again in request logger
   * *use a wrapper request to cache request body and use it in another handler:* create a request wrapper to cache request body, then use it in another handler, this does not work either with the same invalid state error
   
In the end, we finally got it working using a filter:
   1. add a filter that replaces the request with request wrapper, where request body is cached
   1. pass the wrapped request to the servlet handler, which processes as usual
   1. apply the second part of the filter to read cached request body and log it

## Notes

   1. Filter `init` is called during server start when the filter is registered
   1. Inside a filter `doFilter` method, there are two parts: the part before calling `chain.doFilter(request, response);` is invoked before a request is processed by a servlet handler. The part after `chain.doFilter(request, response);` is processed after a servlet handler.
   1. Handlers are invoked in the order of registration with the server, e.g. in our example in `ServerMain`, we add `ServletContextHandler` to process servlet request, then request log, and finally sampled request log.
   
## References

   * https://www.eclipse.org/jetty/documentation/jetty-9/index.html#embedding-jetty
   * https://www.journaldev.com/1933/java-servlet-filter-example-tutorial
   * (mark and reset to log body not working) https://www.kopis.de/blog/2017/11/02/enable-request-logging-in-jetty/
   * (also not working if the request input stream has been consumed already before passing to filter chain) https://howtodoinjava.com/java/servlets/httpservletrequestwrapper-example-read-request-body/
   