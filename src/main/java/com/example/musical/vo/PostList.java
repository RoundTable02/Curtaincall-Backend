package com.example.musical.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PostList {
    private Long totalCount;
    private List<SimplePost> list;
}
