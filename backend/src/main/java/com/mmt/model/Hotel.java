package com.mmt.model;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="hotels") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Hotel { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; String name; String city; String address; Double rating; Integer reviewCount; Double basePrice; Double currentPrice; String imageUrl; String amenities; String description; Integer demandScore; }
