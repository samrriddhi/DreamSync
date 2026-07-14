package com.dreamsync.repository;

import com.dreamsync.entity.ActivityLog;
import com.dreamsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByUserOrderByCreatedAtDesc(User user);

    List<ActivityLog> findAllByOrderByCreatedAtDesc();
}