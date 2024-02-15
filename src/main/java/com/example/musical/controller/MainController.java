package com.example.musical.controller;

import com.example.musical.dto.*;
import com.example.musical.entity.*;
import com.example.musical.login.api.entity.user.User;
import com.example.musical.service.BoardService;
import com.example.musical.service.MusicalService;
import com.example.musical.service.UserFrontService;
import com.example.musical.vo.*;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
public class MainController {
    private UserFrontService userFrontService;
    private BoardService boardService;
    private MusicalService musicalService;

    public MainController(UserFrontService userFrontService, BoardService boardService, MusicalService musicalService) {
        this.userFrontService = userFrontService;
        this.boardService = boardService;
        this.musicalService = musicalService;
    }

    @GetMapping("/user")    // 유저 정보
    public User user(Principal principal) {
        return userFrontService.findUserByUserId(principal.getName());
    }

    @GetMapping("/mypage/nickname") // 닉네임 중복 확인
    public NicknameDto nicknameExists(String nickname) throws UnsupportedEncodingException {
        String name = URLDecoder.decode(nickname, "UTF-8");

        return NicknameDto.builder()
                .nickname(name)
                .isUnique(!userFrontService.existsUserByNickname(name))
                .build();
    }

    @PostMapping("/mypage/nickname")    // 닉네임 설정
    public User nicknameSave(@RequestBody Map<String, String> nickname, Principal principal) {
        return userFrontService.setNickname(nickname.get("nickname"), principal.getName());
    }

    @GetMapping("/mypage")  // 마이페이지
    public MyPage mypage(Principal principal) {
        String userId = principal.getName();
        User user = userFrontService.findUserByUserId(userId);
        return MyPage.builder()
                .user(user)
                .myBoard(boardService.findMyPost(userId))
                .myScrap(boardService.findMyScrap(userId))
                .build();
    }

    @PostMapping("/mypage/profileImg")  // 프로필 사진 설정
    public Map<String, String> profileImg(MultipartFile imgFile, Principal principal) throws IOException {
        return Map.of("profileImg", userFrontService.changeProfileImg(imgFile, principal.getName()));
    }

    // BOARD

    @PostMapping("/board/{bType}")  // 게시글 저장
    public Board write(@PathVariable(value = "bType") String bType,
                      @ModelAttribute BoardDto boardDto, Principal principal) throws IOException {
        return boardService.savePost(boardDto, bType, principal.getName());
    }

    @GetMapping("/board/list/{bType}")  // 카테고리(자유게시판 등등...)에 따른 게시글 리스트, Pageable 초기값 1
    public PostList list(@PathVariable(value = "bType") String bType, @PageableDefault(size = 10) Pageable pageable) {
        return PostList.builder()
                .totalCount(boardService.countList(bType))
                .list(boardService.getList(bType, pageable))
                .build();
    }

    @GetMapping("/board/hot/{bType}")   // 해당 게시판의 핫 게시글 3개
    public PostList hot(@PathVariable(value = "bType") String bType) {
        return PostList.builder()
                .totalCount(3L)
                .list(boardService.getHotList(bType, 10))
                .build();
    }

    @GetMapping("/board/{postId}")  // 게시글 세부
    public Post get(@PathVariable(value = "postId") Long Id, Principal principal) {
        Post post = boardService.getPost(Id);
        String userId = principal.getName();
        post.setLike(boardService.isAlreadyLike(Id, userId));   // 사용자가 이미 좋아요 눌렀는지 확인
        post.setScrap(boardService.isAlreadyScrap(Id, userId));
        return post;
    }

    @DeleteMapping("/board/{postId}")   // 게시글 삭제
    public void delete(@PathVariable(value = "postId") Long Id) {
        boardService.deletePost(Id);
    }

    @PostMapping("/board/like/{postId}")    // 게시글 좋아요 (좋아요 추가(true) 삭제(false))
    public LikeEntity likeBoard(@PathVariable(value = "postId") Long postId,
                                @RequestBody Map<String, Boolean> likeDto, Principal principal) {
        return boardService.like(postId, principal.getName(), likeDto.get("like"));
    }

    @PostMapping("/board/scrap/{postId}")   // 게시글 스크랩
    public Scrap scrapBoard(@PathVariable(value = "postId") Long postId,
                            @RequestBody Map<String, Boolean> scrapDto, Principal principal) {
        return boardService.scrap(postId, principal.getName(), scrapDto.get("scrap"));
    }

    @GetMapping("/search")  // 게시글 검색
    public SearchList getSearchList(String keyword, @PageableDefault(size = 10) Pageable pageable) throws UnsupportedEncodingException {
        String name = URLDecoder.decode(keyword, "UTF-8");

        return SearchList.builder()
                        .list(boardService.searchList(name, pageable))
                                .totalCount(boardService.searchCount(name))
                                        .build();
    }

    // REPLY

    @PostMapping("/board/reply/{postId}")   // 특정 게시물에 댓글 추가
    public Reply addReply(@PathVariable(value = "postId") Long postId,
                          @RequestBody ReplyDto replyDto, Principal principal) {
        return boardService.addReply(replyDto, replyDto.getParentId(), postId, principal.getName());
    }

    @DeleteMapping("/board/reply/{replyId}")    // 댓글 삭제
    public void deleteReply(@PathVariable(value = "replyId") Long replyId) {
        boardService.deleteReply(replyId);
    }

    @GetMapping("/board/reply/{postId}")    // 특정 게시물에 달린 댓글 리스트
    public Map<String, List<ReplyVO>> getReplyList(@PathVariable(value = "postId") Long postId) {
        return Map.of("comments",boardService.getReplyList(postId));
    }

    // MUSICAL & REVIEW

    @GetMapping("/musical") // 뮤지컬 리스트, KOPIS OPEN API 이용
    public String getMusicalList(String keyword, String page) throws IOException {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formatedNow = now.format(formatter);

        String urlStr = "http://www.kopis.or.kr/openApi/restful/pblprfr?service=0e6597716e9e4d99ade110f5510f2ec1"
                +"&stdate=" + "20090101"
                +"&eddate=" + formatedNow
                +"&rows=" + "10"
                +"&cpage=" + page
                +"&shcate=" + "AAAB"
                +"&shprfnm=" + keyword;

        return musicalService.openApi(urlStr).toString();
    }

    @GetMapping("/musical/{musicalId}") // 뮤지컬 세부 정보, KOPIS OPEN API 이용
    public String getMusical(@PathVariable(value = "musicalId") String musicalId) throws IOException {
        String urlStr = "http://www.kopis.or.kr/openApi/restful/pblprfr/"
                + musicalId
                + "?service=0e6597716e9e4d99ade110f5510f2ec1";

        return musicalService.openApi(urlStr).toString();
    }

    @PostMapping("/review/{musicalId}") // 특정 뮤지컬에 대한 리뷰 추가
    public Review saveReview(@PathVariable(value = "musicalId") String musicalId,
                             @ModelAttribute ReviewDto review, Principal principal) throws IOException {
        return musicalService.saveReview(review, musicalId, principal.getName());
    }

    @PutMapping("/review/{musicalId}")  // 리뷰 수정
    public Review rewriteReview(@PathVariable(value = "musicalId") String musicalId,
                                @ModelAttribute ReviewRewriteDto review, Principal principal) throws IOException {
        return musicalService.rewriteReview(review, musicalId, principal.getName());
    }

    @GetMapping("/review/myreview") // 나의 리뷰 리스트
    public Map<String, List<SimpleReview>> getMyReview(Principal principal) throws IOException {
        return Map.of("reviewList", musicalService.getReviewByUser(principal.getName()));
    }

    @GetMapping("/review/{reviewId}")   // 리뷰 세부 정보
    public ReviewVO getMusicalReview(@PathVariable(value = "reviewId") Long reviewId) throws IOException {
        return musicalService.getUserReview(reviewId);
    }

    @DeleteMapping("review/{reviewId}") // 리뷰 삭제
    public void deleteReview(@PathVariable Long reviewId) {
        musicalService.deleteReview(reviewId);
    }

//    @GetMapping("/review")
//    public List<SimpleReview> getMusicalReview(@RequestParam(value = "musicalId") Integer musicalId) {
//        return musicalService.getReviewList(musicalId);
//    }


}
