package com.example.musical.dto;

import com.example.musical.entity.Review;
import com.example.musical.vo.MusicalVO;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter @Builder
public class ReviewRewriteDto {
    private Long reviewId;
    private String musical;
    private String mName;
    private double rating;
    private String viewingDate;
    private String place;
    private String content;
    private String cast;
    private List<MultipartFile> imgFiles;
    private List<String> boardImgs;

    public Review toEntity() {
        return Review.builder()
                .mName(mName)
                .cast(cast)
                .viewingDate(viewingDate)
                .rating(rating)
                .content(content)
                .place(place)
                .musical(musical).build();
    }

    @Override
    public String toString() {
        return "ReviewRewriteDto{" +
                "reviewId=" + reviewId +
                ", musical='" + musical + '\'' +
                ", mName='" + mName + '\'' +
                ", rating=" + rating +
                ", viewingDate='" + viewingDate + '\'' +
                ", place='" + place + '\'' +
                ", content='" + content + '\'' +
                ", cast='" + cast + '\'' +
                ", imgFiles=" + imgFiles +
                ", boardImgs=" + boardImgs +
                '}';
    }
}
