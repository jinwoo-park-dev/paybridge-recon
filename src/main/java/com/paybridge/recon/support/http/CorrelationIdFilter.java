package com.paybridge.recon.support.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = CorrelationIdSupport.resolveOrGenerate(request.getHeader(CorrelationIdSupport.HEADER_NAME));
        CorrelationIdSupport.bind(correlationId);
        response.setHeader(CorrelationIdSupport.HEADER_NAME, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            CorrelationIdSupport.clear();
        }
    }
}
