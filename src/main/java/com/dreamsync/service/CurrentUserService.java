package com.dreamsync.service;

import com.dreamsync.entity.User;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User loggedInUser)) {
            throw new AccessDeniedException("Authentication is required");
        }

        return userRepository.findById(loggedInUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }
}