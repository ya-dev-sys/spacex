// ===== LaunchStats (Response DTO) =====
package com.spacex.launcher.dto;

import com.spacex.launcher.model.Launch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LaunchStats {
    private long totalLaunches;
    private double successRate;
    private Launch nextLaunch;
}
