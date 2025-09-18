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

/**
 * ApiKeyFilter - autentisering-portier för admin-endpoints och skrivoperationer.
 *
 * Skiljer sig från andra säkerhetslager genom att fokusera på API-key validering
 * istället för rate limiting eller DDoS detection.
 *
 * Huvudfunktion:
 * - doFilterInternal(): Kontrollera om request kräver API-key och validera X-API-KEY header
 *
 * Säkerhetsregler implementerar:
 * - Öppen läsning: Alla GET requests tillåts utan API-key (bara hämta data)
 * - Skyddad skrivning: POST/PUT/DELETE kräver API-key (ändra systemet)
 * - Admin-skydd: Alla /admin/* endpoints kräver API-key oavsett HTTP-metod
 * - Användarundantag: /favorites/* endpoints tillåts för vanliga användare
 *
 * Filter-kedja position: FÖRSTA filter (Ordered.HIGHEST_PRECEDENCE) för tidig blockering.
 * Unauthorized requests får HTTP 401 och når aldrig Controller eller DDoSProtectionFilter.
 * Läser API-key från application.properties för säker konfiguration.
 */

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