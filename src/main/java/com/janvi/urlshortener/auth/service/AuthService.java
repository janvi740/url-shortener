package com.janvi.urlshortener.auth.service;

import com.janvi.urlshortener.auth.dto.RegisterRequest;
import com.janvi.urlshortener.auth.dto.RegisterResponse;
import com.janvi.urlshortener.auth.mapper.UserMapper;
import com.janvi.urlshortener.user.entity.User;
import com.janvi.urlshortener.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        return userMapper.toRegisterResponse(savedUser);
    }
}