package br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CorrelationIdFilterTest {

    private static final String CORRELATION_ID = "test-correlation-id";

    private final TestableCorrelationIdFilter filter = new TestableCorrelationIdFilter();

    @AfterEach
    void limparMdc() {
        MDC.remove(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
    }

    @Test
    void devePropagarIOExceptionELimparMdc() {
        IOException failure = new IOException("I/O failure");
        var request = request();
        var response = new MockHttpServletResponse();

        IOException thrown = assertThrows(IOException.class,
                () -> filter.filter(request, response, (req, res) -> {
                    throw failure;
                }));

        assertSame(failure, thrown);
        assertFailureCleanup(response);
    }

    @Test
    void devePropagarServletExceptionELimparMdc() {
        ServletException failure = new ServletException("Servlet failure");
        var request = request();
        var response = new MockHttpServletResponse();

        ServletException thrown = assertThrows(ServletException.class,
                () -> filter.filter(request, response, (req, res) -> {
                    throw failure;
                }));

        assertSame(failure, thrown);
        assertFailureCleanup(response);
    }

    @Test
    void devePropagarRuntimeExceptionELimparMdc() {
        RuntimeException failure = new IllegalStateException("Runtime failure");
        var request = request();
        var response = new MockHttpServletResponse();

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> filter.filter(request, response, (req, res) -> {
                    throw failure;
                }));

        assertSame(failure, thrown);
        assertFailureCleanup(response);
    }

    @Test
    void devePropagarErrorELimparMdc() {
        Error failure = new AssertionError("Error failure");
        var request = request();
        var response = new MockHttpServletResponse();

        Error thrown = assertThrows(Error.class,
                () -> filter.filter(request, response, (req, res) -> {
                    throw failure;
                }));

        assertSame(failure, thrown);
        assertFailureCleanup(response);
    }

    private MockHttpServletRequest request() {
        var request = new MockHttpServletRequest("GET", "/test");
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, CORRELATION_ID);
        return request;
    }

    private void assertFailureCleanup(MockHttpServletResponse response) {
        assertEquals(CORRELATION_ID, response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    }

    private static class TestableCorrelationIdFilter extends CorrelationIdFilter {
        void filter(
                MockHttpServletRequest request,
                MockHttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            doFilterInternal(request, response, filterChain);
        }
    }
}
