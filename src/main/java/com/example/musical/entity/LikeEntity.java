package com.example.musical.entity;

import com.example.musical.login.api.entity.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "boardId", foreignKey = @ForeignKey(name = "fk_like_boardId"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Board board;

    @ManyToOne
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name = "fk_like_userId"))
    private User user;

    @Builder
    public LikeEntity(Board board, User user) {
        this.board = board;
        this.user = user;
    }
}
