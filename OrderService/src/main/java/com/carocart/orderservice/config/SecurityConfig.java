package com.carocart.orderservice.config;

import com.carocart.orderservice.service.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and().csrf().disable()
            .authorizeHttpRequests(authz -> authz
                // Public endpoint
                .requestMatchers("/orders/debug/token").permitAll()
                // Admin endpoints
                .requestMatchers("/orders/admin/orders/**").hasAuthority("ROLE_ADMIN")
                // Delivery endpoints
                .requestMatchers("/orders/ready-for-delivery").hasAnyAuthority("ROLE_DELIVERY", "ROLE_ADMIN")
                // All other order endpoints require authentication
                .requestMatchers("/orders/**").authenticated()
                .requestMatchers(
                	    "/swagger-ui.html",
                	    "/swagger-ui/**",
                	    "/v3/api-docs",
                	    "/v3/api-docs/**"
                	).permitAll()

                // All else
                .anyRequest().permitAll()
            )
            // Add JWT authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bean for AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
