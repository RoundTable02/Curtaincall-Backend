package com.example.musical.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Post {
    private Long boardId;
    private String boardType;
    private String title;
    private String content;
    private String nickname;
    private String profileImg;
    private List<String> boardImgs;
    private Integer likeCount;
    private Integer scrapCount;
    private LocalDateTime registerDate;
    private String product;
    private Integer price;
    private String place;
    private String term;
    private Integer views;
    private boolean delivery;

    @Setter
    private boolean isLike;
    @Setter
    private boolean isScrap;
}
