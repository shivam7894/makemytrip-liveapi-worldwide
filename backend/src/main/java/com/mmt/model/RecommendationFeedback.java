package com.mmt.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="recommendation_feedback") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecommendationFeedback { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; Long userId; String itemType; Long itemId; boolean helpful; LocalDateTime createdAt; }
