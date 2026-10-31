package com.dreamsync.controller;

import com.dreamsync.dto.request.LoginRequest;
import com.dreamsync.dto.request.RegisterRequest;
import com.dreamsync.dto.request.RefreshTokenRequest;
import com.dreamsync.dto.request.LogoutRequest;
import com.dreamsync.dto.response.JwtResponse;
import com.dreamsync.dto.response.UserResponse;
import com.dreamsync.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse savedUser = userService.saveUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(userService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody LogoutRequest request) {
        userService.logout(request.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }
}
