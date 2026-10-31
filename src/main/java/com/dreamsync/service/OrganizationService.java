package com.dreamsync.service;

import com.dreamsync.dto.request.CreateOrganizationRequest;
import com.dreamsync.entity.Organization;
import com.dreamsync.entity.OrganizationMember;
import com.dreamsync.entity.User;
import com.dreamsync.enums.OrganizationRole;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.OrganizationMemberRepository;
import com.dreamsync.repository.OrganizationRepository;
import com.dreamsync.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public OrganizationService(OrganizationRepository organizationRepository,
                               OrganizationMemberRepository organizationMemberRepository,
                               UserRepository userRepository,
                               CurrentUserService currentUserService) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Organization saveOrganization(CreateOrganizationRequest request) {
        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        Organization savedOrganization = organizationRepository.save(organization);

        OrganizationMember owner = new OrganizationMember();
        owner.setOrganization(savedOrganization);
        owner.setUser(currentUserService.getCurrentUser());
        owner.setRole(OrganizationRole.OWNER);
        organizationMemberRepository.save(owner);

        return savedOrganization;
    }

    public List<Organization> getAllOrganizations() {
        User currentUser = currentUserService.getCurrentUser();
        return organizationMemberRepository.findByUser(currentUser).stream()
                .map(OrganizationMember::getOrganization)
                .toList();
    }

    public Organization getOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        requireMembership(organization, currentUserService.getCurrentUser());
        return organization;
    }

    @Transactional
    public String addMember(Long organizationId, Long userId) {
        Organization organization = findOrganization(organizationId);
        User currentUser = currentUserService.getCurrentUser();

        OrganizationMember currentMembership = organizationMemberRepository
                .findByOrganizationAndUser(organization, currentUser)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));

        if (currentMembership.getRole() != OrganizationRole.OWNER) {
            throw new AccessDeniedException("Only the organization owner can add members");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (organizationMemberRepository.existsByOrganizationAndUser(organization, user)) {
            throw new IllegalStateException("User is already a member of this organization");
        }

        OrganizationMember member = new OrganizationMember();
        member.setOrganization(organization);
        member.setUser(user);
        member.setRole(OrganizationRole.MEMBER);
        organizationMemberRepository.save(member);

        return "Organization member added successfully";
    }

    public void requireMembership(Organization organization, User user) {
        if (!organizationMemberRepository.existsByOrganizationAndUser(organization, user)) {
            throw new AccessDeniedException("You are not a member of this organization");
        }
    }

    private Organization findOrganization(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }
}
