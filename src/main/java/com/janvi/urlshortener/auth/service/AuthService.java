package com.janvi.urlshortener.auth.service;

import com.janvi.urlshortener.auth.dto.RegisterRequest;
import com.janvi.urlshortener.auth.dto.RegisterResponse;
import com.janvi.urlshortener.auth.mapper.UserMapper;
import com.janvi.urlshortener.common.exception.DuplicateResourceException;
import com.janvi.urlshortener.user.entity.User;
import com.janvi.urlshortener.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.janvi.urlshortener.auth.dto.LoginRequest;
import com.janvi.urlshortener.auth.dto.LoginResponse;
import com.janvi.urlshortener.common.exception.BadRequestException;
import com.janvi.urlshortener.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        return userMapper.toRegisterResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .build();
    }
}