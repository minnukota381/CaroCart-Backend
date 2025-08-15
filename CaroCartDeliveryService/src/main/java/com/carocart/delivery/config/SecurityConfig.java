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
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/deliveryboys/register",
                    "/deliveryboys/login",
                    "/deliveryboys/forgot-password",
                    "/deliveryboys/verify-otp",
                    "/deliveryboys/reset-password",
                    "/api/upload",
                    "/api/upload/**"
                ).permitAll()

                .requestMatchers("/deliveryboys/status/**").hasRole("ADMIN")
                .requestMatchers("/deliveryboys/*/status").hasRole("ADMIN")

                .requestMatchers("/deliveryboys/*/location")
                    .hasAnyRole("DELIVERY_BOY", "ADMIN", "SYSTEM")

                .requestMatchers("/deliveryboys/me", "/deliveryboys/me/**")
                    .hasRole("DELIVERY_BOY")

                .requestMatchers("/delivery/orders/**")
                    .hasAnyRole("DELIVERY_BOY", "SYSTEM", "ADMIN")

                .requestMatchers("/deliveryboys/**")
                    .hasAnyRole("ADMIN", "SYSTEM", "DELIVERY_BOY")

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
