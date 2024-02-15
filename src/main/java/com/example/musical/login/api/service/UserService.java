package com.example.musical.login.api.service;

import com.example.musical.login.api.repository.user.UserRepository;
import com.example.musical.login.api.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUser(String userId) {
        return userRepository.findByUserId(userId);
    }
}