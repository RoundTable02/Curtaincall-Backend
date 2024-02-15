package com.example.musical.dto;

import com.example.musical.entity.*;
import com.example.musical.login.api.entity.user.User;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class BoardDto {
    private String title;
    private String content;
    private List<MultipartFile> files;
    private String product;
    private Integer price;
    private String place;
    private String term;
    private boolean delivery;

    public Board toEntity() {
        Board board = Board.builder()
                .title(title)
                .content(content)
                .product(product)
                .price(price)
                .place(place)
                .term(term)
                .delivery(delivery)
                .build();
        return board;
    }
}
