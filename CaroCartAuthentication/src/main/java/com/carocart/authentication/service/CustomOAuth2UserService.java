package com.carocart.authentication.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.carocart.authentication.entity.User;
import com.carocart.authentication.repository.UserRepository;
import com.carocart.authentication.util.JwtUtil;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        String email = user.getAttribute("email");

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName(user.getAttribute("given_name"));
            newUser.setLastName(user.getAttribute("family_name"));
            newUser.setPassword(""); // Leave blank or random
            newUser.setRole("USER");
            userRepository.save(newUser);
        }

        return user;
    }
}
