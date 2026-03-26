package com.expense.management.service;

import com.expense.management.dto.request.ExpenseRequest;
import com.expense.management.dto.response.ExpenseResponse;
import com.expense.management.entity.*;
import com.expense.management.entity.Expense.ExpenseStatus;
import com.expense.management.entity.Expense.ExpenseType;
import com.expense.management.exception.BusinessException;
import com.expense.management.exception.ResourceNotFoundException;
import com.expense.management.repository.ExpenseAttachmentRepository;
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
@DisplayName("ExpenseService Tests")
class ExpenseServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private ExpenseAttachmentRepository attachmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private NotificationService notificationService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private ExpenseService expenseService;

    private User employee;
    private ExpenseRequest validRequest;
    private Expense draftExpense;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name("EMPLOYEE").build();
        Department dept = Department.builder().id(1L).name("Engineering").build();

        employee = User.builder()
                .id(1L)
                .email("alice@company.com")
                .fullName("Alice Johnson")
                .role(role)
                .department(dept)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = new ExpenseRequest();
        validRequest.setExpenseType(ExpenseType.TRAVEL);
        validRequest.setAmount(new BigDecimal("250.00"));
        validRequest.setExpenseDate(LocalDate.now());
        validRequest.setDescription("Business trip to conference");

        draftExpense = Expense.builder()
                .id(1L)
                .employee(employee)
                .expenseType(ExpenseType.TRAVEL)
                .amount(new BigDecimal("250.00"))
                .expenseDate(LocalDate.now())
                .description("Business trip to conference")
                .status(ExpenseStatus.DRAFT)
                .attachments(new ArrayList<>())
                .approvals(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("createExpense: should create and return a DRAFT expense")
    void createExpense_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(expenseRepository.save(any(Expense.class))).thenReturn(draftExpense);

        ExpenseResponse result = expenseService.createExpense(validRequest, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("createExpense: should throw ResourceNotFoundException for unknown employee")
    void createExpense_unknownEmployee_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.createExpense(validRequest, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    @DisplayName("submitExpense: DRAFT → SUBMITTED transition succeeds")
    void submitExpense_draft_becomesSubmitted() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(draftExpense));
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));

        Expense submittedExpense = Expense.builder()
                .id(1L)
                .employee(employee)
                .expenseType(ExpenseType.TRAVEL)
                .amount(new BigDecimal("250.00"))
                .expenseDate(LocalDate.now())
                .description("Business trip")
                .status(ExpenseStatus.SUBMITTED)
                .attachments(new ArrayList<>())
                .approvals(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(expenseRepository.save(any(Expense.class))).thenReturn(submittedExpense);

        ExpenseResponse result = expenseService.submitExpense(1L, 1L);

        assertThat(result.getStatus()).isEqualTo("SUBMITTED");
        verify(notificationService).notifyExpenseSubmitted(any(Expense.class));
    }

    @Test
    @DisplayName("submitExpense: SUBMITTED expense cannot be re-submitted")
    void submitExpense_alreadySubmitted_throws() {
        draftExpense.setStatus(ExpenseStatus.SUBMITTED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(draftExpense));

        assertThatThrownBy(() -> expenseService.submitExpense(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only DRAFT expenses can be submitted");
    }

    @Test
    @DisplayName("submitExpense: different employee cannot submit another's expense")
    void submitExpense_wrongOwner_throws() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(draftExpense));

        // Employee ID 2 tries to submit expense owned by employee 1
        assertThatThrownBy(() -> expenseService.submitExpense(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("don't have permission");
    }

    @Test
    @DisplayName("updateExpense: can update DRAFT expense")
    void updateExpense_draft_success() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(draftExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(draftExpense);

        ExpenseRequest updateRequest = new ExpenseRequest();
        updateRequest.setExpenseType(ExpenseType.MEALS);
        updateRequest.setAmount(new BigDecimal("75.00"));
        updateRequest.setExpenseDate(LocalDate.now());
        updateRequest.setDescription("Team lunch meeting");

        ExpenseResponse result = expenseService.updateExpense(1L, updateRequest, 1L);

        assertThat(result).isNotNull();
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("updateExpense: cannot update non-DRAFT expense")
    void updateExpense_nonDraft_throws() {
        draftExpense.setStatus(ExpenseStatus.SUBMITTED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(draftExpense));

        assertThatThrownBy(() -> expenseService.updateExpense(1L, validRequest, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only DRAFT expenses can be edited");
    }

    @Test
    @DisplayName("deleteExpense: can delete DRAFT expense")
    void deleteExpense_draft_success() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(draftExpense));

        assertThatCode(() -> expenseService.deleteExpense(1L, 1L))
                .doesNotThrowAnyException();
        verify(expenseRepository).delete(draftExpense);
    }

    @Test
    @DisplayName("deleteExpense: cannot delete non-DRAFT expense")
    void deleteExpense_nonDraft_throws() {
        draftExpense.setStatus(ExpenseStatus.APPROVED);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(draftExpense));

        assertThatThrownBy(() -> expenseService.deleteExpense(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only DRAFT expenses can be deleted");
    }
}
