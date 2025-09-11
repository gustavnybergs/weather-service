package com.grupp3.weather.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.api-key}")
    private String expected;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        String path = req.getRequestURI();
        boolean isWrite = !HttpMethod.GET.matches(req.getMethod());
        boolean isFavoritesEndpoint = path.startsWith("/favorites");

        // Läsning (GET) tillåts utan nyckel, samt favorites-endpoints
        if (isWrite && !isFavoritesEndpoint) {
            String provided = req.getHeader("X-API-KEY");
            if (provided == null || !provided.equals(expected)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                return;
            }
        }

        chain.doFilter(req, res);
    }
}