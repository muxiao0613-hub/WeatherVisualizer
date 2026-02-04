package com.backend.lxy.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentWeatherDTO {
    private String city;
    private Double lat;
    private Double lon;
    private Double temp;
    private Double feelsLike;
    private String description;
    private String icon;
    private Double windSpeed;
    private Integer windDeg;
    private Integer humidity;
    private Double pressure;
    private Double visibility;
    private Long timestamp;
    private String country;
    private Integer aqi;
}
