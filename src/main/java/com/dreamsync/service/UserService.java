package com.dreamsync.service;

import com.dreamsync.dto.request.LoginRequest;
import com.dreamsync.dto.request.RefreshTokenRequest;
import com.dreamsync.dto.request.RegisterRequest;
import com.dreamsync.dto.request.UpdateUserRequest;
import com.dreamsync.dto.response.JwtResponse;
import com.dreamsync.dto.response.UserResponse;
import com.dreamsync.entity.RefreshToken;
import com.dreamsync.entity.User;
import com.dreamsync.enums.Role;
import com.dreamsync.mapper.UserMapper;
import com.dreamsync.repository.UserRepository;
import com.dreamsync.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

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

    public UserResponse saveUser(RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Public registration must never grant elevated privileges.
        user.setRole(Role.DEVELOPER);

        return userMapper.toResponse(userRepository.save(user));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserResponse updateUser(Long id, UpdateUserRequest updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        existingUser.setRole(updatedUser.getRole());

        return userMapper.toResponse(userRepository.save(existingUser));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new JwtResponse(accessToken, refreshToken.getToken());
    }

    public JwtResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken =
                refreshTokenService.findByToken(request.getRefreshToken());

        if (refreshTokenService.isExpired(refreshToken)) {
            throw new RuntimeException("Refresh token has expired");
        }

        String accessToken =
                jwtService.generateToken(refreshToken.getUser().getEmail());

        RefreshToken rotatedToken =
                refreshTokenService.rotateRefreshToken(refreshToken);

        return new JwtResponse(accessToken, rotatedToken.getToken());
    }

    public List<UserResponse> getAllUserResponses() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenService.findByToken(refreshToken);
        refreshTokenService.deleteByUser(token.getUser());
    }
}
