package com.backend.lxy.service;

import com.backend.lxy.domain.dto.PreferenceDTO;
import com.backend.lxy.domain.entity.UserPreference;
import com.backend.lxy.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreferenceService {

    private final UserPreferenceRepository preferenceRepository;

    public PreferenceDTO getPreferences() {
        UserPreference preference = preferenceRepository.findFirstByOrderByIdAsc();

        if (preference == null) {
            preference = createDefaultPreference();
        }

        return mapToDTO(preference);
    }

    @Transactional
    public PreferenceDTO updatePreferences(PreferenceDTO dto) {
        UserPreference preference = preferenceRepository.findFirstByOrderByIdAsc();

        if (preference == null) {
            preference = new UserPreference();
        }

        if (dto.getDefaultCity() != null) {
            preference.setDefaultCity(dto.getDefaultCity());
        }
        if (dto.getTemperatureUnit() != null) {
            preference.setTemperatureUnit(dto.getTemperatureUnit());
        }
        if (dto.getWindSpeedUnit() != null) {
            preference.setWindSpeedUnit(dto.getWindSpeedUnit());
        }
        if (dto.getShowCurrentCard() != null) {
            preference.setShowCurrentCard(dto.getShowCurrentCard());
        }
        if (dto.getShowLineChart() != null) {
            preference.setShowLineChart(dto.getShowLineChart());
        }
        if (dto.getShowBarChart() != null) {
            preference.setShowBarChart(dto.getShowBarChart());
        }
        if (dto.getShowGaugeCard() != null) {
            preference.setShowGaugeCard(dto.getShowGaugeCard());
        }
        if (dto.getShowAlertsCard() != null) {
            preference.setShowAlertsCard(dto.getShowAlertsCard());
        }
        if (dto.getShowAiAssistant() != null) {
            preference.setShowAiAssistant(dto.getShowAiAssistant());
        }

        preference.setUpdatedAt(System.currentTimeMillis());

        UserPreference saved = preferenceRepository.save(preference);
        log.info("Updated user preferences");
        return mapToDTO(saved);
    }

    private UserPreference createDefaultPreference() {
        UserPreference preference = UserPreference.builder()
                .defaultCity("Beijing")
                .temperatureUnit("C")
                .windSpeedUnit("m/s")
                .showCurrentCard(true)
                .showLineChart(true)
                .showBarChart(true)
                .showGaugeCard(true)
                .showAlertsCard(true)
                .showAiAssistant(true)
                .createdAt(System.currentTimeMillis())
                .build();

        return preferenceRepository.save(preference);
    }

    private PreferenceDTO mapToDTO(UserPreference entity) {
        return PreferenceDTO.builder()
                .id(entity.getId())
                .defaultCity(entity.getDefaultCity())
                .temperatureUnit(entity.getTemperatureUnit())
                .windSpeedUnit(entity.getWindSpeedUnit())
                .showCurrentCard(entity.getShowCurrentCard())
                .showLineChart(entity.getShowLineChart())
                .showBarChart(entity.getShowBarChart())
                .showGaugeCard(entity.getShowGaugeCard())
                .showAlertsCard(entity.getShowAlertsCard())
                .showAiAssistant(entity.getShowAiAssistant())
                .build();
    }
}
