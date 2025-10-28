// ===== PayloadDto =====
package com.spacex.launcher.dto.spacex;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PayloadDto {
    private String id;
    private String name;
    private String type;

    @JsonProperty("mass_kg")
    private Double massKg;

    private String orbit;

    private String customer;
}
