package com.carocart.authentication.config;

import com.carocart.authentication.entity.User;
import com.carocart.authentication.repository.UserRepository;
import com.carocart.authentication.service.JwtAuthenticationFilter;
import com.carocart.authentication.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
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
    private final Environment env; // ✅ inject this

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            UserRepository userRepository,
            JwtUtil jwtUtil,
            Environment env // ✅ here too
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.env = env;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            		
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
             // --- INTERNAL (SYSTEM) ---
                .requestMatchers(HttpMethod.PATCH, "/internal/users/**").hasRole("SYSTEM")
                .requestMatchers("/users/me", "/users/me/location").authenticated()

                // Public Auth Endpoints
                .requestMatchers(
                    "/admins/signup", "/admins/login",
                    "/users/signup", "/users/login",
                    "/users/forgot-password",   // <-- add
                    "/users/verify-otp",        // <-- add
                    "/users/reset-password",    // <-- add
                    "/vendors/signup/**","/vendors/signup/request-otp", "/vendors/login",
                    "/vendors/nearby", "/vendors/nearby/**",
                    "/vendors/forgot-password",   // <-- add
                    "/vendors/verify-otp",        // <-- add
                    "/vendors/reset-password" ,
                    "/vendors/{id}",
                    "/vendors/with-eta",
                    "vendors//by-ids"
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

             // OAuth endpoints MUST be public
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/users/internal/**").hasRole("SYSTEM")

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
            .formLogin(form -> form.disable())

        // OAuth2 login: on success -> mint JWT -> redirect to frontend with token
        .oauth2Login(oauth -> oauth
            .successHandler((request, response, authentication) -> {
                var oAuth2User = (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
                String email = oAuth2User.getAttribute("email");
                String firstName = oAuth2User.getAttribute("given_name");
                String lastName = oAuth2User.getAttribute("family_name");

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

                    String successRedirect = env.getProperty("app.oauth.success-redirect", "http://localhost:5173/oauth-success");
                    response.sendRedirect(successRedirect + "?token=" + jwt);
                } else {
                    // If you want auto-provisioning, create a user here instead of failing.
                    String failureRedirect = env.getProperty("app.oauth.failure-redirect", "http://localhost:5173/oauth-failed");
                    response.sendRedirect(failureRedirect + "?reason=user_not_found");
                }
            })
            .failureHandler((request, response, exception) -> {
                String failureRedirect = env.getProperty("app.oauth.failure-redirect", "http://localhost:5173/oauth-failed");
                response.sendRedirect(failureRedirect + "?reason=" + exception.getClass().getSimpleName());
            })
        );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}