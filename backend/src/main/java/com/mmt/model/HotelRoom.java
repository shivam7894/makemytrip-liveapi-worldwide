package com.mmt.model;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="hotel_rooms") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HotelRoom { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; Long hotelId; String roomType; Double price; Integer totalRooms; Integer availableRooms; String imageUrl; String amenities; Boolean refundable; }
