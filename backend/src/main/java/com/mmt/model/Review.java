package com.mmt.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime; import java.util.List;
@Entity @Table(name="reviews") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; Long userId; String entityType; Long entityId; Integer rating; @Column(columnDefinition="TEXT") String text; @Column(columnDefinition="LONGTEXT") String photoUrl; boolean flagged; boolean moderatedRemoved; Integer helpfulCount; LocalDateTime createdAt; @Transient List<ReviewReply> replies; }
