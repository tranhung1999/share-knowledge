package com.expense.management.dto.response;

import com.expense.management.entity.Expense;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ExpenseResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private String department;
    private String expenseType;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String description;
    private String status;
    private List<AttachmentResponse> attachments;
    private List<ApprovalResponse> approvals;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExpenseResponse from(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .employeeId(expense.getEmployee().getId())
                .employeeName(expense.getEmployee().getFullName())
                .employeeEmail(expense.getEmployee().getEmail())
                .department(expense.getEmployee().getDepartment() != null
                        ? expense.getEmployee().getDepartment().getName() : null)
                .expenseType(expense.getExpenseType().name())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .description(expense.getDescription())
                .status(expense.getStatus().name())
                .attachments(expense.getAttachments().stream()
                        .map(AttachmentResponse::from)
                        .collect(Collectors.toList()))
                .approvals(expense.getApprovals().stream()
                        .map(ApprovalResponse::from)
                        .collect(Collectors.toList()))
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
