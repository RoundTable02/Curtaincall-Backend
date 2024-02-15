package com.example.musical.service;

import com.example.musical.entity.BoardImg;
import com.example.musical.login.api.entity.user.User;
import com.example.musical.login.api.repository.user.UserRefreshTokenRepository;
import com.example.musical.login.api.repository.user.UserRepository;
import com.example.musical.login.oauth.entity.RoleType;
import com.example.musical.repository.BoardImgRepository;
import com.example.musical.s3.S3Uploader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserFrontService {
    private UserRepository userRepository;
    private UserRefreshTokenRepository userRefreshTokenRepository;
    private BoardImgRepository boardImgRepository;
    private S3Uploader s3Uploader;

    public UserFrontService(UserRepository userRepository, UserRefreshTokenRepository userRefreshTokenRepository, BoardImgRepository boardImgRepository, S3Uploader s3Uploader) {
        this.userRepository = userRepository;
        this.userRefreshTokenRepository = userRefreshTokenRepository;
        this.boardImgRepository = boardImgRepository;
        this.s3Uploader = s3Uploader;
    }

    @Transactional
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public boolean existsUserByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


    @Transactional
    public User setNickname(String nickname, String userId) {
        User user = findUserByUserId(userId);
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    @Transactional
    public User findUserByUserId(String name) {
        return userRepository.findByUserId(name);
    }


    @Transactional
    public String changeProfileImg(MultipartFile m, String userId) throws IOException {
        User user = findUserByUserId(userId);
        BoardImg orgImg = user.getBoardImg();
        if(!Objects.isNull(orgImg)) {   // 현재 프로필 이미지가 존재하는지 check
            s3Uploader.deleteFile(orgImg.getFileKey());
            boardImgRepository.delete(orgImg);
        }
        BoardImg boardImg = s3Uploader.upload(m, "profile");
        boardImg.setUser(user);
        user.setProfileImageUrl(boardImg.getFileRoute());
        user.setBoardImg(boardImg);
        return boardImg.getFileRoute();
    }

    @Transactional
    public User deleteUser(String userId) {
        User user = this.findUserByUserId(userId);
        user.setDeletedAt(LocalDateTime.now());
        user.setNickname(null);
        user.setBoardImg(null);
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    /**
     * 유저 삭제
     *  - 유저 삭제는 실제 데이터 삭제가 아닌 deletedAt에 날짜를 추가하는 것입니다.
     *  - 나중에 User가 존재하는지 확인할 때 deletedAt이 null이면 존재하는 사용자이고, 아니면 탈퇴된 사용자입니다.
     */

    @Transactional
    public User deleteUserByNickname(String nickname) {
        User user = findByNickname(nickname).orElseThrow();
        user.setDeletedAt(LocalDateTime.now());
        user.setNickname(null);
        user.setBoardImg(null);
        return userRepository.save(user);
    }

    public User setAdmin(String nickname) {
        User user = findByNickname(nickname).orElseThrow();
        user.setRoleType(RoleType.ADMIN);
        return userRepository.save(user);
    }
}
