package com.carocart.authentication.config;

import com.carocart.authentication.repository.UserRepository;
import com.carocart.authentication.service.JwtAuthenticationFilter;
import com.carocart.authentication.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserRepository userRepository, JwtUtil jwtUtil) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public Health & Docs
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()

                // Public Auth Endpoints
                .requestMatchers(
                    "/admins/signup", "/admins/login",
                    "/users/signup", "/users/login",
                    "/vendors/signup/**", "/vendors/login",
                    "/vendors/nearby", "/vendors/nearby/**"
                ).permitAll()

                // --- ADMIN ONLY ---
                .requestMatchers("/admins/me").hasRole("ADMIN")
                .requestMatchers("/users/admin/users/**").hasRole("ADMIN")
                .requestMatchers("/vendors/admin/**").hasRole("ADMIN")
                .requestMatchers("/vendors/admin/vendors/count").hasRole("ADMIN")

                // --- USER ACCESS ---
                .requestMatchers("/users/me", "/users/me/update-location").authenticated()
                .requestMatchers("/users/{id}").hasAnyRole("USER", "ADMIN", "SYSTEM")
                .requestMatchers("/users/**").hasAnyRole("USER", "ADMIN", "SYSTEM")

                // --- VENDOR ACCESS ---
                .requestMatchers("/vendors/me").hasAnyRole("VENDOR", "ADMIN")
                .requestMatchers("/vendors/{id}").hasAnyRole("VENDOR", "ADMIN", "SYSTEM")
                .requestMatchers("/vendors/email/**").hasAnyRole("VENDOR", "ADMIN", "SYSTEM")

                // --- PROFILE Images ---
                .requestMatchers(
                    "/admins/profile/**",
                    "/users/profile/**"
                ).authenticated()

                // --- Address Service (Public/Internal) ---
                .requestMatchers("/api/addresses/**").permitAll()

                // --- Catch All ---
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Uncomment and configure if using OAuth2 login in future
    /*
    @Bean
    public AuthenticationSuccessHandler oAuth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");

            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String jwt = jwtUtil.generateToken(
                    user.getId(),
                    user.getEmail(),
                    user.getRole(),
                    user.getFirstName(),
                    user.getLastName()
                );
                response.sendRedirect("http://localhost:5173/oauth-success?token=" + jwt);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            }
        };
    }
    */
}
