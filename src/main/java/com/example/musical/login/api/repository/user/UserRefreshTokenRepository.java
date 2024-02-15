package com.example.musical.login.api.repository.user;

import com.example.musical.login.api.entity.user.UserRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {
    UserRefreshToken findByUserId(String userId);
    UserRefreshToken findByRefreshToken(String refreshToken);
    UserRefreshToken findByUserIdAndRefreshToken(String userId, String refreshToken);
}