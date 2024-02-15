package com.example.musical.entity;

import com.example.musical.login.api.entity.user.User;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Review extends BaseEntity{
    private String musical;

    @ManyToOne
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name = "fk_review_userId"))
    private User user;

    private double rating;

    private String viewingDate;

    private String place;

    private String content;

    private String cast;

    private String mName;

    @Column
    @OneToMany(mappedBy = "review",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true
    )
    private List<BoardImg> boardImgs;

    public void setBoardImgs(List<BoardImg> boardImgs) {
        this.boardImgs = boardImgs;
    }

}
