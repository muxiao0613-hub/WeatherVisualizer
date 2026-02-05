package com.backend.lxy.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "default_city")
    @Builder.Default
    private String defaultCity = "北京";

    @Column(name = "temperature_unit")
    @Builder.Default
    private String temperatureUnit = "C";

    @Column(name = "wind_speed_unit")
    @Builder.Default
    private String windSpeedUnit = "m/s";

    @Column(name = "show_current_card")
    @Builder.Default
    private Boolean showCurrentCard = true;

    @Column(name = "show_line_chart")
    @Builder.Default
    private Boolean showLineChart = true;

    @Column(name = "show_bar_chart")
    @Builder.Default
    private Boolean showBarChart = true;

    @Column(name = "show_gauge_card")
    @Builder.Default
    private Boolean showGaugeCard = true;

    @Column(name = "show_alerts_card")
    @Builder.Default
    private Boolean showAlertsCard = true;

    @Column(name = "show_ai_assistant")
    @Builder.Default
    private Boolean showAiAssistant = true;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;
}
