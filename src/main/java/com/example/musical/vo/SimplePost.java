package com.example.musical.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimplePost {
    private Long boardId;
    private String title;
    private String nickname;
    private LocalDateTime registerDate;
    private Integer likeCount;
    private String place;
}
