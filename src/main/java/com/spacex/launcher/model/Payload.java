package com.spacex.launcher.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payloads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payload {
    @Id
    private String id;

    private String name;
    private String type;
    private Double massKg;
    private String orbit;
    private String customer;
}
