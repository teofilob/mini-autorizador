package br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final Pattern VALID_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String correlationId = getOrCreateCorrelationId(request.getHeader(CORRELATION_ID_HEADER));
        long startTime = System.nanoTime();

        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        try {
            filterChain.doFilter(request, response);
            LOGGER.info("http.request.completed method={} status={} durationMs={}",
                    request.getMethod(), response.getStatus(), durationMillis(startTime));
        } catch (IOException | ServletException | RuntimeException | Error ex) {
            LOGGER.error("http.request.failed method={} status={} durationMs={}",
                    request.getMethod(), response.getStatus(), durationMillis(startTime), ex);
            throw ex;
        } finally {
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    private long durationMillis(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    private String getOrCreateCorrelationId(String requestCorrelationId) {
        if (requestCorrelationId != null && VALID_CORRELATION_ID.matcher(requestCorrelationId).matches()) {
            return requestCorrelationId;
        }
        return UUID.randomUUID().toString();
    }
}
