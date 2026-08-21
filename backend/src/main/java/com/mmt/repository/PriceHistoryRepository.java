package com.mmt.repository;
import com.mmt.model.PriceHistory; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface PriceHistoryRepository extends JpaRepository<PriceHistory,Long> { List<PriceHistory> findByItemTypeAndItemIdOrderByRecordedAtAsc(String itemType, Long itemId); }
