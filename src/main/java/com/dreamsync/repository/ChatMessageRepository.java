package com.dreamsync.repository;

import com.dreamsync.entity.ChatMessage;
import com.dreamsync.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop100ByProjectOrderBySentAtDesc(Project project);
}
