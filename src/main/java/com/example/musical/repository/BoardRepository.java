package com.example.musical.repository;

import com.example.musical.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query("select b from Board b " +
            "where b.title like concat('%', ?1, '%') or b.content like concat('%', ?1, '%') " +
            "order by b.Id DESC")
    Page<Board> findByTitleContainingOrContentContainingOrderByIdDesc(String keyword, Pageable pageable);

    @Query("select s from Board s where s.boardType = ?1")
    List<Board> findByBoardName(String boardType);

    @Query("select b from Board b where b.boardType = ?1 order by b.Id DESC")
    Page<Board> findByBoardTypeOrderByIdDesc(String boardType, Pageable pageable);

    @Query("select b from Board b where b.boardType = ?1 and b.likeCount >= ?2 order by b.Id DESC")
    List<Board> findAllByBoardTypeAndLikeCountGreaterThanEqualOrderByIdDesc(String boardType, Integer likeCount);

    @Query("select count(b) from Board b where b.boardType = ?1")
    Long countByBoardType(String boardType);

    @Query("select b from Board b where b.user.userId = ?1 order by b.Id DESC")
    List<Board> findAllByUserUserIdOrderByIdDesc(String userId);

    @Query("select count(b) from Board b where b.title like concat('%', ?1, '%') or b.content like concat('%', ?1, '%')")
    Long countByTitleContainingOrContentContaining(String keyword);
}
