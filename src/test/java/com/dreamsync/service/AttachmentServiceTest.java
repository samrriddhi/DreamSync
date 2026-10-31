package com.dreamsync.service;

import com.dreamsync.entity.*;
import com.dreamsync.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttachmentServiceTest {
    private AttachmentRepository attachments;
    private TaskRepository tasks;
    private ProjectMemberRepository members;
    private CurrentUserService users;
    private AttachmentService service;
    private User current;
    private Project project;
    private Task task;

    @BeforeEach void setUp() {
        attachments = mock(AttachmentRepository.class);
        tasks = mock(TaskRepository.class);
        members = mock(ProjectMemberRepository.class);
        users = mock(CurrentUserService.class);
        service = new AttachmentService(attachments, tasks, members, users);
        current = new User(); current.setId(10L);
        project = new Project(); project.setId(5L);
        task = new Task(); task.setId(7L); task.setProject(project);
        when(users.getCurrentUser()).thenReturn(current);
    }

    @Test void nonMemberCannotUploadFile() {
        when(tasks.findById(7L)).thenReturn(Optional.of(task));
        when(members.existsByProjectAndUser(project, current)).thenReturn(false);
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt",
                "text/plain", "hello".getBytes());
        assertThrows(AccessDeniedException.class, () -> service.uploadFile(7L, file));
        verify(attachments, never()).save(any());
    }

    @Test void nonMemberCannotListAttachments() {
        when(tasks.findById(7L)).thenReturn(Optional.of(task));
        when(members.existsByProjectAndUser(project, current)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.getAttachments(7L));
        verify(attachments, never()).findByTask(any());
    }

    @Test void nonMemberCannotDownloadAttachment() {
        Attachment attachment = new Attachment(); attachment.setTask(task);
        when(attachments.findById(4L)).thenReturn(Optional.of(attachment));
        when(members.existsByProjectAndUser(project, current)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.downloadAttachment(4L));
    }

    @Test void nonMemberCannotDeleteAttachment() {
        Attachment attachment = new Attachment(); attachment.setTask(task);
        when(attachments.findById(4L)).thenReturn(Optional.of(attachment));
        when(members.existsByProjectAndUser(project, current)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.deleteAttachment(4L));
        verify(attachments, never()).delete(any(Attachment.class));
    }

    @Test void emptyUploadIsRejected() {
        when(tasks.findById(7L)).thenReturn(Optional.of(task));
        when(members.existsByProjectAndUser(project, current)).thenReturn(true);
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt",
                "text/plain", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> service.uploadFile(7L, file));
        verify(attachments, never()).save(any());
    }

    @Test void uploadLargerThanTenMegabytesIsRejected() {
        when(tasks.findById(7L)).thenReturn(Optional.of(task));
        when(members.existsByProjectAndUser(project, current)).thenReturn(true);
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(10L * 1024 * 1024 + 1);
        assertThrows(IllegalArgumentException.class, () -> service.uploadFile(7L, file));
        verify(attachments, never()).save(any());
    }

    @Test void storedPathOutsideUploadDirectoryIsRejected() {
        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setFilePath("C:/sensitive/secret.txt");
        when(attachments.findById(4L)).thenReturn(Optional.of(attachment));
        when(members.existsByProjectAndUser(project, current)).thenReturn(true);
        assertThrows(AccessDeniedException.class, () -> service.downloadAttachment(4L));
    }
}