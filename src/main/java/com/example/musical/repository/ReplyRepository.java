package com.example.musical.repository;

import com.example.musical.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    @Query("select r from Reply r where r.board.Id = ?1")
    List<Reply> findAllByBoardId(Long boardId);

}
