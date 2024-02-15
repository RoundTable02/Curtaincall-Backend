package com.example.musical.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NicknameDto {
    String nickname;
    boolean isUnique;
}
