package com.carocart.authentication.service;

import com.carocart.authentication.entity.User;
import com.carocart.authentication.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth = super.loadUser(userRequest);
        String email = oauth.getAttribute("email");
        String given = oauth.getAttribute("given_name");
        String family = oauth.getAttribute("family_name");

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Google account does not expose email");
        }

        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isEmpty()) {
            User u = new User();
            u.setEmail(email);
            u.setFirstName(given != null ? given : "CaroCart");
            u.setLastName(family != null ? family : "");
            u.setRole("USER");
            u.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // random, unusable
            userRepository.save(u);
        }

        return oauth; // success handler will mint JWT
    }
}
