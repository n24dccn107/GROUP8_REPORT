package org.example.library_management.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtService {
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes.");
        }
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(long accountId, String role, String identifier) {
        long expiresAt = Instant.now().plusSeconds(expirationSeconds).getEpochSecond();
        String payloadJson = "{\"sub\":" + accountId
                + ",\"role\":\"" + escapeJson(role)
                + "\",\"identifier\":\"" + escapeJson(identifier)
                + "\",\"exp\":" + expiresAt + "}";

        String unsignedToken = ENCODER.encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8))
                + "." + ENCODER.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return unsignedToken + "." + ENCODER.encodeToString(sign(unsignedToken));
    }

    public JwtPrincipal verify(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) return null;

            String headerJson = new String(DECODER.decode(parts[0]), StandardCharsets.UTF_8);
            if (!HEADER_JSON.equals(headerJson)) return null;

            byte[] expectedSignature = sign(parts[0] + "." + parts[1]);
            byte[] actualSignature = DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) return null;

            String payload = new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            long accountId = longClaim(payload, "sub");
            long expiresAt = longClaim(payload, "exp");
            String role = stringClaim(payload, "role");
            String identifier = stringClaim(payload, "identifier");
            if (Instant.now().getEpochSecond() >= expiresAt) return null;
            if (!"Member".equals(role) && !"Librarian".equals(role)) return null;

            return new JwtPrincipal(accountId, role, identifier);
        } catch (Exception exception) {
            return null;
        }
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign JWT.", exception);
        }
    }

    private static long longClaim(String json, String claim) {
        Matcher matcher = Pattern.compile("\\\"" + claim + "\\\":([0-9]+)").matcher(json);
        if (!matcher.find()) throw new IllegalArgumentException("JWT claim missing: " + claim);
        return Long.parseLong(matcher.group(1));
    }

    private static String stringClaim(String json, String claim) {
        Matcher matcher = Pattern.compile("\\\"" + claim + "\\\":\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"").matcher(json);
        if (!matcher.find()) throw new IllegalArgumentException("JWT claim missing: " + claim);
        return matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
