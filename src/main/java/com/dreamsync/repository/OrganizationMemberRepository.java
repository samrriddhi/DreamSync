package com.dreamsync.repository;

import com.dreamsync.entity.Organization;
import com.dreamsync.entity.OrganizationMember;
import com.dreamsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    List<OrganizationMember> findByUser(User user);

    List<OrganizationMember> findByOrganization(Organization organization);

    Optional<OrganizationMember> findByOrganizationAndUser(Organization organization, User user);

    boolean existsByOrganizationAndUser(Organization organization, User user);
}
