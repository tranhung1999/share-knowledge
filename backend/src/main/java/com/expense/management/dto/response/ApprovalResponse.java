package com.expense.management.dto.response;

import com.expense.management.entity.Approval;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApprovalResponse {
    private Long id;
    private Long expenseId;
    private String approverName;
    private String approverEmail;
    private String action;
    private String comment;
    private LocalDateTime createdAt;

    public static ApprovalResponse from(Approval approval) {
        return ApprovalResponse.builder()
                .id(approval.getId())
                .expenseId(approval.getExpense().getId())
                .approverName(approval.getApprover().getFullName())
                .approverEmail(approval.getApprover().getEmail())
                .action(approval.getAction().name())
                .comment(approval.getComment())
                .createdAt(approval.getCreatedAt())
                .build();
    }
}
