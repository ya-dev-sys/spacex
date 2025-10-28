// ===== LaunchPadDto =====
package com.spacex.launcher.dto.spacex;

import lombok.Data;

@Data
public class LaunchPadDto {
    private String id;
    private String name;
    private String locality;
    private String region;
    private Double latitude;
    private Double longitude;
}
