package com.expense.management.service;

import com.expense.management.dto.request.ApprovalRequest;
import com.expense.management.dto.response.ExpenseResponse;
import com.expense.management.entity.Approval;
import com.expense.management.entity.Approval.ApprovalAction;
import com.expense.management.entity.Expense;
import com.expense.management.entity.Expense.ExpenseStatus;
import com.expense.management.entity.User;
import com.expense.management.exception.BusinessException;
import com.expense.management.exception.ResourceNotFoundException;
import com.expense.management.repository.ApprovalRepository;
import com.expense.management.repository.ExpenseRepository;
import com.expense.management.repository.UserRepository;
import com.expense.management.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {

    private final ExpenseRepository expenseRepository;
    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Transactional
    public ExpenseResponse approveExpense(Long expenseId, ApprovalRequest request,
                                          Long approverId) {
        Expense expense = getSubmittedExpense(expenseId);
        User approver = getUser(approverId);
        verifyRole(approver, Constants.ROLE_MANAGER, Constants.ROLE_ADMIN);

        recordApproval(expense, approver, ApprovalAction.APPROVE, request.getComment());
        expense.setStatus(ExpenseStatus.APPROVED);
        Expense saved = expenseRepository.save(expense);

        auditLogService.log(approver, Constants.AUDIT_APPROVE, "Expense", expenseId,
                "SUBMITTED", "APPROVED", null);
        notificationService.notifyExpenseApproved(saved);

        log.info("Expense {} approved by {}", expenseId, approver.getEmail());
        return ExpenseResponse.from(saved);
    }

    @Transactional
    public ExpenseResponse rejectExpense(Long expenseId, ApprovalRequest request,
                                         Long approverId) {
        Expense expense = getSubmittedExpense(expenseId);
        User approver = getUser(approverId);
        verifyRole(approver, Constants.ROLE_MANAGER, Constants.ROLE_ADMIN);

        recordApproval(expense, approver, ApprovalAction.REJECT, request.getComment());
        expense.setStatus(ExpenseStatus.REJECTED);
        Expense saved = expenseRepository.save(expense);

        auditLogService.log(approver, Constants.AUDIT_REJECT, "Expense", expenseId,
                "SUBMITTED", "REJECTED", null);
        notificationService.notifyExpenseRejected(saved, request.getComment());

        log.info("Expense {} rejected by {}", expenseId, approver.getEmail());
        return ExpenseResponse.from(saved);
    }

    @Transactional
    public ExpenseResponse markAsPaid(Long expenseId, ApprovalRequest request, Long accountantId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));

        if (expense.getStatus() != ExpenseStatus.APPROVED) {
            throw new BusinessException("Only APPROVED expenses can be marked as paid");
        }

        User accountant = getUser(accountantId);
        verifyRole(accountant, Constants.ROLE_ACCOUNTANT, Constants.ROLE_ADMIN);

        recordApproval(expense, accountant, ApprovalAction.PAY, request.getComment());
        expense.setStatus(ExpenseStatus.PAID);
        Expense saved = expenseRepository.save(expense);

        auditLogService.log(accountant, Constants.AUDIT_PAY, "Expense", expenseId,
                "APPROVED", "PAID", null);
        notificationService.notifyExpensePaid(saved);

        log.info("Expense {} marked as paid by {}", expenseId, accountant.getEmail());
        return ExpenseResponse.from(saved);
    }

    private Expense getSubmittedExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));
        if (expense.getStatus() != ExpenseStatus.SUBMITTED) {
            throw new BusinessException(
                "Only SUBMITTED expenses can be approved or rejected. Current status: "
                    + expense.getStatus());
        }
        return expense;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private void verifyRole(User user, String... allowedRoles) {
        String userRole = user.getRole().getName();
        for (String role : allowedRoles) {
            if (role.equals(userRole)) return;
        }
        throw new BusinessException("Insufficient permissions for this operation",
                HttpStatus.FORBIDDEN);
    }

    private void recordApproval(Expense expense, User approver,
                                 ApprovalAction action, String comment) {
        Approval approval = Approval.builder()
                .expense(expense)
                .approver(approver)
                .action(action)
                .comment(comment)
                .build();
        approvalRepository.save(approval);
    }
}
