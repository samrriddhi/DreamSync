package com.dreamsync.controller;

import com.dreamsync.dto.response.AttachmentResponse;
import com.dreamsync.service.AttachmentService;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<AttachmentResponse> uploadFile(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file) throws IOException {

        return ResponseEntity.ok(
                attachmentService.uploadFile(taskId, file)
        );
    }

    @GetMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(
            @PathVariable Long taskId) {

        return ResponseEntity.ok(
                attachmentService.getAttachments(taskId)
        );
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long attachmentId) {

        Path path = attachmentService.downloadAttachment(attachmentId);

        Resource resource = new PathResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long attachmentId) throws IOException {

        attachmentService.deleteAttachment(attachmentId);

        return ResponseEntity.noContent().build();
    }
}