package com.mmt.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="otp_codes") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpCode { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @Column(nullable=false) String email; @Column(nullable=false) String purpose; @Column(nullable=false) String code; LocalDateTime expiresAt; boolean used; }
