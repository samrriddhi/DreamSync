package com.dreamsync.controller;

import com.dreamsync.entity.Organization;
import com.dreamsync.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations")
@Tag(name = "Organizations", description = "Manage organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @Operation(summary = "Create a new organization")
    @PostMapping
    public ResponseEntity<Organization> createOrganization(
            @Valid @RequestBody Organization organization) {

        Organization savedOrganization =
                organizationService.saveOrganization(organization);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedOrganization);
    }

    @Operation(summary = "Get all organizations")
    @GetMapping
    public ResponseEntity<List<Organization>> getAllOrganizations() {

        return ResponseEntity.ok(
                organizationService.getAllOrganizations()
        );
    }

    @Operation(summary = "Get organization by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Organization> getOrganizationById(
            @PathVariable Long id) {

        Organization organization =
                organizationService.getOrganizationById(id);

        return ResponseEntity.ok(organization);
    }
}