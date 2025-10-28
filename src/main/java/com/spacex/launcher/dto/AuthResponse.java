// ===== AuthResponse =====
package com.spacex.launcher.dto;

public record AuthResponse(
        String token,
        String type) {
}
