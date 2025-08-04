package com.carocart.authentication.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.carocart.authentication.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authHeader.substring(7);
        if (!jwtUtil.validateToken(jwtToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        String role = jwtUtil.extractRole(jwtToken);
        String email = jwtUtil.extractUsername(jwtToken);

        if (role == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authToken;

        if ("SYSTEM".equalsIgnoreCase(role)) {
            // Internal Feign client access
            authToken = new UsernamePasswordAuthenticationToken(
                    "internal_system", null,
                    List.of(new SimpleGrantedAuthority("ROLE_SYSTEM")));
        } else {
            // Normal user (ADMIN / USER / VENDOR)
            authToken = new UsernamePasswordAuthenticationToken(
                    email, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        }

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/users/signup") ||
               path.startsWith("/users/login") ||
               path.startsWith("/users/forgot-password") ||
               path.startsWith("/users/verify-otp") ||
               path.startsWith("/users/reset-password") ||
               path.startsWith("/admins/signup") ||
               path.startsWith("/admins/login") ||
               path.startsWith("/vendors/signup") ||
               path.startsWith("/vendors/login") ||
               path.startsWith("/vendors/nearby") ||
               path.startsWith("/actuator");
    }
}
