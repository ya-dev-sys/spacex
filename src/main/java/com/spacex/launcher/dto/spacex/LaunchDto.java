package com.spacex.launcher.dto.spacex;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * DTO pour la réponse de l'API SpaceX /v5/launches
 * Documentation:
 * https://github.com/r-spacex/SpaceX-API/blob/master/docs/launches/v5/one.md
 */
@Data
public class LaunchDto {
    private String id;
    private String name;

    @JsonProperty("date_utc")
    private Instant dateUtc;

    private Boolean success;
    private String details;

    private String rocket;
    private String launchpad;

    private List<String> payloads;

    private LinksDto links;
}
