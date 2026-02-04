package com.backend.lxy.service;

import com.backend.lxy.domain.dto.CityDTO;
import com.backend.lxy.domain.dto.FavoriteCreateDTO;
import com.backend.lxy.domain.entity.FavoriteCity;
import com.backend.lxy.repository.FavoriteCityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteCityRepository favoriteCityRepository;

    public List<CityDTO> getAllFavorites() {
        List<FavoriteCity> favorites = favoriteCityRepository.findAllByOrderByCreatedAtDesc();
        return favorites.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CityDTO addFavorite(FavoriteCreateDTO dto) {
        FavoriteCity favorite = FavoriteCity.builder()
                .name(dto.getName())
                .country(dto.getCountry())
                .state(dto.getState())
                .lat(dto.getLat())
                .lon(dto.getLon())
                .createdAt(System.currentTimeMillis())
                .build();

        FavoriteCity saved = favoriteCityRepository.save(favorite);
        log.info("Added favorite city: {}", dto.getName());
        return mapToDTO(saved);
    }

    @Transactional
    public void removeFavorite(String name, String country, Double lat, Double lon) {
        favoriteCityRepository.deleteByNameAndCountryAndLatAndLon(name, country, lat, lon);
        log.info("Removed favorite city: {}", name);
    }

    private CityDTO mapToDTO(FavoriteCity entity) {
        return CityDTO.builder()
                .name(entity.getName())
                .country(entity.getCountry())
                .state(entity.getState())
                .lat(entity.getLat())
                .lon(entity.getLon())
                .build();
    }
}
