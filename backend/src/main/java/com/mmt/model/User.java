package com.mmt.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="users") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @Column(unique=true,nullable=false) String email; String name; String passwordHash; String preferences; String favoriteDestination; String preferredSeat; String preferredRoomType; LocalDateTime createdAt; }
