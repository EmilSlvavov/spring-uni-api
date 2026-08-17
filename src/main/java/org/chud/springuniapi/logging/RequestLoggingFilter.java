package org.chud.springuniapi.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

//This class writes a log for every HTTP request that reaches the app
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)   // just after CorrelationFilter
public class RequestLoggingFilter extends OncePerRequestFilter {

    //The logger doing the actual work
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /**
     * Bytes the wrapper keeps for us. Anything past this is still delivered to the
     * controller, it just is not cached - so it is also the most we can ever log.
     */

    //request bodies arrive as streams. You read it once. Jackson reads it and builds the request.
    //We cache(copy) the first X bytes and bytes after that the rest still reach Jackson, we just dont cache them.
    private static final int CACHE_LIMIT = 1024;


    //doFilterInternal is the method OncePerRequestFilter calls once it verifies that this is a new request
    //using NonNull annotation to make sure method arguments are never null
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        //the copy discussed earlier
        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, CACHE_LIMIT);
        long start = System.nanoTime(); //measuring elapsed time

        try {
            filterChain.doFilter(wrapped, response); //we pass wrapped, not request to the controller. Giving the request would nullify the copy
        } finally {
            long millis = (System.nanoTime() - start) / 1_000_000; //log the request even if it failed with ms (we divide nano into ms)
            log.info("{} {}{} -> {} ({}ms){}", //{} are placeholders for the values later written
                    wrapped.getMethod(), //log method
                    wrapped.getRequestURI(), //log uri
                    wrapped.getQueryString() == null ? "" : "?" + wrapped.getQueryString(), //log params if they exist
                    response.getStatus(), //log status
                    millis, //log the elapsed time
                    body(wrapped));
        }
    }


    //
    private String body(ContentCachingRequestWrapper request) {
        byte[] bytes = request.getContentAsByteArray(); //get whatever wrapper cached in bytes
        if (bytes.length == 0) { //check for empty since get and delete have no body
            return "";
        }


        //get the request content type and check if its null or doesnt contain a json. We dothat
        // because things like pngs would get corrupted if we just print the bytes. and we will only log the type instead of content
        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains("json")) {
            return " body=<" + contentType + ">";
        }

        //We turn the bytes to text
        String body = new String(bytes, StandardCharsets.UTF_8).replaceAll("\\s+", " ");

        // getContentLength() is the real size; bytes.length is what the wrapper kept.
        // If they differ, the body was longer than CACHE_LIMIT and we only have the head.
        if (request.getContentLength() > bytes.length) {
            body = body + "...";
        }

        //return body after it was masked
        return " body=" + mask(body);
    }


    //mask all the email password and token fields with *****
    //So {"name":"Ana","email":"ana@uni.bg"} becomes {"name":"Ana","email":"***"}.
    private String mask(String body) {
        return body.replaceAll("(?i)(\"(?:email|password|token)\"\\s*:\\s*\")[^\"]*(\")", "$1***$2");
    }


    //makes sure you do not filter requests if the uri starts  with actuator, equals /error or /favicon.ico method was suggested but right now
    //it isnt functional
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String uri = request.getRequestURI();
//        return uri.startsWith("/actuator") || uri.equals("/error") || uri.equals("/favicon.ico");
//    }
}