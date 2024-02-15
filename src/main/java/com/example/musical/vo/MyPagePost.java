package com.example.musical.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class MyPagePost {
    private Long boardId;
    private String title;
    private String boardType;
    private String nickname;
    private LocalDateTime registerDate;
    private Integer likeCount;
    private String content;
    private String img;
}
