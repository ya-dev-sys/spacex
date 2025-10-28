// ===== YearlyStats =====
package com.spacex.launcher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YearlyStats {
    private Integer year;
    private long totalLaunches;
    private double successRate;

    public YearlyStats(long totalLaunches, double successRate) {
        this.totalLaunches = totalLaunches;
        this.successRate = successRate;
    }
}
