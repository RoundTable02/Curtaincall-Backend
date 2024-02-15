package com.example.musical.service;

import com.example.musical.dto.ReviewDto;
import com.example.musical.dto.ReviewRewriteDto;
import com.example.musical.entity.Board;
import com.example.musical.entity.BoardImg;
import com.example.musical.entity.Review;
import com.example.musical.login.api.entity.user.User;
import com.example.musical.login.api.repository.user.UserRepository;
import com.example.musical.repository.ReviewRepository;
import com.example.musical.s3.S3Uploader;
import com.example.musical.vo.MusicalVO;
import com.example.musical.vo.ReviewVO;
import com.example.musical.vo.SimpleReview;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MusicalService {
    ReviewRepository reviewRepository;
    UserRepository userRepository;
    private S3Uploader s3Uploader;

    public MusicalService(ReviewRepository reviewRepository, UserRepository userRepository, S3Uploader s3Uploader) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.s3Uploader = s3Uploader;
    }

    @Transactional
    public User findUserByUserId(String name) {
        return userRepository.findByUserId(name);
    }

    /**
     * Review Service
     *  saveReview(ReviewDto reviewDto, String musicalId, String userId)
     *   - 뮤지컬 리뷰 저장
     *
     *  getMusicalInfo(String musical)
     *   - 뮤지컬 정보 불러오기, Open API
     *
     *  getUserReview(Long reviewId)
     *   - 유저 리뷰 세부 정보
     *
     *  getReviewByUser(String userId)
     *   - 유저가 작성한 모든 리뷰
     *
     *  deleteReview(Long reviewId)
     *   - 리뷰 삭제
     *
     *  rewriteReview(ReviewRewriteDto reviewDto, String musicalId, String userId)
     *   - 리뷰 수정
     *   - imgFiles가 새로 추가할 이미지, boardImgs가 삭제할 이미지의 url
     */

    @Transactional
    public Review saveReview(ReviewDto reviewDto, String musicalId, String userId) throws IOException {
        User user = findUserByUserId(userId);
        Review review = reviewDto.toEntity();
        review.setUser(user);
        review.setMusical(musicalId);

        List<MultipartFile> files = reviewDto.getImgFiles();
        List<BoardImg> boardImgs = new ArrayList<>();
        if(!Objects.isNull(files) & !files.get(0).isEmpty()) {
            for(MultipartFile m : files) {
                BoardImg boardImg = s3Uploader.upload(m, "review");
                boardImg.setReview(review);
                boardImgs.add(boardImg);
            }
        }
        else boardImgs = null;
        review.setBoardImgs(boardImgs);

        return reviewRepository.save(review);
    }

    @Transactional
    public List<SimpleReview> getReviewList(String musicalId) throws IOException {
        List<Review> reviewList = reviewRepository.findAllByMusical(musicalId);
        List<SimpleReview> simpleReviewList = new ArrayList<>();
        for(Review r : reviewList) {
            simpleReviewList.add(convertEntityToSimple(r));
        }
        return simpleReviewList;
    }

    private SimpleReview convertEntityToSimple(Review r) throws IOException {
        SimpleReview simpleReview = SimpleReview.builder()
                .id(r.getId())
                .rating(r.getRating())
                .viewingDate(r.getViewingDate())
                .place(r.getPlace())
                .cast(r.getCast())
                .build();
        simpleReview.setMusical(getMusicalInfo(r.getMusical()));

        return simpleReview;
    }

    private MusicalVO getMusicalInfo(String musical) throws IOException {
        String musicalJson = "http://www.kopis.or.kr/openApi/restful/pblprfr/"
                + musical
                + "?service=0e6597716e9e4d99ade110f5510f2ec1";
        JSONObject jsonObject = this.openApi(musicalJson);
        JSONObject dbs = (JSONObject) jsonObject.get("dbs");
        JSONObject db = (JSONObject) dbs.get("db");
        return MusicalVO.builder()
                .poster(db.getString("poster"))
                .prfnm(db.getString("prfnm"))
                .musicalId(musical)
                .build();
    }

    public JSONObject openApi(String urlStr) throws IOException {
        StringBuilder result = new StringBuilder();

        URL url = new URL(urlStr);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        BufferedReader br;

        br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
        String returnLine;
        while ((returnLine = br.readLine()) != null) {
            result.append(returnLine+"\n\r");
        }

        urlConnection.disconnect();

        JSONObject jsonObject = XML.toJSONObject(result.toString());
        return jsonObject;
    }

    @Transactional
    public ReviewVO getUserReview(Long reviewId) throws IOException {
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        List<String> boardImgs = new ArrayList<>();
        List<BoardImg> imgs = review.getBoardImgs();
        for (BoardImg b : imgs) {
            boardImgs.add(b.getFileRoute());
        }

        return ReviewVO.builder()
                .reviewId(review.getId())
                .mName(review.getMName())
                .musical(getMusicalInfo(review.getMusical()))
                .content(review.getContent())
                .viewingDate(review.getViewingDate())
                .rating(review.getRating())
                .place(review.getPlace())
                .boardImgs(boardImgs)
                .cast(review.getCast())
                .build();
    }

    @Transactional
    public List<SimpleReview> getReviewByUser(String userId) throws IOException {
        List<Review> reviewList = reviewRepository.findAllByUserUserIdOrderByIdDesc(userId);
        List<SimpleReview> simpleReviewList = new ArrayList<>();
        for(Review r : reviewList) {
            simpleReviewList.add(convertEntityToSimple(r));
        }
        return simpleReviewList;
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        List<BoardImg> boardImgs = review.getBoardImgs();
        for(BoardImg b: boardImgs) {
            s3Uploader.deleteFile(b.getFileKey());
        }

        reviewRepository.deleteById(reviewId);
    }

    @Transactional
    public Review rewriteReview(ReviewRewriteDto reviewDto, String musicalId, String userId) throws IOException {
        Review orgReview = reviewRepository.findById(reviewDto.getReviewId()).orElseThrow();
        List<String> delImgs = reviewDto.getBoardImgs();    // 삭제할 이미지의 url
        List<BoardImg> boardImgs = new ArrayList<>();
        List<BoardImg> orgImgs = orgReview.getBoardImgs();  // 원래 review의 사진들
        Review review = reviewDto.toEntity();
        review.setUser(findUserByUserId(userId));
        review.setMusical(musicalId);

        for(int i = 0; i < orgImgs.size(); i++) {   // 삭제할 이미지 찾기
            boolean e = false;  // 현재 검사하는 이미지가 삭제 대상 이미지인지
            for(int j = 0; j < delImgs.size(); j++) {
                if(orgImgs.get(i).getFileRoute().equals(delImgs.get(j))){   // 대상 이미지 url이 삭제 대상인지 확인
                    e = true;
                    s3Uploader.deleteFile(orgImgs.get(i).getFileKey());
                }
            }
            if(!e) {
                BoardImg boardImg = orgImgs.get(i);
                boardImg.setReview(review);
                boardImgs.add(boardImg);
            }
        }

        if(!Objects.isNull(reviewDto.getImgFiles()) && !reviewDto.getImgFiles().get(0).isEmpty())
            for(MultipartFile m : reviewDto.getImgFiles()) {
                BoardImg boardImg = s3Uploader.upload(m, "review");
                boardImg.setReview(review);
                boardImgs.add(boardImg);
            }

        reviewRepository.deleteById(reviewDto.getReviewId());
        review.setBoardImgs(boardImgs);
        return reviewRepository.save(review);
    }
}
