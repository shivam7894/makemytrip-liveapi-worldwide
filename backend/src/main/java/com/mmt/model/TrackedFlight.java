package com.mmt.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="tracked_flights") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackedFlight { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; Long userId; Long flightId; LocalDateTime createdAt; }
