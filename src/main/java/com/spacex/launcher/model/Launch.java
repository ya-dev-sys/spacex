package com.spacex.launcher.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "launches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Launch {
    @Id
    private String id;

    private String name;
    private Instant dateUtc;
    private Boolean success;

    @Column(columnDefinition = "text")
    private String details;

    @ManyToOne
    @JoinColumn(name = "rocket_id")
    private Rocket rocket;

    @ManyToOne
    @JoinColumn(name = "launch_pad_id")
    private LaunchPad launchPad;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "launch_id")
    private List<Payload> payloads = new ArrayList<>();
}
