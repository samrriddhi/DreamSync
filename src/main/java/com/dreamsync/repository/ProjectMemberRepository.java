package com.dreamsync.repository;

import com.dreamsync.entity.Project;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProject(Project project);

    List<ProjectMember> findByUser(User user);

    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    boolean existsByProjectAndUser(Project project, User user);
}