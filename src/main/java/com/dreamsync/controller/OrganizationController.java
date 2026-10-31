package com.dreamsync.controller;

import com.dreamsync.dto.request.AddOrganizationMemberRequest;
import com.dreamsync.dto.request.CreateOrganizationRequest;
import com.dreamsync.entity.Organization;
import com.dreamsync.dto.response.OrganizationResponse;
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
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {
        Organization savedOrganization = organizationService.saveOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(savedOrganization));
    }

    @Operation(summary = "Get organizations the current user belongs to")
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations().stream()
                .map(this::toResponse)
                .toList());
    }

    @Operation(summary = "Get organization by ID")
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(organizationService.getOrganizationById(id)));
    }

    @Operation(summary = "Add a member to an organization")
    @PostMapping("/{organizationId}/members")
    public ResponseEntity<String> addMember(
            @PathVariable Long organizationId,
            @Valid @RequestBody AddOrganizationMemberRequest request) {
        return ResponseEntity.ok(
                organizationService.addMember(organizationId, request.getUserId()));
    }
    private OrganizationResponse toResponse(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getDescription(),
                organization.getCreatedAt()
        );
    }
}
