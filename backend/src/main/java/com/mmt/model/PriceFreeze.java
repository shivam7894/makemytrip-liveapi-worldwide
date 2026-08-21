package com.mmt.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="price_freezes") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceFreeze { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; Long userId; String itemType; Long itemId; Double lockedPrice; LocalDateTime expiresAt; String status; }
