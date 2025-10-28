package com.spacex.launcher.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.spacex.launcher.dto.spacex.LaunchDto;
import com.spacex.launcher.dto.spacex.LaunchPadDto;
import com.spacex.launcher.dto.spacex.PayloadDto;
import com.spacex.launcher.dto.spacex.RocketDto;
import com.spacex.launcher.model.Launch;
import com.spacex.launcher.model.LaunchPad;
import com.spacex.launcher.model.Payload;
import com.spacex.launcher.model.Rocket;

/**
 * Mapper pour convertir les DTOs de l'API SpaceX en entités JPA
 * Pattern: Manual Mapping (évite les dépendances MapStruct pour ce cas simple)
 */
@Component
public class LaunchMapper {

    /**
     * Convertit un LaunchDto en entité Launch
     */
    public Launch toEntity(LaunchDto dto, Rocket rocket, LaunchPad launchPad) {
        if (dto == null) {
            return null;
        }

        List<Payload> payloads = dto.getPayloads() != null
                ? dto.getPayloads().stream()
                        .map(id -> Payload.builder().id(id).build())
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return Launch.builder()
                .id(dto.getId())
                .name(dto.getName())
                .dateUtc(dto.getDateUtc())
                .success(dto.getSuccess())
                .details(dto.getDetails())
                .rocket(rocket)
                .launchPad(launchPad)
                .payloads(payloads)
                .build();
    }

    /**
     * Convertit un RocketDto en entité Rocket
     */
    public Rocket toEntity(RocketDto dto) {
        if (dto == null) {
            return null;
        }

        return Rocket.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .active(dto.isActive())
                .country(dto.getCountry())
                .company(dto.getCompany())
                .build();
    }

    /**
     * Convertit un LaunchPadDto en entité LaunchPad
     */
    public LaunchPad toEntity(LaunchPadDto dto) {
        if (dto == null) {
            return null;
        }

        return LaunchPad.builder()
                .id(dto.getId())
                .name(dto.getName())
                .locality(dto.getLocality())
                .region(dto.getRegion())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
    }

    /**
     * Convertit un PayloadDto en entité Payload
     */
    public Payload toEntity(PayloadDto dto) {
        if (dto == null) {
            return null;
        }

        return Payload.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .massKg(dto.getMassKg())
                .orbit(dto.getOrbit())
                .build();
    }
}
