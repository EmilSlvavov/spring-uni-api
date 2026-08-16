package org.chud.springuniapi.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

//The job of this class is to give every request a random id so all logs by this request are under
// the same id making it easier to find them
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) //Filters run in an order. HIGHEST_PRECEDENCE makes it so it runs first before everything else
// A normal filter can run multiple times per request thats why we extend once per request filter.
public class CorrelationFilter extends OncePerRequestFilter {


    //TRACE_ID is the key where the id for the log is stored. It's public so the handler can
    // use the same constant instead of making a new traceId
    public static final String TRACE_ID = "traceId";

    //X-Trace-Id is the http header name
    private static final String TRACE_ID_HEADER = "X-Trace-Id";


    //doFilterInternal is the method OncePerRequestFilter calls once it verifies that this is a new request
    //using NonNull annotation to make sure method arguments are never null
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        //creates traceId
        String traceId = resolveTraceId(request);

        // mdc - short for mapped diagnostic context. Its a Map<String, String> which is
        // thread local(every thread has its own). We put the traceId in the map with a key being the constant
        //and we can .get() to get the id when creating a log in the handler for example
        MDC.put(TRACE_ID, traceId);

        //we set the response header from the new request to have TRACE_ID_HEADER to = traceId
        response.setHeader(TRACE_ID_HEADER, traceId);


        // doFilter hands control to the next filter. everything after doFilter happens on the way out with the response
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID); //remove so next request does not share an id with the current one
        }
    }


    //if the caller already set an id reuse it again, check by length and regex for only letters/digits/hyphens
    private String resolveTraceId(HttpServletRequest request) {
        String incoming = request.getHeader(TRACE_ID_HEADER);

        if (incoming != null && incoming.length() <= 64 && incoming.matches("[A-Za-z0-9-]+")) {
            return incoming;
        }

        //return random uuid 
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
