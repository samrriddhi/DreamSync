package com.dreamsync.repository;

import com.dreamsync.entity.ActivityLog;
import com.dreamsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop100ByUserOrderByCreatedAtDesc(User user);

    List<ActivityLog> findTop100ByOrderByCreatedAtDesc();
}