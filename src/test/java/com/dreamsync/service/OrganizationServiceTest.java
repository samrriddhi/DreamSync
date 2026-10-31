package com.dreamsync.service;

import com.dreamsync.dto.request.CreateOrganizationRequest;
import com.dreamsync.entity.Organization;
import com.dreamsync.entity.OrganizationMember;
import com.dreamsync.entity.User;
import com.dreamsync.enums.OrganizationRole;
import com.dreamsync.repository.OrganizationMemberRepository;
import com.dreamsync.repository.OrganizationRepository;
import com.dreamsync.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrganizationServiceTest {

    private OrganizationRepository organizationRepository;
    private OrganizationMemberRepository organizationMemberRepository;
    private UserRepository userRepository;
    private CurrentUserService currentUserService;
    private OrganizationService organizationService;
    private User currentUser;

    @BeforeEach
    void setUp() {
        organizationRepository = mock(OrganizationRepository.class);
        organizationMemberRepository = mock(OrganizationMemberRepository.class);
        userRepository = mock(UserRepository.class);
        currentUserService = mock(CurrentUserService.class);
        organizationService = new OrganizationService(
                organizationRepository,
                organizationMemberRepository,
                userRepository,
                currentUserService
        );

        currentUser = new User();
        currentUser.setId(10L);
        currentUser.setName("Owner");
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    void creatingOrganizationMakesCurrentUserOwner() {
        CreateOrganizationRequest request = new CreateOrganizationRequest();
        request.setName("DreamSync");
        request.setDescription("Workspace");

        when(organizationRepository.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Organization created = organizationService.saveOrganization(request);

        assertEquals("DreamSync", created.getName());
        verify(organizationMemberRepository).save(argThat(member ->
                member.getOrganization() == created
                        && member.getUser() == currentUser
                        && member.getRole() == OrganizationRole.OWNER
        ));
    }

    @Test
    void listingOrganizationsReturnsOnlyCurrentUsersMemberships() {
        Organization organization = new Organization();
        organization.setName("My organization");
        OrganizationMember membership = new OrganizationMember();
        membership.setOrganization(organization);

        when(organizationMemberRepository.findByUser(currentUser))
                .thenReturn(List.of(membership));

        List<Organization> result = organizationService.getAllOrganizations();

        assertEquals(List.of(organization), result);
        verify(organizationMemberRepository).findByUser(currentUser);
        verifyNoInteractions(organizationRepository);
    }

    @Test
    void nonMemberCannotReadOrganization() {
        Organization organization = new Organization();
        when(organizationRepository.findById(5L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationAndUser(organization, currentUser))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> organizationService.getOrganizationById(5L));
    }

    @Test
    void onlyOwnerCanAddOrganizationMember() {
        Organization organization = new Organization();
        User newUser = new User();
        newUser.setId(20L);

        OrganizationMember membership = new OrganizationMember();
        membership.setRole(OrganizationRole.MEMBER);

        when(organizationRepository.findById(5L)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.findByOrganizationAndUser(organization, currentUser))
                .thenReturn(Optional.of(membership));

        assertThrows(AccessDeniedException.class,
                () -> organizationService.addMember(5L, 20L));

        verify(userRepository, never()).findById(any());
        verify(organizationMemberRepository, never()).save(any());
    }
}
