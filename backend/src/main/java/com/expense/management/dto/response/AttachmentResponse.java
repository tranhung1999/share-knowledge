package com.expense.management.dto.response;

import com.expense.management.entity.ExpenseAttachment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String downloadUrl;
    private LocalDateTime createdAt;

    public static AttachmentResponse from(ExpenseAttachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .downloadUrl("/api/v1/files/" + attachment.getId())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
