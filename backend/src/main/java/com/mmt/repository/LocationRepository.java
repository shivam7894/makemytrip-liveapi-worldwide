package com.mmt.repository;
import com.mmt.model.LocationRecord; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface LocationRepository extends JpaRepository<LocationRecord,Long> { List<LocationRecord> findTop100ByNameContainingIgnoreCaseOrCityContainingIgnoreCase(String name,String city); List<LocationRecord> findByType(String type); long countByType(String type); }
