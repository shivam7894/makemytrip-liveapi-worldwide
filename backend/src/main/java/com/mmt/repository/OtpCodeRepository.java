package com.mmt.repository;
import com.mmt.model.OtpCode; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface OtpCodeRepository extends JpaRepository<OtpCode,Long> { Optional<OtpCode> findTopByEmailIgnoreCaseAndPurposeAndUsedFalseOrderByIdDesc(String email,String purpose); }
