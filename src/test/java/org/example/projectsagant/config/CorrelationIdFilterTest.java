package org.example.projectsagant.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorrelationIdFilterTest {

    @Test
    void doFilter_siNoHayHeader_deberiaGenerarUnoYPropagarlo() throws Exception {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response).setHeader(eq(CorrelationIdFilter.CORRELATION_ID_HEADER), any(String.class));
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull(); // se limpia al terminar
    }

    @Test
    void doFilter_siVieneElHeader_deberiaReusarlo() throws Exception {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn("abc-123");

        filter.doFilter(request, response, chain);

        verify(response).setHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "abc-123");
    }
}