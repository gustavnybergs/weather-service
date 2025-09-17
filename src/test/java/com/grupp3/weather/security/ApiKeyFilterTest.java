package com.grupp3.weather.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyFilterTest {

    @InjectMocks
    private ApiKeyFilter apiKeyFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private static final String VALID_API_KEY = "topsecret123";

    @BeforeEach
    void setUp() {
        // Sätt API-nyckeln via reflection eftersom @Value inte fungerar i unit tests
        ReflectionTestUtils.setField(apiKeyFilter, "expected", VALID_API_KEY);
    }

    @Test
    @DisplayName("GET requests ska tillåtas utan API-nyckel")
    void getRequests_ShouldBeAllowedWithoutApiKey() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/weather/Stockholm");

        // Act
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("Favorites endpoints ska tillåtas utan API-nyckel även för POST/PUT/DELETE")
    void favoritesEndpoints_ShouldBeAllowedWithoutApiKey() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/favorites/Stockholm");

        // Act
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("POST requests med giltig API-nyckel ska tillåtas")
    void postRequests_WithValidApiKey_ShouldBeAllowed() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/admin/alerts");
        when(request.getHeader("X-API-KEY")).thenReturn(VALID_API_KEY);

        // Act
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("POST requests utan API-nyckel ska blockeras")
    void postRequests_WithoutApiKey_ShouldBeBlocked() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/admin/alerts");
        when(request.getHeader("X-API-KEY")).thenReturn(null);

        // Act
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("PUT requests med ogiltig API-nyckel ska blockeras")
    void putRequests_WithInvalidApiKey_ShouldBeBlocked() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/admin/alerts/1");
        when(request.getHeader("X-API-KEY")).thenReturn("wrongkey");

        // Act
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("DELETE requests med giltig API-nyckel ska tillåtas")
    void deleteRequests_WithValidApiKey_ShouldBeAllowed() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/admin/alerts/1");
        when(request.getHeader("X-API-KEY")).thenReturn(VALID_API_KEY);

        // Act
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("PATCH requests utan API-nyckel ska blockeras")
    void patchRequests_WithoutApiKey_ShouldBeBlocked() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("PATCH");
        when(request.getRequestURI()).thenReturn("/admin/weather/update");
        when(request.getHeader("X-API-KEY")).thenReturn(null);

        // Act
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Case-sensitive API-nyckel kontroll")
    void apiKey_ShouldBeCaseSensitive() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/admin/alerts");
        when(request.getHeader("X-API-KEY")).thenReturn("TOPSECRET123"); // Versaler

        // Act
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Tom API-nyckel ska blockeras")
    void emptyApiKey_ShouldBeBlocked() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/admin/alerts");
        when(request.getHeader("X-API-KEY")).thenReturn("");

        // Act
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }
}