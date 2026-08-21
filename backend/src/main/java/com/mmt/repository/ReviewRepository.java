package com.mmt.repository;
import com.mmt.model.Review; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface ReviewRepository extends JpaRepository<Review,Long> { List<Review> findByEntityTypeAndEntityIdAndModeratedRemovedFalseOrderByCreatedAtDesc(String entityType,Long entityId); }
