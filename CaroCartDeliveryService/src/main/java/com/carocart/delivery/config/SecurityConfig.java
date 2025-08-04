package com.carocart.delivery.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {}) // Enable CORS
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers(
                    "/deliveryboys/register",
                    "/deliveryboys/login",
                    "/deliveryboys/forgot-password",
                    "/deliveryboys/verify-otp",
                    "/deliveryboys/reset-password",
                    "/api/upload"
                ).permitAll()

                // Admin-only endpoints
                .requestMatchers("/deliveryboys/status/**").hasRole("ADMIN")
                .requestMatchers("/deliveryboys/{id}/status").hasRole("ADMIN")

                // Vendor and System calls to fetch delivery boy location
                .requestMatchers("/deliveryboys/{id}/location").hasAnyRole("DELIVERY_BOY", "ADMIN", "SYSTEM")

                // Delivery boy's private data (JWT secured)
                .requestMatchers("/deliveryboys/me", "/deliveryboys/me/**", "/delivery/orders/**")
                    .hasRole("DELIVERY_BOY")

                // Internal Feign call by OrderService or Admin
                .requestMatchers("/deliveryboys/{id}").hasAnyRole("ADMIN", "SYSTEM", "DELIVERY_BOY")

                // All other endpoints must be authenticated
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                )
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
