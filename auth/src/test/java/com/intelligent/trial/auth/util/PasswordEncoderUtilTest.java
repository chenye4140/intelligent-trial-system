package com.intelligent.trial.auth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PasswordEncoderUtil 单元测试
 */
class PasswordEncoderUtilTest {

    @Test
    void encode_shouldReturnNonNullString() {
        String encoded = PasswordEncoderUtil.encode("testPassword123");
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
    }

    @Test
    void encode_shouldProduceBCryptHash() {
        String encoded = PasswordEncoderUtil.encode("myPassword");
        // BCrypt hashes start with $2a$, $2b$, or $2y$
        assertTrue(encoded.startsWith("$2"));
    }

    @Test
    void encode_shouldGenerateDifferentHashesForSamePassword() {
        String password = "samePassword";
        String hash1 = PasswordEncoderUtil.encode(password);
        String hash2 = PasswordEncoderUtil.encode(password);
        // BCrypt uses random salt, so hashes should differ
        assertNotEquals(hash1, hash2);
    }

    @Test
    void matches_shouldReturnTrueForCorrectPassword() {
        String rawPassword = "correctPassword";
        String encoded = PasswordEncoderUtil.encode(rawPassword);
        assertTrue(PasswordEncoderUtil.matches(rawPassword, encoded));
    }

    @Test
    void matches_shouldReturnFalseForWrongPassword() {
        String encoded = PasswordEncoderUtil.encode("correctPassword");
        assertFalse(PasswordEncoderUtil.matches("wrongPassword", encoded));
    }

    @Test
    void matches_shouldThrowForNullRawPassword() {
        String encoded = PasswordEncoderUtil.encode("somePassword");
        // BCrypt throws IllegalArgumentException for null rawPassword
        assertThrows(IllegalArgumentException.class, () -> PasswordEncoderUtil.matches(null, encoded));
    }

    @Test
    void matches_shouldReturnFalseForNullEncodedPassword() {
        assertFalse(PasswordEncoderUtil.matches("somePassword", null));
    }

    @Test
    void matches_shouldThrowForBothNull() {
        // BCrypt throws IllegalArgumentException for null rawPassword
        assertThrows(IllegalArgumentException.class, () -> PasswordEncoderUtil.matches(null, null));
    }

    @Test
    void matches_shouldHandleEmptyPassword() {
        String encoded = PasswordEncoderUtil.encode("");
        assertTrue(PasswordEncoderUtil.matches("", encoded));
        assertFalse(PasswordEncoderUtil.matches("notempty", encoded));
    }

    @Test
    void encode_shouldHandleSpecialCharacters() {
        String specialPassword = "P@ssw0rd!#$%^&*()_+-=[]{}|;':\",./<>?";
        String encoded = PasswordEncoderUtil.encode(specialPassword);
        assertTrue(PasswordEncoderUtil.matches(specialPassword, encoded));
    }

    @Test
    void encode_shouldHandleUnicodePassword() {
        String unicodePassword = "密码123测试";
        String encoded = PasswordEncoderUtil.encode(unicodePassword);
        assertTrue(PasswordEncoderUtil.matches(unicodePassword, encoded));
    }

    @Test
    void encode_shouldHandleLongPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append('a');
        String longPassword = sb.toString();
        String encoded = PasswordEncoderUtil.encode(longPassword);
        assertTrue(PasswordEncoderUtil.matches(longPassword, encoded));
    }
}
