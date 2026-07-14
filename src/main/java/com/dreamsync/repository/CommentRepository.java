package com.dreamsync.repository;

import com.dreamsync.entity.Comment;
import com.dreamsync.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTaskOrderByCreatedAtAsc(Task task);
}