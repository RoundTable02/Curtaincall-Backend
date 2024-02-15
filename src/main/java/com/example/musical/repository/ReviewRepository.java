package com.example.musical.repository;

import com.example.musical.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("select r from Review r where r.user.userId = ?1 order by r.Id DESC")
    List<Review> findAllByUserUserIdOrderByIdDesc(String userId);

    @Query("select r from Review r where r.musical = ?1")
    List<Review> findAllByMusical(String musicalId);
}
