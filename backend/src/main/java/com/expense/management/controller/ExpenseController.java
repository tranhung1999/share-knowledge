package com.expense.management.controller;

import com.expense.management.dto.request.ExpenseRequest;
import com.expense.management.dto.response.ApiResponse;
import com.expense.management.dto.response.ExpenseResponse;
import com.expense.management.dto.response.PageResponse;
import com.expense.management.entity.Expense.ExpenseStatus;
import com.expense.management.entity.Expense.ExpenseType;
import com.expense.management.security.UserPrincipal;
import com.expense.management.service.ExpenseService;
import com.expense.management.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping(Constants.API_EXPENSES)
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense management endpoints")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Create a new expense")
    public ResponseEntity<ApiResponse<ExpenseResponse>> create(
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ExpenseResponse response = expenseService.createExpense(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Expense created", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a DRAFT expense")
    public ResponseEntity<ApiResponse<ExpenseResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                expenseService.updateExpense(id, request, principal.getId())));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit a DRAFT expense for approval")
    public ResponseEntity<ApiResponse<ExpenseResponse>> submit(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Expense submitted",
                expenseService.submitExpense(id, principal.getId())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a DRAFT expense")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        expenseService.deleteExpense(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Expense deleted", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single expense by ID")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                expenseService.getExpenseById(id, principal.getId(), principal.getRole())));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's expenses")
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> getMyExpenses(
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                expenseService.getMyExpenses(principal.getId(), status, page, size)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMIN')")
    @Operation(summary = "Get all expenses with filters (Manager/Accountant/Admin)")
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> getAll(
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) ExpenseType expenseType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                expenseService.getAllExpenses(status, employeeId, expenseType,
                        startDate, endDate, page, size)));
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload attachment to a DRAFT expense")
    public ResponseEntity<ApiResponse<ExpenseResponse>> uploadAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("File uploaded",
                expenseService.attachFile(id, principal.getId(), file)));
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    @Operation(summary = "Delete an attachment from a DRAFT expense")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable Long id,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        expenseService.deleteAttachment(id, attachmentId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Attachment deleted", null));
    }
}
