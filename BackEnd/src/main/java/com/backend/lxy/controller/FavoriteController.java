package com.backend.lxy.controller;

import com.backend.lxy.common.ApiResponse;
import com.backend.lxy.domain.dto.CityDTO;
import com.backend.lxy.domain.dto.FavoriteCreateDTO;
import com.backend.lxy.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Favorite city APIs")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "Get all favorite cities")
    public ApiResponse<List<CityDTO>> getAllFavorites() {
        List<CityDTO> favorites = favoriteService.getAllFavorites();
        return ApiResponse.success(favorites);
    }

    @PostMapping
    @Operation(summary = "Add a favorite city")
    public ApiResponse<CityDTO> addFavorite(@RequestBody FavoriteCreateDTO dto) {
        CityDTO favorite = favoriteService.addFavorite(dto);
        return ApiResponse.success(favorite);
    }

    @DeleteMapping
    @Operation(summary = "Remove a favorite city")
    public ApiResponse<Void> removeFavorite(
            @Parameter(description = "City name") @RequestParam String name,
            @Parameter(description = "Country code") @RequestParam String country,
            @Parameter(description = "Latitude") @RequestParam Double lat,
            @Parameter(description = "Longitude") @RequestParam Double lon) {
        favoriteService.removeFavorite(name, country, lat, lon);
        return ApiResponse.success();
    }
}
