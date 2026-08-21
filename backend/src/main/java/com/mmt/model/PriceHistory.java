package com.mmt.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="price_history") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceHistory { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; String itemType; Long itemId; Double price; Double multiplier; String reason; LocalDateTime recordedAt; }
