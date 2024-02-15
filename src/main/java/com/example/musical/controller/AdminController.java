package com.example.musical.controller;

import com.example.musical.entity.Board;
import com.example.musical.login.api.entity.user.User;
import com.example.musical.service.BoardService;
import com.example.musical.service.UserFrontService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final UserFrontService userFrontService;
    private final BoardService boardService;

    public AdminController(UserFrontService userFrontService, BoardService boardService) {
        this.userFrontService = userFrontService;
        this.boardService = boardService;
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userFrontService.findAllUsers();
    }

    @GetMapping("/boards")
    public List<Board> getBoards() {
        return boardService.findAllBoards();
    }

    @DeleteMapping("/user/nickname")
    public User deleteUserByNickname(String nickname) {
        return userFrontService.deleteUserByNickname(nickname);
    }

    @DeleteMapping("/user")
    public User deleteUser(String userId) {
        return userFrontService.deleteUser(userId);
    }

    @DeleteMapping("/board")
    public void deleteBoard(Long boardId) {
        boardService.deletePost(boardId);
    }

    @PutMapping("/admin")
    public User admin(String nickname) {
        return userFrontService.setAdmin(nickname);
    }
}
