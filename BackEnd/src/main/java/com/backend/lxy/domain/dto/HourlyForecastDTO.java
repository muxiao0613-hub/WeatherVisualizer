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
public class HourlyForecastDTO {
    private String city;
    private Double lat;
    private Double lon;
    private LocalDateTime time;
    private Double temp;
    private Double feelsLike;
    private String description;
    private String icon;
    private Double windSpeed;
    private Integer humidity;
    private Double pop;
}
