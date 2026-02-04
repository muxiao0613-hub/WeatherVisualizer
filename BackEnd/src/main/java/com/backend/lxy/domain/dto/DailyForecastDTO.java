package com.backend.lxy.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyForecastDTO {
    private String city;
    private Double lat;
    private Double lon;
    private LocalDate date;
    private Double tempMin;
    private Double tempMax;
    private String description;
    private String icon;
    private Double windSpeed;
    private Integer humidity;
    private Double pop;
}
