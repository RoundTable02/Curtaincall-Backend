package com.example.musical.vo;

import com.example.musical.entity.Scrap;
import com.example.musical.login.api.entity.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPage {
    private User user;
    private List<MyPagePost> myBoard;
    private List<MyPagePost> myScrap;
}
