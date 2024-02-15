package com.example.musical.login.api.repository.user;

import com.example.musical.login.api.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select (count(u) > 0) from User u where u.nickname = ?1")
    boolean existsByNickname(String nickname);

    @Query("select u from User u where u.userId = ?1")
    User findByUserId(String userId);

    @Query("select u from User u where u.nickname = ?1")
    Optional<User> findByNickname(String nickname);
}
