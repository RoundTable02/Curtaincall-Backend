package com.example.musical.repository;

import com.example.musical.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface LikeRepository extends JpaRepository<LikeEntity,Long> {
    @Query("select l from LikeEntity l where l.board.Id = ?1 and l.user.userId = ?2")
    Optional<LikeEntity> findByBoardIdAndUserId(Long BoardId, String UserId);

    @Modifying
    @Query("delete from LikeEntity l where l.board.Id = ?1 and l.user.userId = ?2")
    void deleteByBoardIdAndUserId(Long boardId, String userId);

    @Query("select (count(l) > 0) from LikeEntity l where l.board.Id = ?1 and l.user.userId = ?2")
    boolean existsByBoardIdAndUserId(Long boardId, String userId);
}
