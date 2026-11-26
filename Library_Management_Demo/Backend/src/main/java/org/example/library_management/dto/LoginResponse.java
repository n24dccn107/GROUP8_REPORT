package org.example.library_management.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        String role,
        String identifier,
        long expiresInSeconds
) {
}
