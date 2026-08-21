package com.mmt.model;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="locations", indexes={@Index(name="idx_loc_name",columnList="name"),@Index(name="idx_loc_type",columnList="type")}) @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationRecord { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; String type; String name; String state; String district; String city; String code; String country; Double latitude; Double longitude; }
