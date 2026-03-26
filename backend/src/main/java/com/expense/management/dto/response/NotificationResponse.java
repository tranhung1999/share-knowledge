package com.expense.management.dto.response;

import com.expense.management.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private Long expenseId;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType().name())
                .expenseId(n.getExpense() != null ? n.getExpense().getId() : null)
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
