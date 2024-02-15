package com.example.musical.service;

import com.example.musical.dto.BoardDto;
import com.example.musical.dto.ReplyDto;
import com.example.musical.entity.*;
import com.example.musical.login.api.entity.user.User;
import com.example.musical.login.api.repository.user.UserRepository;
import com.example.musical.repository.*;
import com.example.musical.s3.S3Uploader;
import com.example.musical.vo.MyPagePost;
import com.example.musical.vo.Post;
import com.example.musical.vo.ReplyVO;
import com.example.musical.vo.SimplePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class BoardService {
    private BoardRepository boardRepository;
    private LikeRepository likeRepository;
    private ReplyRepository replyRepository;
    private ScrapRepository scrapRepository;
    private UserRepository userRepository;
    private S3Uploader s3Uploader;

    public BoardService(BoardRepository boardRepository, LikeRepository likeRepository, ReplyRepository replyRepository, ScrapRepository scrapRepository, UserRepository userRepository, S3Uploader s3Uploader) {
        this.boardRepository = boardRepository;
        this.likeRepository = likeRepository;
        this.replyRepository = replyRepository;
        this.scrapRepository = scrapRepository;
        this.userRepository = userRepository;
        this.s3Uploader = s3Uploader;
    }

    @Transactional  // UserId로 유저 정보 받아오기
    public User findUserByUserId(String name) {
        return userRepository.findByUserId(name);
    }

    /**
     * Board Service
     *  savePost(BoardDto boardDto, String boardType, String userId)
     *   - 게시글 저장
     *   - files(MultipartFile List) -> S3에 사진 저장
     *
     *  getList(String boardType, Pageable pageable)
     *   - 해당 게시판(boardType)의 게시글 리스트
     *   - Pageable로 게시글 페이징
     *
     *  countList(String boardType)
     *   - 게시판의 게시글 수
     *
     *  deletePost(Long id)
     *   - 게시글 삭제
     */

    @Transactional
    public Board savePost(BoardDto boardDto, String boardType, String userId) throws IOException {
        Board board = boardDto.toEntity();
        User user = findUserByUserId(userId);
        board.setUser(user);
        board.setBoardType(boardType);

        List<MultipartFile> files = boardDto.getFiles();
        List<BoardImg> boardImgs = new ArrayList<>();   // 게시글에 추가될 사진 리스트

        if(!Objects.isNull(files) && files.get(0).isEmpty()) {  // 추가할 게시글에 사진이 존재하는지
            for(MultipartFile m : files) {  // 사진 하나씩 s3에 업로드
                BoardImg boardImg = s3Uploader.upload(m, boardType);
                boardImg.setBoard(board);
                boardImgs.add(boardImg);
            }
        }
        else boardImgs = null;
        board.setBoardImgs(boardImgs);

        return boardRepository.save(board);
    }

    @Transactional
    public List<SimplePost> getList(String boardType, Pageable pageable) {
        Page<Board> boardList = boardRepository.findByBoardTypeOrderByIdDesc(boardType, pageable);
        List<SimplePost> simplePostList = new ArrayList<>();

        for(Board b : boardList){
            simplePostList.add(convertSimplePost(b));
        }
        return simplePostList;
    }

    private SimplePost convertSimplePost(Board b) {
        return SimplePost.builder()
                .nickname(b.getUser().getNickname())
                .boardId(b.getId())
                .likeCount(b.getLikeCount())
                .registerDate(b.getRegisterDate())
                .place(b.getPlace())
                .title(b.getTitle()).build();
    }

    @Transactional
    public Long countList(String boardType) {
        return boardRepository.countByBoardType(boardType);
    }

    @Transactional
    public Post getPost(Long id) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("일치하는 게시물이 없습니다."));
        Post post = this.convertEntityToPost(board);
        board.addViews();
        boardRepository.save(board);

        return post;
    }

    private Post convertEntityToPost(Board board) {
        List<BoardImg> imgs = board.getBoardImgs();
        List<String> boardImgs = new ArrayList<>();
        for(BoardImg bi : imgs) {   // 프론트로 보낼 때는 fileRoute만 따로 전달
            boardImgs.add(bi.getFileRoute());
        }

        User user = board.getUser();
        BoardImg boardImg = user.getBoardImg();
        String fileRoute = null;
        if(!Objects.isNull(boardImg))   // 프로필 이미지
            fileRoute = boardImg.getFileRoute();

        return Post.builder()
                .boardId(board.getId())
                .boardType(board.getBoardType())
                .content(board.getContent())
                .registerDate(board.getRegisterDate())
                .likeCount(board.getLikeCount())
                .scrapCount(board.getScrapCount())
                .views(board.getViews())
                .title(board.getTitle())
                .place(board.getPlace())
                .product(board.getProduct())
                .price(board.getPrice())
                .term(board.getTerm())
                .boardImgs(boardImgs)
                .nickname(user.getNickname())
                .profileImg(fileRoute)
                .delivery(board.isDelivery())
                .build();
    }

    @Transactional
    public void deletePost(Long id) {
        Board board = boardRepository.findById(id).orElseThrow();
        List<BoardImg> boardImgs = board.getBoardImgs();
        for(BoardImg b: boardImgs) {    // 게시글에 업로드 되었던 사진 S3에서 삭제
            s3Uploader.deleteFile(b.getFileKey());
        }

        boardRepository.deleteById(id);
    }

    @Transactional
    public Integer getLike(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("좋아요가 존재하지 않습니다.")).getLikeCount();
    }

    @Transactional
    public Integer getScrap(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("스크랩이 존재하지 않습니다.")).getScrapCount();
    }

    /**
     * Board Search
     *  searchList(String keyword, Pageable pageable)
     *   - 게시글 키워드로 검색
     *   - Pageable로 페이징 (기본 값 1)
     *
     *  searchCount(String keyword)
     *   - 검색 결과 수
     */

    @Transactional
    public List<MyPagePost> searchList(String keyword, Pageable pageable) {
        Page<Board> boardList = boardRepository.findByTitleContainingOrContentContainingOrderByIdDesc(keyword, pageable);
        List<MyPagePost> simplePostList = new ArrayList<>();
        for(Board b : boardList){
            simplePostList.add(this.convertEntityToMyPagePost(b));
        }

        return simplePostList;
    }
    @Transactional
    public Long searchCount(String keyword) {
        return boardRepository.countByTitleContainingOrContentContaining(keyword);
    }

//    private BoardDto convertEntityToDto(Board board){
//        return BoardDto.builder()
//                .boardId(board.getId())
//                .boardType(board.getBoardType())
//                .content(board.getContent())
//                .modifiedDate(board.getModifiedDate())
//                .registerDate(board.getRegisterDate())
//                .likeCount(board.getLikeCount())
//                .scrapCount(board.getScrapCount())
//                .views(board.getViews())
//                .title(board.getTitle())
//                .user(board.getUser())
//                .place(board.getPlace())
//                .product(board.getProduct())
//                .price(board.getPrice())
//                .term(board.getTerm())
//                .boardImgs(board.getBoardImgs())
//                .build();
//    }

    /**
     * Reply Service
     *  addReply(ReplyDto reply, Long parentReplyId, Long postId, String userId)
     *   - 댓글 추가
     *   - parentReplyId null이면 댓글, 존재하면 대댓글
     *
     *  deleteReply(Long replyId)
     *   - 댓글 삭제
     *
     *  getReplyList(Long boardId)
     *   - 해당 게시글의 댓글 리스트
     */

    @Transactional
    public Reply addReply(ReplyDto reply, Long parentReplyId, Long postId, String userId) {
        Reply parentReply = null;
        Integer depth = 0;
        if(!Objects.isNull(parentReplyId)){ // 댓글인지 대댓글인지 확인
            parentReply = replyRepository.findById(parentReplyId)
                    .orElseThrow(() -> new IllegalArgumentException("일치하는 댓글이 없습니다."));
            depth = 1;  // 대댓글이라면 댓글 깊이가 1
        }
        Board board = boardRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("일치하는 글이 없습니다."));
        User user = findUserByUserId(userId);

        Reply replyEntity = Reply.builder()
                .parentReply(parentReply)
                .replyContent(reply.getReplyContent())
                .board(board)
                .user(user)
                .depth(depth)
                .secret(reply.isSecret())
                .build();
        return replyRepository.save(replyEntity);
    }

    private ReplyVO replyEntityToVO(Reply r, Long parentId) {
        BoardImg boardImg = r.getUser().getBoardImg();
        String profileImg = null;
        if(!Objects.isNull(boardImg)) {
            profileImg = boardImg.getFileRoute();
        }
        return ReplyVO.builder()
                .id(r.getId())
                .registerDate(r.getRegisterDate())
                .parentReply(parentId)
                .replyContent(r.getReplyContent())
                .nickname(r.getUser().getNickname())
                .profileImg(profileImg)
                .depth(r.getDepth())
                .secret(r.isSecret())
                .likeCount(r.getLikeCount())
                .build();
    }

    @Transactional
    public void deleteReply(Long replyId) {
        replyRepository.deleteById(replyId);
    }

    @Transactional
    public List<ReplyVO> getReplyList(Long boardId){
        List<Reply> replyList = replyRepository.findAllByBoardId(boardId);
        List<ReplyVO> replyVOList = new ArrayList<>();
        for(Reply r : replyList) {
            Long parentId = null;
            if(!Objects.isNull(r.getParentReply())){    // 대댓글 check
                parentId = r.getParentReply().getId();
            }
            ReplyVO replyVO = this.replyEntityToVO(r, parentId);
            replyVOList.add(replyVO);
        }
        return replyVOList;
    }

    /**
     * Like And Scrap
     *  isAlreadyLike(Long boardId, String userId)
     *   - 해당 사용자가 이미 좋아요를 눌렀는지 확인
     *
     *  isAlreadyScrap(Long boardId, String userId)
     *   - 해당 사용자가 이미 스크랩을 눌렀는지 확인
     *
     *  like(Long boardId, String userId, boolean like)
     *   - 게시판에 좋아요 추가 / 취소
     *   - like = true(추가) false(취소)
     *
     *  scrap(Long boardId, String userId, boolean scrap)
     *  - 게시판에 스크랩 추가 / 취소
     *  - scrap = true(추가) false(취소)
     */

    @Transactional
    public boolean isAlreadyLike(Long boardId, String userId) {
        return likeRepository.existsByBoardIdAndUserId(boardId, userId);
    }

    @Transactional
    public boolean isAlreadyScrap(Long boardId, String userId) {
        return scrapRepository.existsByBoardIdAndUserId(boardId, userId);
    }

    @Transactional
    public LikeEntity like(Long boardId, String userId, boolean like) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("일치하는 글이 없습니다."));
        User user = findUserByUserId(userId);
        if(like) {                   // 좋아요 신규 생성
            board.addLike();    // 좋아요 1 추가
            boardRepository.save(board);
            return likeRepository.save(LikeEntity.builder()
                    .board(board)
                    .user(user)
                    .build()
            );
        }
        else {       // 좋아요 취소
            board.subLike();    // 좋아요 수 1 감소
            boardRepository.save(board);
            LikeEntity s = likeRepository.findByBoardIdAndUserId(boardId, userId).orElseThrow();
            likeRepository.delete(s);
        }
        return null;
    }

    @Transactional
    public Scrap scrap(Long boardId, String userId, boolean scrap) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("일치하는 글이 없습니다."));
        User user = findUserByUserId(userId);
        if(scrap) {    // 스크랩 신규 생성
            board.addScrap();   // 스크랩 수 1 추가
            boardRepository.save(board);
            return scrapRepository.save(Scrap.builder()
                    .board(board)
                    .user(user)
                    .build()
            );
        }
        else {       // 스크랩 취소
            board.subScrap();     // 스크랩 수 1 감소
            boardRepository.save(board);
            Scrap s = scrapRepository.findByBoardIdAndUserId(boardId, userId).orElseThrow();
            scrapRepository.delete(s);
        }
        return null;
    }

    /**
     * MyPage Service
     *  findMyPost(String userId)
     *   - 사용자(userId)가 쓴 모든 글 return
     *
     *  findMyScrap(String userId)
     *   - 사용자(userId)가 스크랩한 모든 글 return
     */

    @Transactional
    public List<Board> findBoardsByUserId(String userId) {
       return boardRepository.findAllByUserUserIdOrderByIdDesc(userId);
    }

    @Transactional
    public List<MyPagePost> findMyPost(String userId) {
        List<Board> boardList = this.findBoardsByUserId(userId);
        List<MyPagePost> myPagePostList = new ArrayList<>();
        for (Board b : boardList) {
            myPagePostList.add(this.convertEntityToMyPagePost(b));
        }
        return myPagePostList;
    }

    private MyPagePost convertEntityToMyPagePost(Board b) {
        String boardImg = null;
        List<BoardImg> boardImgs = b.getBoardImgs();
        if(!boardImgs.isEmpty()){
            boardImg = boardImgs.get(0).getFileRoute();
        }
        return MyPagePost.builder()
                .boardId(b.getId())
                .img(boardImg)
                .boardType(b.getBoardType())
                .registerDate(b.getRegisterDate())
                .likeCount(b.getLikeCount())
                .nickname(b.getUser().getNickname())
                .content(b.getContent())
                .title(b.getContent())
                .build();
    }

    @Transactional
    public List<Scrap> findScrapsByUserId(String userId) {
        return scrapRepository.findAllByUserUserIdOrderByIdDesc(userId);
    }

    @Transactional
    public List<MyPagePost> findMyScrap(String userId) {
        List<Scrap> scrapList = this.findScrapsByUserId(userId);
        List<MyPagePost> myPagePostList = new ArrayList<>();
        for(Scrap s : scrapList){
            Board b = s.getBoard();
            myPagePostList.add(this.convertEntityToMyPagePost(b));
        }
        return myPagePostList;
    }

    /**
     * Hot Board
     *  getHotList(String bType, Integer likeCount)
     *   - 핫 게시판
     *   - 해당 게시판에서 좋아요 수가 likeCount 이상인 글들 중에서 최근 3개만 반환
     */

    @Transactional
    public List<SimplePost> getHotList(String bType, Integer likeCount) {
        List<Board> boardList = boardRepository.findAllByBoardTypeAndLikeCountGreaterThanEqualOrderByIdDesc(bType, likeCount);
        List<SimplePost> simplePostList = new ArrayList<>();
        if(!boardList.isEmpty()) {
            int size = boardList.size() > 3 ? 3 : boardList.size(); // 핫 게시글의 수가 3개 미만이면 그 갯수만큼만 반환
            for (int i = 0; i < size; i++) {
                simplePostList.add(convertSimplePost(boardList.get(i)));
            }
        }
        return simplePostList;
    }

    public List<Board> findAllBoards() {
        return boardRepository.findAll();
    }
}
