package com.mmt.repository;
import com.mmt.model.ReviewReply; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface ReviewReplyRepository extends JpaRepository<ReviewReply,Long> { List<ReviewReply> findByReviewIdOrderByCreatedAtAsc(Long reviewId); }
