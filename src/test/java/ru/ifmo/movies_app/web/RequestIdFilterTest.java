package ru.ifmo.movies_app.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void addsGeneratedRequestIdToResponseAndMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/movies");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                mdcValueInsideChain.set(MDC.get("requestId")));

        assertThat(response.getHeader(RequestIdFilter.HEADER_NAME)).isNotBlank();
        assertThat(mdcValueInsideChain.get()).isEqualTo(response.getHeader(RequestIdFilter.HEADER_NAME));
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void reusesIncomingRequestIdAfterTrimming() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/movies");
        request.addHeader(RequestIdFilter.HEADER_NAME, "  qa-trace-42  ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        });

        assertThat(response.getHeader(RequestIdFilter.HEADER_NAME)).isEqualTo("qa-trace-42");
    }

    @Test
    void replacesUnsafeIncomingRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/movies");
        request.addHeader(RequestIdFilter.HEADER_NAME, "bad\r\nvalue");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        });

        assertThat(response.getHeader(RequestIdFilter.HEADER_NAME))
                .isNotBlank()
                .isNotEqualTo("bad\r\nvalue");
    }
}
