// ===== RocketDto =====
package com.spacex.launcher.dto.spacex;

import lombok.Data;

@Data
public class RocketDto {
    private String id;
    private String name;
    private String type;
    private boolean active;
    private String country;
    private String company;
}
