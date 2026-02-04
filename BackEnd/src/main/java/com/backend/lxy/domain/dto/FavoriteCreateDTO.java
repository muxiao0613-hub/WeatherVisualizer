package com.backend.lxy.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteCreateDTO {
    private String name;
    private String country;
    private String state;
    private Double lat;
    private Double lon;
}
