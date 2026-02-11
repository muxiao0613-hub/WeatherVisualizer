package com.backend.lxy.controller;

import com.backend.lxy.common.ApiResponse;
import com.backend.lxy.domain.dto.CityDTO;
import com.backend.lxy.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@Tag(name = "Cities", description = "City search APIs")
public class CityController {

    private final CityService cityService;

    @GetMapping("/search")
    @Operation(summary = "Search cities by keyword")
    public ApiResponse<List<CityDTO>> searchCities(
            @Parameter(description = "Search keyword") @RequestParam String keyword) throws Exception {
        List<CityDTO> cities = cityService.searchCities(keyword);
        return ApiResponse.success(cities);
    }
}
