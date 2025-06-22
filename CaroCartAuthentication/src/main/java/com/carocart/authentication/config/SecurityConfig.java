package com.carocart.authentication.config;

import com.carocart.authentication.entity.User;
import com.carocart.authentication.repository.UserRepository;
import com.carocart.authentication.service.JwtAuthenticationFilter;
import com.carocart.authentication.util.JwtUtil;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
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
                // your permitAll and role-based matchers (unchanged)
                .requestMatchers(
                    "/admins/signup",
                    "/admins/login",
                    "/users/login",
                    "/users/signup",
                    "/vendors/signup/**",
                    "/vendors/login"
                ).permitAll()
                .requestMatchers("/admins/me").hasRole("ADMIN")
                .requestMatchers("/users/admin/users/all").hasRole("ADMIN")
                .requestMatchers("/users/admin/users/{id}").hasRole("ADMIN")
                .requestMatchers("/users/{id}").hasAnyRole("USER", "ADMIN")

                .requestMatchers("/vendors/admin/**").hasRole("ADMIN")

                .requestMatchers(
                    "/admins/profile/upload-image",
                    "/admins/profile/image",
                    "/admins/profile",
                    "/admins/me"
                ).hasRole("ADMIN")
                .requestMatchers(
                    "/users/profile/upload-image",
                    "/users/profile/image",
                    "/users/profile",
                    "/users/me"
                ).authenticated()
                .requestMatchers("/api/addresses/**").permitAll() // or authenticated()
                .requestMatchers("/vendors/me").authenticated()
                .anyRequest().authenticated()
            )
            
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler())
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // ✅ FIXED
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler oAuth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");

            Optional<User> userOpt = userRepository.findByEmail(email);
            String jwt = jwtUtil.generateToken(
                email,
                userOpt.map(User::getRole).orElse("USER"),
                userOpt.map(User::getFirstName).orElse(""),
                userOpt.map(User::getLastName).orElse("")
            );

            // Redirect with JWT to frontend
            response.sendRedirect("http://localhost:5173/oauth-success?token=" + jwt);
        };
    }
}
