package com.expense.management.controller;

import com.expense.management.dto.request.ApprovalRequest;
import com.expense.management.dto.response.ApiResponse;
import com.expense.management.dto.response.ExpenseResponse;
import com.expense.management.security.UserPrincipal;
import com.expense.management.service.ApprovalService;
import com.expense.management.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.API_APPROVALS)
@RequiredArgsConstructor
@Tag(name = "Approvals", description = "Approval workflow endpoints")
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping("/{expenseId}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Approve a submitted expense (Manager/Admin)")
    public ResponseEntity<ApiResponse<ExpenseResponse>> approve(
            @PathVariable Long expenseId,
            @Valid @RequestBody(required = false) ApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (request == null) request = new ApprovalRequest();
        return ResponseEntity.ok(ApiResponse.ok("Expense approved",
                approvalService.approveExpense(expenseId, request, principal.getId())));
    }

    @PostMapping("/{expenseId}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Reject a submitted expense (Manager/Admin)")
    public ResponseEntity<ApiResponse<ExpenseResponse>> reject(
            @PathVariable Long expenseId,
            @Valid @RequestBody(required = false) ApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (request == null) request = new ApprovalRequest();
        return ResponseEntity.ok(ApiResponse.ok("Expense rejected",
                approvalService.rejectExpense(expenseId, request, principal.getId())));
    }

    @PostMapping("/{expenseId}/pay")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'ADMIN')")
    @Operation(summary = "Mark an approved expense as paid (Accountant/Admin)")
    public ResponseEntity<ApiResponse<ExpenseResponse>> markAsPaid(
            @PathVariable Long expenseId,
            @Valid @RequestBody(required = false) ApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (request == null) request = new ApprovalRequest();
        return ResponseEntity.ok(ApiResponse.ok("Expense marked as paid",
                approvalService.markAsPaid(expenseId, request, principal.getId())));
    }
}
