package com.mmt.repository;
import com.mmt.model.RecommendationFeedback; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback,Long> { List<RecommendationFeedback> findByUserId(Long userId); }
