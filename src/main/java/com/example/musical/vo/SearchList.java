package com.example.musical.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SearchList {
    private Long totalCount;
    private List<MyPagePost> list;
}
