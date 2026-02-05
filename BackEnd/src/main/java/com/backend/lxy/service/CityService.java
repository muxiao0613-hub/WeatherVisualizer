package com.backend.lxy.service;

import com.backend.lxy.client.MockDataFactory;
import com.backend.lxy.client.WeatherClient;
import com.backend.lxy.config.AppProperties;
import com.backend.lxy.domain.dto.CityDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CityService {

    private final MockDataFactory mockDataFactory;
    private final WeatherClient weatherClient;
    private final AppProperties appProperties;

    public List<CityDTO> searchCities(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be empty");
        }

        log.debug("Searching cities with keyword: {}", keyword);

        if (appProperties.isMockEnabled() && !appProperties.isForceRealApi()) {
            log.debug("Using mock data for city search");
            return mockDataFactory.searchCities(keyword);
        }

        try {
            log.debug("Calling real QWeather city search API");
            return weatherClient.searchCities(keyword);
        } catch (Exception e) {
            log.warn("Failed to call city search API, falling back to mock: {}", e.getMessage());
            return mockDataFactory.searchCities(keyword);
        }
    }
}
