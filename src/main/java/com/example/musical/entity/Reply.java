package com.example.musical.entity;

import com.example.musical.login.api.entity.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Reply extends BaseEntity {
    @Column
    private String replyContent;

    @Column
    private boolean secret;

    @ManyToOne
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name = "fk_reply_userId"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "boardId", foreignKey = @ForeignKey(name = "fk_reply_boardId"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Board board;

    @ManyToOne
    @JoinColumn(name = "parentId", foreignKey = @ForeignKey(name = "fk_reply_parentId"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Reply parentReply;

    @Column(nullable = false)
    private Integer depth;

    @Column
    private Integer likeCount;

    @PrePersist
    public void prePersist() {
        this.likeCount = this.likeCount == null ? 0 : this.likeCount;
    }

    //builder
    @Builder
    public Reply(String replyContent, boolean secret, User user, Board board, Reply parentReply, Integer depth, Integer likeCount) {
        this.replyContent = replyContent;
        this.secret = secret;
        this.user = user;
        this.board = board;
        this.parentReply = parentReply;
        this.depth = depth;
        this.likeCount = likeCount;
    }
}
