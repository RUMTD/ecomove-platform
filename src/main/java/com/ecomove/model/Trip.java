package com.ecomove.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Trip {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long driverId;
    private String departureAddress;
    private String arrivalAddress;
    private LocalDateTime departureTime;
    private int totalSeats;
    private int availableSeats;
    private double co2PerKmKg;
    @Enumerated(EnumType.STRING)
    private TripStatus status;
    public enum TripStatus { ACTIVE, FULL, CANCELLED }
}
