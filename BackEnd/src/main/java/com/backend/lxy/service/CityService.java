package com.backend.lxy.service;

import com.backend.lxy.client.MockDataFactory;
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

    public List<CityDTO> searchCities(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be empty");
        }

        log.debug("Searching cities with keyword: {}", keyword);
        return mockDataFactory.searchCities(keyword);
    }
}
