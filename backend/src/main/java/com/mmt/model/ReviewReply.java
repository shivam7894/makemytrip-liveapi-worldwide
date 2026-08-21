package com.mmt.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="review_replies") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewReply { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; Long reviewId; Long userId; String text; LocalDateTime createdAt; }
