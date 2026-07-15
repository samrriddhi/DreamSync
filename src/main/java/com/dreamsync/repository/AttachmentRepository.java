package com.dreamsync.repository;

import com.dreamsync.entity.Attachment;
import com.dreamsync.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTask(Task task);
}