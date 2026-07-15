package com.dreamsync.service;

import com.dreamsync.entity.RefreshToken;
import com.dreamsync.entity.User;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }
    @Transactional
    public RefreshToken createRefreshToken(User user) {

        Optional<RefreshToken> existingToken =
                refreshTokenRepository.findByUser(user);

        if (existingToken.isPresent()) {

            RefreshToken refreshToken = existingToken.get();

            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

            return refreshTokenRepository.save(refreshToken);
        }

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        return refreshTokenRepository.save(refreshToken);
    }
    public RefreshToken findByToken(String token) {

        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Refresh token not found"));
    }
    public boolean isExpired(RefreshToken refreshToken) {

        return refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now());
    }
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

}