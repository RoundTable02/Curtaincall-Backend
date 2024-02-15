package com.example.musical.dto;

import com.example.musical.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
public class ReviewDto {
    private String mName;
    private double rating;
    private String viewingDate;
    private String place;
    private String content;
    private String cast;
    private List<MultipartFile> imgFiles;

    public Review toEntity() {
        return Review.builder()
                .mName(mName)
                .rating(rating)
                .viewingDate(viewingDate)
                .place(place)
                .content(content)
                .cast(cast)
                .build();
    }
}
