package com.backend.lxy.controller;

import com.backend.lxy.common.ApiResponse;
import com.backend.lxy.domain.dto.PreferenceDTO;
import com.backend.lxy.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
@Tag(name = "Preferences", description = "User preference APIs")
public class PreferenceController {

    private final PreferenceService preferenceService;

    @GetMapping
    @Operation(summary = "Get user preferences")
    public ApiResponse<PreferenceDTO> getPreferences() {
        PreferenceDTO preferences = preferenceService.getPreferences();
        return ApiResponse.success(preferences);
    }

    @PutMapping
    @Operation(summary = "Update user preferences")
    public ApiResponse<PreferenceDTO> updatePreferences(@RequestBody PreferenceDTO dto) {
        PreferenceDTO updated = preferenceService.updatePreferences(dto);
        return ApiResponse.success(updated);
    }
}
