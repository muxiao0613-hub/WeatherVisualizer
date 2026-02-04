package com.backend.lxy.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceDTO {
    private Long id;
    private String defaultCity;
    private String temperatureUnit;
    private String windSpeedUnit;
    private Boolean showCurrentCard;
    private Boolean showLineChart;
    private Boolean showBarChart;
    private Boolean showGaugeCard;
    private Boolean showAlertsCard;
    private Boolean showAiAssistant;
}
