package com.dreamsync.service;

import com.dreamsync.entity.User;
import com.dreamsync.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import com.dreamsync.dto.request.LoginRequest;
import com.dreamsync.security.JwtService;
import com.dreamsync.dto.response.UserResponse;
import java.util.ArrayList;
import com.dreamsync.mapper.UserMapper;
import com.dreamsync.dto.response.JwtResponse;
import com.dreamsync.entity.RefreshToken;
import com.dreamsync.dto.request.RefreshTokenRequest;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UserMapper userMapper,
                       RefreshTokenService refreshTokenService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.refreshTokenService = refreshTokenService;
    }
    public User saveUser(User user) {

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User updatedUser) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPassword(
                passwordEncoder.encode(updatedUser.getPassword())
        );
        existingUser.setRole(updatedUser.getRole());

        return userRepository.save(existingUser);
    }
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
    }
    public JwtResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String accessToken = jwtService.generateToken(user.getEmail());

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return new JwtResponse(
                accessToken,
                refreshToken.getToken()
        );
    }
    public JwtResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenService.findByToken(request.getRefreshToken());

        if (refreshTokenService.isExpired(refreshToken)) {
            throw new RuntimeException("Refresh token has expired");
        }

        String accessToken =
                jwtService.generateToken(refreshToken.getUser().getEmail());

        return new JwtResponse(
                accessToken,
                refreshToken.getToken()
        );
    }
    public List<UserResponse> getAllUserResponses() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }
    public void logout(String refreshToken) {

        RefreshToken token =
                refreshTokenService.findByToken(refreshToken);

        refreshTokenService.deleteByUser(token.getUser());
    }
    private final UserMapper userMapper;
}