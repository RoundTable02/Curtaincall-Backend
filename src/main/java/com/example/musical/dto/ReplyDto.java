package com.example.musical.dto;

import com.example.musical.entity.Board;
import com.example.musical.entity.Reply;
import com.example.musical.login.api.entity.user.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ReplyDto {
    private String replyContent;
    private Long parentId;
    private boolean secret;
}
