package com.example.musical.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReplyVO {
    private Long id;
    private LocalDateTime registerDate;
    private String replyContent;
    private boolean secret;
    private String nickname;
    private String profileImg;
    private Long parentReply;
    private Integer depth;
    private Integer likeCount;
}
