package com.spacex.launcher.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rockets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rocket {
    @Id
    private String id;

    private String name;
    private String type;
    private boolean active;
    private String country;
    private String company;
}
