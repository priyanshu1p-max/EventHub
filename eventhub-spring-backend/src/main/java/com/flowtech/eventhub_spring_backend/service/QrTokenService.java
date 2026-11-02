package com.flowtech.eventhub_spring_backend.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class QrTokenService {

	private final SecureRandom secureRandom = new SecureRandom();

    // Generate a unique random token for every ticket
    public String generateToken() {

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    // Convert the original token into SHA-256 hash
    public String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    token.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                hexString.append(
                    String.format("%02x", b)
                );
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new IllegalStateException(
                "Unable to generate QR token hash", e
            );
        }
    }
}
