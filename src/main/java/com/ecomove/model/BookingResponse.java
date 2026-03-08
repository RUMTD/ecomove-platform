package com.ecomove.model;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class BookingResponse {
    private Long id;
    private String status;
    private LocalDateTime bookingDate;
    private double co2SavedKg;
    private Long userId;
    private Long tripId;
}
