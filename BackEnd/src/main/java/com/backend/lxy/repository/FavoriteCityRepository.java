package com.backend.lxy.repository;

import com.backend.lxy.domain.entity.FavoriteCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteCityRepository extends JpaRepository<FavoriteCity, Long> {
    List<FavoriteCity> findAllByOrderByCreatedAtDesc();
    Optional<FavoriteCity> findByNameAndCountryAndLatAndLon(String name, String country, Double lat, Double lon);
    void deleteByNameAndCountryAndLatAndLon(String name, String country, Double lat, Double lon);
}
