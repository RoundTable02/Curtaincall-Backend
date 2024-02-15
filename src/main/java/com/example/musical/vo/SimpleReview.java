package com.example.musical.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class SimpleReview {
    private Long id;
    private double rating;
    private String viewingDate;
    private String place;
    private String cast;
    @Setter
    private MusicalVO musical;
}
