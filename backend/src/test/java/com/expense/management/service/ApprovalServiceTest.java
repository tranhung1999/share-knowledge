package com.expense.management.service;

import com.expense.management.dto.request.ApprovalRequest;
import com.expense.management.dto.response.ExpenseResponse;
import com.expense.management.entity.*;
import com.expense.management.entity.Expense.ExpenseStatus;
import com.expense.management.entity.Expense.ExpenseType;
import com.expense.management.exception.BusinessException;
import com.expense.management.exception.ResourceNotFoundException;
import com.expense.management.repository.ApprovalRepository;
import com.expense.management.repository.ExpenseRepository;
import com.expense.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalService Tests")
class ApprovalServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private ApprovalRepository approvalRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private ApprovalService approvalService;

    private User manager;
    private User accountant;
    private User employee;
    private Expense submittedExpense;
    private Expense approvedExpense;
    private ApprovalRequest approvalRequest;

    @BeforeEach
    void setUp() {
        Role managerRole    = Role.builder().id(2L).name("MANAGER").build();
        Role accountantRole = Role.builder().id(3L).name("ACCOUNTANT").build();
        Role employeeRole   = Role.builder().id(4L).name("EMPLOYEE").build();
        Department dept     = Department.builder().id(1L).name("Engineering").build();

        manager = User.builder().id(2L).email("manager@company.com").fullName("John Manager")
                .role(managerRole).department(dept).isActive(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        accountant = User.builder().id(3L).email("accountant@company.com").fullName("Jane Accountant")
                .role(accountantRole).department(dept).isActive(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        employee = User.builder().id(1L).email("alice@company.com").fullName("Alice Johnson")
                .role(employeeRole).department(dept).isActive(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        submittedExpense = Expense.builder()
                .id(1L).employee(employee).expenseType(ExpenseType.TRAVEL)
                .amount(new BigDecimal("500.00")).expenseDate(LocalDate.now())
                .description("Conference travel").status(ExpenseStatus.SUBMITTED)
                .attachments(new ArrayList<>()).approvals(new ArrayList<>())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        approvedExpense = Expense.builder()
                .id(2L).employee(employee).expenseType(ExpenseType.MEALS)
                .amount(new BigDecimal("120.00")).expenseDate(LocalDate.now())
                .description("Client dinner").status(ExpenseStatus.APPROVED)
                .attachments(new ArrayList<>()).approvals(new ArrayList<>())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        approvalRequest = new ApprovalRequest();
        approvalRequest.setComment("Approved as per policy");
    }

    @Test
    @DisplayName("approveExpense: SUBMITTED → APPROVED transition succeeds")
    void approveExpense_submitted_becomesApproved() {
        Expense saved = Expense.builder()
                .id(1L).employee(employee).expenseType(ExpenseType.TRAVEL)
                .amount(new BigDecimal("500.00")).expenseDate(LocalDate.now())
                .description("Conference travel").status(ExpenseStatus.APPROVED)
                .attachments(new ArrayList<>()).approvals(new ArrayList<>())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(submittedExpense));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(approvalRepository.save(any(Approval.class))).thenReturn(mock(Approval.class));
        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);

        ExpenseResponse result = approvalService.approveExpense(1L, approvalRequest, 2L);

        assertThat(result.getStatus()).isEqualTo("APPROVED");
        verify(notificationService).notifyExpenseApproved(any(Expense.class));
    }

    @Test
    @DisplayName("approveExpense: DRAFT expense cannot be approved")
    void approveExpense_draft_throws() {
        Expense draft = Expense.builder()
                .id(1L).employee(employee).expenseType(ExpenseType.TRAVEL)
                .amount(new BigDecimal("100.00")).expenseDate(LocalDate.now())
                .description("Draft").status(ExpenseStatus.DRAFT)
                .attachments(new ArrayList<>()).approvals(new ArrayList<>())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> approvalService.approveExpense(1L, approvalRequest, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only SUBMITTED expenses");
    }

    @Test
    @DisplayName("rejectExpense: SUBMITTED → REJECTED transition succeeds")
    void rejectExpense_submitted_becomesRejected() {
        Expense saved = Expense.builder()
                .id(1L).employee(employee).expenseType(ExpenseType.TRAVEL)
                .amount(new BigDecimal("500.00")).expenseDate(LocalDate.now())
                .description("Conference travel").status(ExpenseStatus.REJECTED)
                .attachments(new ArrayList<>()).approvals(new ArrayList<>())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(submittedExpense));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(approvalRepository.save(any(Approval.class))).thenReturn(mock(Approval.class));
        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);

        ApprovalRequest rejectRequest = new ApprovalRequest();
        rejectRequest.setComment("Not within policy");

        ExpenseResponse result = approvalService.rejectExpense(1L, rejectRequest, 2L);

        assertThat(result.getStatus()).isEqualTo("REJECTED");
        verify(notificationService).notifyExpenseRejected(any(Expense.class), anyString());
    }

    @Test
    @DisplayName("markAsPaid: APPROVED → PAID transition succeeds")
    void markAsPaid_approved_becomesPaid() {
        Expense saved = Expense.builder()
                .id(2L).employee(employee).expenseType(ExpenseType.MEALS)
                .amount(new BigDecimal("120.00")).expenseDate(LocalDate.now())
                .description("Client dinner").status(ExpenseStatus.PAID)
                .attachments(new ArrayList<>()).approvals(new ArrayList<>())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(expenseRepository.findById(2L)).thenReturn(Optional.of(approvedExpense));
        when(userRepository.findById(3L)).thenReturn(Optional.of(accountant));
        when(approvalRepository.save(any(Approval.class))).thenReturn(mock(Approval.class));
        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);

        ExpenseResponse result = approvalService.markAsPaid(2L, approvalRequest, 3L);

        assertThat(result.getStatus()).isEqualTo("PAID");
        verify(notificationService).notifyExpensePaid(any(Expense.class));
    }

    @Test
    @DisplayName("markAsPaid: SUBMITTED expense cannot be marked as paid")
    void markAsPaid_submitted_throws() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(submittedExpense));

        assertThatThrownBy(() -> approvalService.markAsPaid(1L, approvalRequest, 3L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only APPROVED expenses");
    }

    @Test
    @DisplayName("approveExpense: expense not found throws ResourceNotFoundException")
    void approveExpense_notFound_throws() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> approvalService.approveExpense(99L, approvalRequest, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }
}
