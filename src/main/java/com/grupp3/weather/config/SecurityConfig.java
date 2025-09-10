package com.grupp3.weather.config;

import com.grupp3.weather.security.ApiKeyFilter;
import com.grupp3.weather.security.DDoSProtectionFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistration(ApiKeyFilter filter) {
        FilterRegistrationBean<ApiKeyFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(filter);
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);     // Kör först
        return reg;
    }

    @Bean
    public FilterRegistrationBean<DDoSProtectionFilter> ddosFilterRegistration(DDoSProtectionFilter filter) {
        FilterRegistrationBean<DDoSProtectionFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(filter);
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 1); // Kör efter API key filter
        return reg;
    }
}