package com.spacex.launcher.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "launch_pads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaunchPad {
    @Id
    private String id;
    private String name;
    private String locality;
    private String region;
    private Double latitude;
    private Double longitude;
}
