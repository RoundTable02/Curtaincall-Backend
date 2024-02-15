package com.example.musical.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Builder
public class ReviewVO {
    private Long reviewId;
    private String mName;
    private MusicalVO musical;
    private double rating;
    private String viewingDate;
    private String place;
    private String content;
    private String cast;
    private List<String> boardImgs;
}
