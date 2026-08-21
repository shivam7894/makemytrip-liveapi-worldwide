package com.mmt.repository;
import com.mmt.model.PriceFreeze; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface PriceFreezeRepository extends JpaRepository<PriceFreeze,Long> { List<PriceFreeze> findByUserIdAndStatus(Long userId,String status); Optional<PriceFreeze> findTopByUserIdAndItemTypeAndItemIdAndStatusOrderByExpiresAtDesc(Long userId,String itemType,Long itemId,String status); }
