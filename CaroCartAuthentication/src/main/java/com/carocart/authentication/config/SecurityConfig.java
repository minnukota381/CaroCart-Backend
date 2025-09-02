package com.carocart.authentication.config;

import com.carocart.authentication.entity.User;
import com.carocart.authentication.repository.UserRepository;
import com.carocart.authentication.service.CustomOAuth2UserService;
import com.carocart.authentication.service.JwtAuthenticationFilter;
import com.carocart.authentication.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest; // ← Jakarta import
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.URI;
import java.util.Optional;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final Environment env;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserRepository userRepository,
                          JwtUtil jwtUtil,
                          Environment env,
                          @Lazy CustomOAuth2UserService customOAuth2UserService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.env = env;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()

                // INTERNAL (SYSTEM)
                .requestMatchers(HttpMethod.PATCH, "/users/internal/**").hasRole("SYSTEM")

                // Public auth endpoints
                .requestMatchers(
                    "/admins/signup", "/admins/login",
                    "/users/signup", "/users/login",
                    "/users/forgot-password", "/users/verify-otp", "/users/reset-password",
                    "/vendors/signup/**", "/vendors/signup/request-otp", "/vendors/login",
                    "/vendors/nearby", "/vendors/nearby/**",
                    "/vendors/forgot-password", "/vendors/verify-otp", "/vendors/reset-password",
                    "/oauth2/**", "/login/oauth2/**",
                    "/users/phone/**",
                    "/vendors/{id}"
                ).permitAll()

                // Admin-only
                .requestMatchers("/admins/me").hasRole("ADMIN")
                .requestMatchers("/users/admin/users/**").hasRole("ADMIN")
                .requestMatchers("/vendors/admin/**").hasRole("ADMIN")
                .requestMatchers("/vendors/admin/vendors/count").hasRole("ADMIN")

                // User access
                .requestMatchers("/users/me", "/users/me/location").authenticated()
                .requestMatchers("/users/{id}").hasAnyRole("USER","ADMIN","SYSTEM")
                .requestMatchers("/users/**").hasAnyRole("USER","ADMIN","SYSTEM")

                // Vendor access
                .requestMatchers("/vendors/me").hasAnyRole("VENDOR","ADMIN")
                .requestMatchers("/vendors/{id}").hasAnyRole("VENDOR","ADMIN","SYSTEM")
                .requestMatchers("/vendors/email/**").hasAnyRole("VENDOR","ADMIN","SYSTEM")

                // Profile images
                .requestMatchers("/admins/profile/**","/users/profile/**").authenticated()

                // Catch-all
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // We keep IF_REQUIRED to allow the short-lived session needed for OAuth2 login
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())
            // ✅ Enable OAuth2 login with dynamic (env-based) redirects
            .oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler((request, response, authentication) -> {
                    var oAuth2User = (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
                    String email = oAuth2User.getAttribute("email");

                    Optional<User> userOpt = userRepository.findByEmail(email);
                    if (userOpt.isEmpty()) {
                        String failureRedirect = env.getProperty(
                                "app.oauth.failure-redirect",
                                defaultFrontend(request) + "/oauth-failed"
                        );
                        response.sendRedirect(failureRedirect + "?reason=user_not_found");
                        return;
                    }

                    User user = userOpt.get();
                    String jwt = jwtUtil.generateToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole(),
                        user.getFirstName(),
                        user.getLastName()
                    );

                    String successRedirect = env.getProperty(
                            "app.oauth.success-redirect",
                            defaultFrontend(request) + "/oauth-success"
                    );
                    response.sendRedirect(successRedirect + "?token=" + jwt);
                })
                .failureHandler((request, response, exception) -> {
                    String failureRedirect = env.getProperty(
                            "app.oauth.failure-redirect",
                            defaultFrontend(request) + "/oauth-failed"
                    );
                    response.sendRedirect(failureRedirect + "?reason=" + exception.getClass().getSimpleName());
                })
            );

        return http.build();
    }

    /**
     * Picks a sensible default frontend base URL when not explicitly configured:
     * 1) If APP_FRONTEND_BASE_URL is set, use it.
     * 2) Else, try to infer from the request's Host header & scheme.
     * 3) Fallback to "http://localhost:5173".
     */
    private String defaultFrontend(HttpServletRequest request) {
        // Highest priority: explicit property (works for prod/staging/dev)
        String configured = env.getProperty("app.frontend.base-url");
        if (configured != null && !configured.isBlank()) {
            return stripTrailingSlash(configured);
        }

        // Next: infer from request (useful behind reverse proxies / same domain deployments)
        try {
            String scheme = request.getHeader("X-Forwarded-Proto");
            if (scheme == null || scheme.isBlank()) {
                scheme = request.getScheme(); // http/https
            }
            String host = request.getHeader("X-Forwarded-Host");
            if (host == null || host.isBlank()) {
                host = request.getHeader("Host");
            }
            if (host != null && !host.isBlank()) {
                URI inferred = new URI(scheme == null ? "http" : scheme, null, host, -1, null, null, null);
                return stripTrailingSlash(inferred.toString());
            }
        } catch (Exception ignored) {}

        // Dev fallback
        return "http://localhost:5173";
    }

    private String stripTrailingSlash(String url) {
        return (url != null && url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
