package com.expense.management.controller;

import com.expense.management.entity.ExpenseAttachment;
import com.expense.management.exception.ResourceNotFoundException;
import com.expense.management.repository.ExpenseAttachmentRepository;
import com.expense.management.security.UserPrincipal;
import com.expense.management.service.FileStorageService;
import com.expense.management.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.API_FILES)
@RequiredArgsConstructor
@Tag(name = "Files", description = "File download endpoints")
public class FileController {

    private final FileStorageService fileStorageService;
    private final ExpenseAttachmentRepository attachmentRepository;

    @GetMapping("/{attachmentId}")
    @Operation(summary = "Download an attachment file")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserPrincipal principal) {

        ExpenseAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));

        byte[] data = fileStorageService.loadFile(attachment.getFilePath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(attachment.getFileName())
                                .build().toString())
                .body(data);
    }
}
