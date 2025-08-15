package com.carocart.orderservice.service;

import com.carocart.orderservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

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

        try {
            Claims claims = jwtUtil.getClaims(jwtToken); // alias method below
            String role = claims.get("role", String.class);
            Long userId = jwtUtil.extractUserId(jwtToken);
            String subject = claims.getSubject();

            if (role == null) {
                filterChain.doFilter(request, response);
                return;
            }

            String grantedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            
            Object principal = (userId != null) ? userId : subject;

            var authToken = new UsernamePasswordAuthenticationToken(
                    principal, null,
                    List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(grantedRole))
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception ignored) {}

        filterChain.doFilter(request, response);
    }
}
