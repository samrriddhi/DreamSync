package com.dreamsync.controller;

import com.dreamsync.dto.request.LoginRequest;
import com.dreamsync.entity.User;
import com.dreamsync.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dreamsync.dto.response.JwtResponse;
import com.dreamsync.dto.request.RefreshTokenRequest;
import com.dreamsync.dto.request.LogoutRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {

        User savedUser = userService.saveUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {

        JwtResponse response = userService.login(request);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        JwtResponse response = userService.refreshToken(request);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestBody LogoutRequest request) {

        userService.logout(request.getRefreshToken());

        return ResponseEntity.ok("Logged out successfully");
    }
}