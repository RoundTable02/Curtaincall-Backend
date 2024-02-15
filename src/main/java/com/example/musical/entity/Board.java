package com.example.musical.entity;

import com.example.musical.login.api.entity.user.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Board extends BaseEntity {
    @Column
    private String title;

    @Column
    private String content;

    @ManyToOne
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name = "fk_board_userId"))
    private User user;

    @Column
    private Integer likeCount;

    @Column
    private Integer scrapCount;

    @Column
    private Integer views;

    @Column
    private String boardType;

    @Column
    @OneToMany(mappedBy = "board",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true
    )
    private List<BoardImg> boardImgs;

    public void setBoardImgs(List<BoardImg> boardImgs) {
        this.boardImgs = boardImgs;
    }

    //sell
    @Column
    private String product;

    @Column
    private Integer price;

    @Column
    private String place;

    @Column
    private boolean delivery;

    //rent
    @Column
    private String term;

    @PrePersist
    public void prePersist() {
        this.likeCount = this.likeCount == null ? 0 : this.likeCount;
        this.scrapCount = this.scrapCount == null ? 0 : this.scrapCount;
        this.views = this.views == null ? 0 : this.views;
    }


    public void addLike() {
        this.likeCount++;
    }

    public void subLike() {
        this.likeCount--;
    }

    public void addScrap() {
        this.scrapCount++;
    }

    public void subScrap() {
        this.scrapCount--;
    }

    public void addViews() {
        this.views++;
    }

}
