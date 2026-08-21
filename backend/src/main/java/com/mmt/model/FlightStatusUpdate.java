package com.mmt.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="flight_status_updates") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlightStatusUpdate { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; Long flightId; String status; Integer delayMinutes; String message; LocalDateTime estimatedArrivalTime; LocalDateTime updatedAt; }
