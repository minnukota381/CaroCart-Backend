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
                .requestMatchers("/orders/debug/token").permitAll()

                .requestMatchers("/orders/orderitems/available-for-delivery")
                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_DELIVERY", "ROLE_DELIVERY_BOY", "ROLE_SYSTEM")
                    .requestMatchers("/orders/orderitems/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_DELIVERY", "ROLE_DELIVERY_BOY", "ROLE_SYSTEM", "ROLE_USER")

                    .requestMatchers("/orders/internal/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_DELIVERY", "ROLE_DELIVERY_BOY", "ROLE_SYSTEM", "ROLE_USER")

                .requestMatchers("/orders/admin/orders/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/orders/admin/vendor-earnings/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/orders/ready-for-delivery")
                
                    .hasAnyAuthority("ROLE_DELIVERY", "ROLE_ADMIN", "ROLE_DELIVERY_BOY")
                .requestMatchers("/orders/admin/vendor-earnings").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/orders/**").authenticated()
                
                .requestMatchers("/orders/admin/pending-items").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/orders/admin/items/**").hasAuthority("ROLE_ADMIN")
                
                .requestMatchers("/orders/vendor/earnings/**")
                .hasAnyAuthority("ROLE_VENDOR")


                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
