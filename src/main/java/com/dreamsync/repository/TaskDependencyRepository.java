package com.dreamsync.repository;

import com.dreamsync.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {

    List<TaskDependency> findByDependsOnTaskId(Long taskId);

    List<TaskDependency> findByTaskId(Long taskId);

    @Query("select dependency from TaskDependency dependency where dependency.task.project.id = :projectId")
    List<TaskDependency> findByProjectId(@Param("projectId") Long projectId);

}