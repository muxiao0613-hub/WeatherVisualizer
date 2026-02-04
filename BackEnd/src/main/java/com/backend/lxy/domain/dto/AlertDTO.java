package com.backend.lxy.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    private String city;
    private Double lat;
    private Double lon;
    private String event;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private String level;
    private String tags;
}
