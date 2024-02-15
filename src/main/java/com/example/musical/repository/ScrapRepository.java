package com.example.musical.repository;

import com.example.musical.entity.Board;
import com.example.musical.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface ScrapRepository extends JpaRepository<Scrap,Long> {
    @Query("select s from Scrap s where s.board.Id = ?1 and s.user.userId = ?2")
    Optional<Scrap> findByBoardIdAndUserId(Long boardId, String userId);

    @Transactional
    @Modifying
    @Query("delete from Scrap s where s.board.Id = ?1 and s.user.userId = ?2")
    void deleteByBoardIdAndUserUserId(Long boardId, String userId);

    @Query("select (count(s) > 0) from Scrap s where s.board.Id = ?1 and s.user.userId = ?2")
    boolean existsByBoardIdAndUserId(Long boardId, String userId);

    @Query("select s from Scrap s where s.user.userId = ?1 order by s.Id DESC")
    List<Scrap> findAllByUserUserIdOrderByIdDesc(String userId);
}
