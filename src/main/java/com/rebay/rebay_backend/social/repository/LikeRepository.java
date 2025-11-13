package com.rebay.rebay_backend.social.repository;

import com.rebay.rebay_backend.Post.entity.Post;
import com.rebay.rebay_backend.social.entity.Like;
import com.rebay.rebay_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);

    boolean existsByUserAndPost(User user, Post post);

    void deleteByUserAndPost(User user, Post post);

    /**
     * 일주일 내 좋아요가 가장 많은 Post들을 좋아요 수 기준 내림차순으로 페이지 조회합니다.
     * @param oneWeekAgo 현재 시간으로부터 일주일 전 시간
     * @param pageable 페이지 정보 (PageRequest)
     * @return Post 엔티티의 Page 객체
     */
    @Query(
            value = "SELECT p.* " +
                    "FROM posts p " +
                    "INNER JOIN (" +
                    "    SELECT l.post_id, COUNT(l.post_id) as like_count " +
                    "    FROM likes l " +
                    "    WHERE l.created_at >= :oneWeekAgo " +
                    "    GROUP BY l.post_id " +
                    ") AS weekly_likes " +
                    "ON p.id = weekly_likes.post_id " +
                    "ORDER BY weekly_likes.like_count DESC",
            countQuery = "SELECT COUNT(DISTINCT l.post_id) " +
                    "FROM likes l " +
                    "WHERE l.created_at >= :oneWeekAgo",
            nativeQuery = true
    )
    Page<Post> findTopLikedPostsLastWeek(
            @Param("oneWeekAgo") LocalDateTime oneWeekAgo,
            Pageable pageable
    );
}