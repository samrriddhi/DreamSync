package com.dreamsync.service;

import com.dreamsync.dto.response.AttachmentResponse;
import com.dreamsync.entity.Attachment;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.User;
import com.dreamsync.enums.ProjectRole;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.AttachmentRepository;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Path UPLOAD_ROOT = Paths.get("uploads").toAbsolutePath().normalize();
    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;

    public AttachmentService(AttachmentRepository attachmentRepository, TaskRepository taskRepository,
                             ProjectMemberRepository projectMemberRepository, CurrentUserService currentUserService) {
        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
    }

    public AttachmentResponse uploadFile(Long taskId, MultipartFile file) throws IOException {
        Task task = findTask(taskId);
        User currentUser = requireMembership(task);
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Uploaded file cannot be empty");
        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("File size must not exceed 10 MB");

        Files.createDirectories(UPLOAD_ROOT);
        Path filePath = UPLOAD_ROOT.resolve(UUID.randomUUID().toString()).normalize();
        if (!filePath.startsWith(UPLOAD_ROOT)) throw new IllegalArgumentException("Invalid upload path");
        Files.copy(file.getInputStream(), filePath);
        try {
            Attachment attachment = new Attachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileType(file.getContentType());
            attachment.setFilePath(filePath.toString());
            attachment.setUploadedAt(LocalDateTime.now());
            attachment.setTask(task);
            attachment.setUploadedBy(currentUser);
            return toResponse(attachmentRepository.save(attachment));
        } catch (RuntimeException exception) {
            Files.deleteIfExists(filePath);
            throw exception;
        }
    }

    public void deleteAttachmentsForTask(Task task) throws IOException {
        List<Attachment> attachments = attachmentRepository.findByTask(task);
        for (Attachment attachment : attachments) {
            Files.deleteIfExists(resolveStoredPath(attachment.getFilePath()));
        }
        attachmentRepository.deleteAll(attachments);
    }

    public List<AttachmentResponse> getAttachments(Long taskId) {
        Task task = findTask(taskId);
        requireMembership(task);
        List<AttachmentResponse> response = new ArrayList<>();
        for (Attachment attachment : attachmentRepository.findByTask(task)) response.add(toResponse(attachment));
        return response;
    }

    public void deleteAttachment(Long attachmentId) throws IOException {
        Attachment attachment = findAuthorizedAttachment(attachmentId);
        User currentUser = currentUserService.getCurrentUser();
        ProjectMember membership = findMembership(attachment.getTask(), currentUser);
        boolean isManager = membership.getRole() == ProjectRole.PROJECT_MANAGER;
        boolean isUploader = attachment.getUploadedBy() != null
                && attachment.getUploadedBy().getId().equals(currentUser.getId());
        if (!isManager && !isUploader)
            throw new AccessDeniedException("Only the uploader or project manager can delete this attachment");
        Path filePath = resolveStoredPath(attachment.getFilePath());
        Files.deleteIfExists(filePath);
        attachmentRepository.delete(attachment);
    }

    public Path downloadAttachment(Long attachmentId) {
        Attachment attachment = findAuthorizedAttachment(attachmentId);
        Path filePath = resolveStoredPath(attachment.getFilePath());
        if (!Files.isRegularFile(filePath)) throw new ResourceNotFoundException("Attachment file not found");
        return filePath;
    }

    public String getAttachmentFileName(Long attachmentId) {
        Attachment attachment = findAuthorizedAttachment(attachmentId);
        return attachment.getFileName() == null || attachment.getFileName().isBlank()
                ? "attachment" : attachment.getFileName();
    }

    private Attachment findAuthorizedAttachment(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
        requireMembership(attachment.getTask());
        return attachment;
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private User requireMembership(Task task) {
        User user = currentUserService.getCurrentUser();
        findMembership(task, user);
        return user;
    }

    private ProjectMember findMembership(Task task, User user) {
        return projectMemberRepository.findByProjectAndUser(task.getProject(), user)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project's team"));
    }

    private Path resolveStoredPath(String storedPath) {
        Path path = Paths.get(storedPath).toAbsolutePath().normalize();
        if (!path.startsWith(UPLOAD_ROOT)) throw new AccessDeniedException("Invalid attachment path");
        return path;
    }

    private AttachmentResponse toResponse(Attachment attachment) {
        AttachmentResponse response = new AttachmentResponse();
        response.setId(attachment.getId());
        response.setFileName(attachment.getFileName());
        response.setFileType(attachment.getFileType());
        response.setUploadedAt(attachment.getUploadedAt());
        User currentUser = currentUserService.getCurrentUser();
        ProjectMember membership = findMembership(attachment.getTask(), currentUser);
        boolean isManager = membership.getRole() == ProjectRole.PROJECT_MANAGER;
        boolean isUploader = attachment.getUploadedBy() != null
                && attachment.getUploadedBy().getId().equals(currentUser.getId());
        response.setCanDelete(isManager || isUploader);
        return response;
    }
}