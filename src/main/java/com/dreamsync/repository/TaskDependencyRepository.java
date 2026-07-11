package com.dreamsync.repository;

import com.dreamsync.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {

    List<TaskDependency> findByDependsOnTaskId(Long taskId);

    List<TaskDependency> findByTaskId(Long taskId);

}