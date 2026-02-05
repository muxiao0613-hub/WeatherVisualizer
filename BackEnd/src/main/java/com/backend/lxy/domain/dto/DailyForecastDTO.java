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
    private String iconDay;
    private String textDay;
    private String iconNight;
    private String textNight;
    private Integer wind360Day;
    private String windDirDay;
    private String windScaleDay;
    private Double windSpeedDay;
    private Integer wind360Night;
    private String windDirNight;
    private String windScaleNight;
    private Double windSpeedNight;
    private Integer humidity;
    private Double pressure;
    private Double visibility;
    private Double precip;
    private Integer cloud;
    private Double dew;
    private Double pop;
    private String sunrise;
    private String sunset;
    private String moonrise;
    private String moonset;
    private String moonPhase;
    private String moonPhaseIcon;
    private Integer uvIndex;
}
