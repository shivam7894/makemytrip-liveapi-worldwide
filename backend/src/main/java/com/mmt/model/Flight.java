package com.mmt.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="flights") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Flight { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; String airline; String flightNumber; String sourceCity; String destinationCity; LocalDateTime departureTime; LocalDateTime arrivalTime; Double price; Double basePrice; Integer totalSeats; Integer availableSeats; String status; Integer delayMinutes; String delayReason; LocalDateTime estimatedDepartureTime; LocalDateTime estimatedArrivalTime; LocalDateTime lastUpdated; }
