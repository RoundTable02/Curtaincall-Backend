package com.example.musical.entity;

import com.example.musical.login.api.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BoardImg {
    @Id @GeneratedValue
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "boardId")
    private Board board;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "reviewId")
    private Review review;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "userId")
    private User user;

    @JsonIgnore
    private String originalName;
    @JsonIgnore
    private String fileKey;

    private String fileRoute;


}
