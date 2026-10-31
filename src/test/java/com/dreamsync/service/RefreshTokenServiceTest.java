package com.dreamsync.service;

import com.dreamsync.entity.RefreshToken;
import com.dreamsync.entity.User;
import com.dreamsync.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {
    private RefreshTokenRepository repository;
    private RefreshTokenService service;

    @BeforeEach void setUp() {
        repository = mock(RefreshTokenRepository.class);
        service = new RefreshTokenService(repository);
    }

    @Test void rotatingTokenReplacesValueAndRenewsExpiry() {
        RefreshToken token = new RefreshToken();
        token.setId(1L);
        token.setToken("old-token");
        token.setExpiryDate(LocalDateTime.now().plusDays(1));
        token.setUser(new User());
        when(repository.rotateIfCurrent(
                eq(1L), eq("old-token"), any(LocalDateTime.class),
                anyString(), any(LocalDateTime.class)
        )).thenReturn(1);

        RefreshToken rotated = service.rotateRefreshToken(token);

        assertNotEquals("old-token", rotated.getToken());
        assertTrue(rotated.getExpiryDate().isAfter(LocalDateTime.now().plusDays(6)));
        verify(repository).rotateIfCurrent(
                eq(1L), eq("old-token"), any(LocalDateTime.class),
                eq(rotated.getToken()), eq(rotated.getExpiryDate())
        );
    }

    @Test void concurrentOrReplayedRotationIsRejected() {
        RefreshToken token = new RefreshToken();
        token.setId(1L);
        token.setToken("already-used-token");
        token.setExpiryDate(LocalDateTime.now().plusDays(1));

        when(repository.rotateIfCurrent(
                eq(1L), eq("already-used-token"), any(LocalDateTime.class),
                anyString(), any(LocalDateTime.class)
        )).thenReturn(0);

        assertThrows(
                com.dreamsync.exception.ResourceNotFoundException.class,
                () -> service.rotateRefreshToken(token)
        );
    }

    @Test void expiredTokenIsRejectedAtExpiryBoundary() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(LocalDateTime.now().minusSeconds(1));
        assertTrue(service.isExpired(token));
    }

    @Test void validTokenIsNotExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(LocalDateTime.now().plusDays(1));
        assertFalse(service.isExpired(token));
    }
}