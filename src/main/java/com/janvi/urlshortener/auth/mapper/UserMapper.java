package com.janvi.urlshortener.auth.mapper;

import com.janvi.urlshortener.auth.dto.RegisterResponse;
import com.janvi.urlshortener.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public RegisterResponse toRegisterResponse(User user) {
        return RegisterResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}