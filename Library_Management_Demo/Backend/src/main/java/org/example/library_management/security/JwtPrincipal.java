package org.example.library_management.security;

public record JwtPrincipal(long accountId, String role, String identifier) {
}
