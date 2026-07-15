package com.dreamsync.service;

import com.dreamsync.dto.response.AttachmentResponse;
import com.dreamsync.entity.Attachment;
import com.dreamsync.entity.Task;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.AttachmentRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import java.util.List;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             TaskRepository taskRepository) {

        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
    }
    public AttachmentResponse uploadFile(Long taskId,
                                         MultipartFile file) throws IOException {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));

        String uploadDir = "uploads/";

        Files.createDirectories(Paths.get(uploadDir));

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        Path filePath = Paths.get(uploadDir, fileName);

        Files.copy(file.getInputStream(), filePath);

        Attachment attachment = new Attachment();

        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileType(file.getContentType());
        attachment.setFilePath(filePath.toString());
        attachment.setUploadedAt(LocalDateTime.now());
        attachment.setTask(task);

        Attachment savedAttachment = attachmentRepository.save(attachment);

        AttachmentResponse response = new AttachmentResponse();

        response.setId(savedAttachment.getId());
        response.setFileName(savedAttachment.getFileName());
        response.setFileType(savedAttachment.getFileType());
        response.setUploadedAt(savedAttachment.getUploadedAt());

        return response;
    }
    public List<AttachmentResponse> getAttachments(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));

        List<Attachment> attachments = attachmentRepository.findByTask(task);

        List<AttachmentResponse> response = new ArrayList<>();

        for (Attachment attachment : attachments) {

            AttachmentResponse dto = new AttachmentResponse();

            dto.setId(attachment.getId());
            dto.setFileName(attachment.getFileName());
            dto.setFileType(attachment.getFileType());
            dto.setUploadedAt(attachment.getUploadedAt());

            response.add(dto);
        }

        return response;
    }
    public void deleteAttachment(Long attachmentId) throws IOException {

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Attachment not found"));

        Files.deleteIfExists(Paths.get(attachment.getFilePath()));

        attachmentRepository.delete(attachment);
    }
    public Path downloadAttachment(Long attachmentId) {

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Attachment not found"));

        return Paths.get(attachment.getFilePath());
    }

}